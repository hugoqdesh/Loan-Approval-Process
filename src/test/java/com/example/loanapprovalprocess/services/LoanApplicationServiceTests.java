package com.example.loanapprovalprocess.services;

import com.example.loanapprovalprocess.api.dto.CreateLoanApplicationRequest;
import com.example.loanapprovalprocess.api.dto.LoanApplicationResponse;
import com.example.loanapprovalprocess.entities.LoanApplicationEntity;
import com.example.loanapprovalprocess.entities.PaymentScheduleEntryEntity;
import com.example.loanapprovalprocess.enums.LoanApplicationStatus;
import com.example.loanapprovalprocess.enums.RejectionReason;
import com.example.loanapprovalprocess.exceptions.ActiveLoanApplicationExistsException;
import com.example.loanapprovalprocess.exceptions.LoanApplicationNotFoundException;
import com.example.loanapprovalprocess.exceptions.LoanApplicationNotReviewableException;
import com.example.loanapprovalprocess.repositories.LoanApplicationRepository;
import com.example.loanapprovalprocess.support.AnnuityPaymentScheduleCalculator;
import com.example.loanapprovalprocess.support.PersonalCodeSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTests {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-04-17T09:30:00Z"), ZoneOffset.UTC);

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private PersonalCodeSupport personalCodeSupport;

    @Mock
    private AnnuityPaymentScheduleCalculator paymentScheduleCalculator;

    @Captor
    private ArgumentCaptor<LoanApplicationEntity> loanApplicationCaptor;

    private LoanApplicationService loanApplicationService;

    @BeforeEach
    void setUp() {
        loanApplicationService = new LoanApplicationService(
                loanApplicationRepository,
                personalCodeSupport,
                paymentScheduleCalculator,
                FIXED_CLOCK,
                70
        );
    }

    @Test
    void submitCreatesInReviewApplicationAndScheduleForEligibleCustomer() {
        CreateLoanApplicationRequest request = validCreateRequest();
        LocalDate birthDate = LocalDate.of(1995, 4, 12);
        List<PaymentScheduleEntryEntity> schedule = List.of(
                scheduleEntry(1, LocalDate.of(2026, 4, 17), "10000.00", "820.89", "18.63", "839.52", "9179.11"),
                scheduleEntry(2, LocalDate.of(2026, 5, 17), "9179.11", "822.42", "17.10", "839.52", "8356.69")
        );

        when(loanApplicationRepository.existsByPersonalCodeAndStatus("39504120016", LoanApplicationStatus.IN_REVIEW))
                .thenReturn(false);
        when(personalCodeSupport.extractBirthDate("39504120016")).thenReturn(birthDate);
        when(personalCodeSupport.calculateAge("39504120016", LocalDate.of(2026, 4, 17))).thenReturn(31);
        when(paymentScheduleCalculator.generate(
                new BigDecimal("10000.00"),
                new BigDecimal("2.235"),
                12,
                LocalDate.of(2026, 4, 17)
        )).thenReturn(schedule);
        when(loanApplicationRepository.saveAndFlush(any(LoanApplicationEntity.class)))
                .thenAnswer(invocation -> {
                    LoanApplicationEntity entity = invocation.getArgument(0);
                    entity.setId(10L);
                    return entity;
                });

        LoanApplicationResponse response = loanApplicationService.submit(request);

        verify(loanApplicationRepository).saveAndFlush(loanApplicationCaptor.capture());
        LoanApplicationEntity savedEntity = loanApplicationCaptor.getValue();

        assertThat(savedEntity.getStatus()).isEqualTo(LoanApplicationStatus.IN_REVIEW);
        assertThat(savedEntity.getRejectionReason()).isNull();
        assertThat(savedEntity.getFirstPaymentDate()).isEqualTo(LocalDate.of(2026, 4, 17));
        assertThat(savedEntity.getBirthDate()).isEqualTo(birthDate);
        assertThat(savedEntity.getCustomerAge()).isEqualTo(31);
        assertThat(savedEntity.getTotalAnnualInterestRate()).isEqualTo(new BigDecimal("2.235"));
        assertThat(savedEntity.getPaymentScheduleEntries()).hasSize(2);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(LoanApplicationStatus.IN_REVIEW);
        assertThat(response.rejectionReason()).isNull();
        assertThat(response.firstPaymentDate()).isEqualTo(LocalDate.of(2026, 4, 17));
        assertThat(response.paymentSchedule()).hasSize(2);
    }

    @Test
    void submitAutoRejectsCustomerOlderThanConfiguredLimit() {
        CreateLoanApplicationRequest request = validCreateRequest();
        LocalDate birthDate = LocalDate.of(1950, 4, 12);

        when(loanApplicationRepository.existsByPersonalCodeAndStatus("39504120016", LoanApplicationStatus.IN_REVIEW))
                .thenReturn(false);
        when(personalCodeSupport.extractBirthDate("39504120016")).thenReturn(birthDate);
        when(personalCodeSupport.calculateAge("39504120016", LocalDate.of(2026, 4, 17))).thenReturn(76);
        when(loanApplicationRepository.saveAndFlush(any(LoanApplicationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponse response = loanApplicationService.submit(request);

        verify(loanApplicationRepository).saveAndFlush(loanApplicationCaptor.capture());
        LoanApplicationEntity savedEntity = loanApplicationCaptor.getValue();

        assertThat(savedEntity.getStatus()).isEqualTo(LoanApplicationStatus.REJECTED);
        assertThat(savedEntity.getRejectionReason()).isEqualTo(RejectionReason.CUSTOMER_TOO_OLD);
        assertThat(savedEntity.getReviewedAt()).isNotNull();
        assertThat(savedEntity.getPaymentScheduleEntries()).isEmpty();

        verify(paymentScheduleCalculator, never()).generate(any(), any(), anyInt(), any(LocalDate.class));

        assertThat(response.status()).isEqualTo(LoanApplicationStatus.REJECTED);
        assertThat(response.rejectionReason()).isEqualTo(RejectionReason.CUSTOMER_TOO_OLD);
        assertThat(response.paymentSchedule()).isEmpty();
    }

    @Test
    void submitRejectsSecondActiveApplicationForSameCustomer() {
        when(loanApplicationRepository.existsByPersonalCodeAndStatus("39504120016", LoanApplicationStatus.IN_REVIEW))
                .thenReturn(true);

        assertThatThrownBy(() -> loanApplicationService.submit(validCreateRequest()))
                .isInstanceOf(ActiveLoanApplicationExistsException.class)
                .hasMessage("Customer already has an active loan application.");
    }

    @Test
    void approveMarksReviewableApplicationAsApproved() {
        LoanApplicationEntity entity = reviewableApplication();
        when(loanApplicationRepository.findDetailedById(11L)).thenReturn(Optional.of(entity));
        when(loanApplicationRepository.saveAndFlush(entity)).thenReturn(entity);

        LoanApplicationResponse response = loanApplicationService.approve(11L);

        assertThat(entity.getStatus()).isEqualTo(LoanApplicationStatus.APPROVED);
        assertThat(entity.getReviewedAt()).isNotNull();
        assertThat(response.status()).isEqualTo(LoanApplicationStatus.APPROVED);
    }

    @Test
    void rejectFailsWhenApplicationAlreadyFinished() {
        LoanApplicationEntity entity = reviewableApplication();
        entity.setStatus(LoanApplicationStatus.APPROVED);
        when(loanApplicationRepository.findDetailedById(11L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> loanApplicationService.reject(11L, RejectionReason.FAILED_CREDIT_POLICY))
                .isInstanceOf(LoanApplicationNotReviewableException.class)
                .hasMessage("Only applications in IN_REVIEW status can be reviewed.");
    }

    @Test
    void getByIdFailsForMissingApplication() {
        when(loanApplicationRepository.findDetailedById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanApplicationService.getById(99L))
                .isInstanceOf(LoanApplicationNotFoundException.class)
                .hasMessage("Loan application with id 99 was not found.");
    }

    private CreateLoanApplicationRequest validCreateRequest() {
        return new CreateLoanApplicationRequest(
                "Mari",
                "Tamm",
                "39504120016",
                12,
                new BigDecimal("1.001"),
                new BigDecimal("1.234"),
                new BigDecimal("10000.00")
        );
    }

    private LoanApplicationEntity reviewableApplication() {
        LoanApplicationEntity entity = new LoanApplicationEntity();
        entity.setId(11L);
        entity.setFirstName("Mari");
        entity.setLastName("Tamm");
        entity.setPersonalCode("39504120016");
        entity.setBirthDate(LocalDate.of(1995, 4, 12));
        entity.setCustomerAge(31);
        entity.setLoanTermMonths(12);
        entity.setInterestMargin(new BigDecimal("1.001"));
        entity.setBaseInterestRate(new BigDecimal("1.234"));
        entity.setTotalAnnualInterestRate(new BigDecimal("2.235"));
        entity.setLoanAmount(new BigDecimal("10000.00"));
        entity.setFirstPaymentDate(LocalDate.of(2026, 4, 17));
        entity.setStatus(LoanApplicationStatus.IN_REVIEW);
        entity.replacePaymentSchedule(List.of(
                scheduleEntry(1, LocalDate.of(2026, 4, 17), "10000.00", "820.89", "18.63", "839.52", "9179.11")
        ));
        return entity;
    }

    private PaymentScheduleEntryEntity scheduleEntry(int installmentNumber,
                                                     LocalDate dueDate,
                                                     String openingBalance,
                                                     String principalPayment,
                                                     String interestPayment,
                                                     String totalPayment,
                                                     String closingBalance) {
        PaymentScheduleEntryEntity entity = new PaymentScheduleEntryEntity();
        entity.setInstallmentNumber(installmentNumber);
        entity.setDueDate(dueDate);
        entity.setOpeningBalance(new BigDecimal(openingBalance));
        entity.setPrincipalPayment(new BigDecimal(principalPayment));
        entity.setInterestPayment(new BigDecimal(interestPayment));
        entity.setTotalPayment(new BigDecimal(totalPayment));
        entity.setClosingBalance(new BigDecimal(closingBalance));
        return entity;
    }
}
