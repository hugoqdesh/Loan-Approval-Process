package com.example.loanapprovalprocess;

import com.example.loanapprovalprocess.api.dto.CreateLoanApplicationRequest;
import com.example.loanapprovalprocess.entities.LoanApplicationEntity;
import com.example.loanapprovalprocess.enums.LoanApplicationStatus;
import com.example.loanapprovalprocess.repositories.LoanApplicationRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class LoanApplicationConstraintsTests {

    @Autowired
    private Validator validator;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Test
    void createRequestAllowsNegativeBaseInterestRate() {
        CreateLoanApplicationRequest request = new CreateLoanApplicationRequest(
                "Mari",
                "Tamm",
                validPersonalCode(LocalDate.of(1995, 4, 12), 1),
                120,
                new BigDecimal("1.500"),
                new BigDecimal("-0.125"),
                new BigDecimal("10000.00")
        );

        Set<ConstraintViolation<CreateLoanApplicationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .doesNotContain("baseInterestRate");
    }

    @Test
    void repositoryAllowsOnlyOneActiveApplicationPerPersonalCode() {
        loanApplicationRepository.saveAndFlush(inReviewApplication("39504120016"));

        assertThatThrownBy(() -> loanApplicationRepository.saveAndFlush(inReviewApplication("39504120016")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private LoanApplicationEntity inReviewApplication(String personalCode) {
        LoanApplicationEntity entity = new LoanApplicationEntity();
        entity.setFirstName("Mari");
        entity.setLastName("Tamm");
        entity.setPersonalCode(personalCode);
        entity.setBirthDate(LocalDate.of(1995, 4, 12));
        entity.setCustomerAge(30);
        entity.setLoanTermMonths(120);
        entity.setInterestMargin(new BigDecimal("1.500"));
        entity.setBaseInterestRate(new BigDecimal("-0.125"));
        entity.setTotalAnnualInterestRate(new BigDecimal("1.375"));
        entity.setLoanAmount(new BigDecimal("10000.00"));
        entity.setFirstPaymentDate(LocalDate.of(2026, 4, 11));
        entity.setStatus(LoanApplicationStatus.IN_REVIEW);
        return entity;
    }

    private String validPersonalCode(LocalDate birthDate, int sequence) {
        int firstDigit = birthDate.getYear() >= 2000 ? 5 : 3;
        String base = "%d%02d%02d%02d%03d".formatted(
                firstDigit,
                birthDate.getYear() % 100,
                birthDate.getMonthValue(),
                birthDate.getDayOfMonth(),
                sequence
        );
        return base + checksum(base);
    }

    private int checksum(String firstTenDigits) {
        int[] firstStageWeights = {1, 2, 3, 4, 5, 6, 7, 8, 9, 1};
        int[] secondStageWeights = {3, 4, 5, 6, 7, 8, 9, 1, 2, 3};

        int checksum = weightedModulo(firstTenDigits, firstStageWeights);
        if (checksum == 10) {
            checksum = weightedModulo(firstTenDigits, secondStageWeights);
        }
        return checksum == 10 ? 0 : checksum;
    }

    private int weightedModulo(String firstTenDigits, int[] weights) {
        int sum = 0;
        for (int index = 0; index < firstTenDigits.length(); index++) {
            sum += Character.digit(firstTenDigits.charAt(index), 10) * weights[index];
        }
        return sum % 11;
    }
}
