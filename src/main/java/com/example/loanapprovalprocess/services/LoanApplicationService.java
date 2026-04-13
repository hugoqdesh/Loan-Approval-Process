package com.example.loanapprovalprocess.services;

import com.example.loanapprovalprocess.api.dto.CreateLoanApplicationRequest;
import com.example.loanapprovalprocess.api.dto.LoanApplicationResponse;
import com.example.loanapprovalprocess.api.dto.PaymentScheduleEntryResponse;
import com.example.loanapprovalprocess.entities.LoanApplicationEntity;
import com.example.loanapprovalprocess.entities.PaymentScheduleEntryEntity;
import com.example.loanapprovalprocess.enums.LoanApplicationStatus;
import com.example.loanapprovalprocess.enums.RejectionReason;
import com.example.loanapprovalprocess.repositories.LoanApplicationRepository;
import com.example.loanapprovalprocess.support.AnnuityPaymentScheduleCalculator;
import com.example.loanapprovalprocess.support.PersonalCodeSupport;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class LoanApplicationService {

    private static final int MONEY_SCALE = 2;
    private static final int PERCENTAGE_SCALE = 3;
    private static final String ACTIVE_APPLICATION_CONSTRAINT_NAME = "uq_loan_applications_active_personal_code";

    private final LoanApplicationRepository loanApplicationRepository;
    private final PersonalCodeSupport personalCodeSupport;
    private final AnnuityPaymentScheduleCalculator paymentScheduleCalculator;
    private final Clock clock;
    private final int maxCustomerAge;

    public LoanApplicationService(
            LoanApplicationRepository loanApplicationRepository,
            PersonalCodeSupport personalCodeSupport,
            AnnuityPaymentScheduleCalculator paymentScheduleCalculator,
            Clock clock,
            @Value("${loan.customer.max-age:70}") int maxCustomerAge
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.personalCodeSupport = personalCodeSupport;
        this.paymentScheduleCalculator = paymentScheduleCalculator;
        this.clock = clock;
        this.maxCustomerAge = maxCustomerAge;
    }

    public LoanApplicationResponse submit(CreateLoanApplicationRequest request) {
        String personalCode = request.personalCode().trim();
        if (loanApplicationRepository.existsByPersonalCodeAndStatus(personalCode, LoanApplicationStatus.IN_REVIEW)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer already has an active loan application.");
        }

        LocalDate today = LocalDate.now(clock);
        LocalDate birthDate = personalCodeSupport.extractBirthDate(personalCode);
        int customerAge = personalCodeSupport.calculateAge(personalCode, today);

        LoanApplicationEntity entity = new LoanApplicationEntity();
        entity.setFirstName(request.firstName().trim());
        entity.setLastName(request.lastName().trim());
        entity.setPersonalCode(personalCode);
        entity.setBirthDate(birthDate);
        entity.setCustomerAge(customerAge);
        entity.setLoanTermMonths(request.loanTermMonths());
        entity.setInterestMargin(scalePercentage(request.interestMargin()));
        entity.setBaseInterestRate(scalePercentage(request.baseInterestRate()));
        entity.setTotalAnnualInterestRate(scalePercentage(request.interestMargin().add(request.baseInterestRate())));
        entity.setLoanAmount(scaleMoney(request.loanAmount()));
        entity.setFirstPaymentDate(today);

        if (customerAge > maxCustomerAge) {
            entity.setStatus(LoanApplicationStatus.REJECTED);
            entity.setRejectionReason(RejectionReason.CUSTOMER_TOO_OLD);
            entity.setReviewedAt(OffsetDateTime.now(clock));
        } else {
            entity.replacePaymentSchedule(paymentScheduleCalculator.generate(
                    entity.getLoanAmount(),
                    entity.getTotalAnnualInterestRate(),
                    entity.getLoanTermMonths(),
                    entity.getFirstPaymentDate()
            ));
            entity.setStatus(LoanApplicationStatus.IN_REVIEW);
        }

        try {
            return toResponse(loanApplicationRepository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            if (isActiveApplicationConstraintViolation(exception)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Customer already has an active loan application.", exception);
            }
            throw exception;
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public LoanApplicationResponse getById(Long id) {
        return toResponse(findDetailedById(id));
    }

    public LoanApplicationResponse approve(Long id) {
        LoanApplicationEntity entity = findReviewableById(id);
        entity.setStatus(LoanApplicationStatus.APPROVED);
        entity.setRejectionReason(null);
        entity.setReviewedAt(OffsetDateTime.now(clock));
        return toResponse(loanApplicationRepository.saveAndFlush(entity));
    }

    public LoanApplicationResponse reject(Long id, RejectionReason rejectionReason) {
        LoanApplicationEntity entity = findReviewableById(id);
        entity.setStatus(LoanApplicationStatus.REJECTED);
        entity.setRejectionReason(rejectionReason);
        entity.setReviewedAt(OffsetDateTime.now(clock));
        return toResponse(loanApplicationRepository.saveAndFlush(entity));
    }

    private LoanApplicationEntity findDetailedById(Long id) {
        return loanApplicationRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Loan application with id %d was not found.".formatted(id)));
    }

    private LoanApplicationEntity findReviewableById(Long id) {
        LoanApplicationEntity entity = findDetailedById(id);
        if (entity.getStatus() != LoanApplicationStatus.IN_REVIEW) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only applications in IN_REVIEW status can be reviewed.");
        }
        return entity;
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal scalePercentage(BigDecimal value) {
        return value.setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }

    private boolean isActiveApplicationConstraintViolation(DataIntegrityViolationException exception) {
        Throwable current = exception;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains(ACTIVE_APPLICATION_CONSTRAINT_NAME)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private LoanApplicationResponse toResponse(LoanApplicationEntity entity) {
        List<PaymentScheduleEntryResponse> schedule = entity.getPaymentScheduleEntries().stream()
                .map(this::toScheduleResponse)
                .toList();

        return new LoanApplicationResponse(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPersonalCode(),
                entity.getBirthDate(),
                entity.getCustomerAge(),
                entity.getLoanTermMonths(),
                entity.getInterestMargin(),
                entity.getBaseInterestRate(),
                entity.getTotalAnnualInterestRate(),
                entity.getLoanAmount(),
                entity.getFirstPaymentDate(),
                entity.getStatus(),
                entity.getRejectionReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getReviewedAt(),
                schedule
        );
    }

    private PaymentScheduleEntryResponse toScheduleResponse(PaymentScheduleEntryEntity entity) {
        return new PaymentScheduleEntryResponse(
                entity.getInstallmentNumber(),
                entity.getDueDate(),
                entity.getOpeningBalance(),
                entity.getPrincipalPayment(),
                entity.getInterestPayment(),
                entity.getTotalPayment(),
                entity.getClosingBalance()
        );
    }
}
