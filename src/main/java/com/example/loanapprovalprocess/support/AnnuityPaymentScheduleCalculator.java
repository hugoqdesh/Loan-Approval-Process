package com.example.loanapprovalprocess.support;

import com.example.loanapprovalprocess.entities.PaymentScheduleEntryEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class AnnuityPaymentScheduleCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12);
    private static final MathContext MATH_CONTEXT = new MathContext(20, RoundingMode.HALF_UP);

    public List<PaymentScheduleEntryEntity> generate(BigDecimal principal,
                                                     BigDecimal annualInterestRate,
                                                     int termMonths,
                                                     LocalDate firstPaymentDate) {
        BigDecimal monthlyRate = annualInterestRate
                .divide(HUNDRED, 10, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR, 10, RoundingMode.HALF_UP);
        BigDecimal standardPayment = calculateMonthlyPayment(principal, monthlyRate, termMonths);

        List<PaymentScheduleEntryEntity> schedule = new ArrayList<>(termMonths);
        BigDecimal remainingBalance = principal.setScale(2, RoundingMode.HALF_UP);

        for (int installment = 1; installment <= termMonths; installment++) {
            BigDecimal openingBalance = remainingBalance;
            BigDecimal interestPayment = openingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalPayment = standardPayment;
            BigDecimal principalPayment = totalPayment.subtract(interestPayment).setScale(2, RoundingMode.HALF_UP);

            if (installment == termMonths) {
                principalPayment = openingBalance;
                totalPayment = principalPayment.add(interestPayment).setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal closingBalance = openingBalance.subtract(principalPayment).setScale(2, RoundingMode.HALF_UP);
            if (closingBalance.signum() < 0) {
                closingBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }

            PaymentScheduleEntryEntity entry = new PaymentScheduleEntryEntity();
            entry.setInstallmentNumber(installment);
            entry.setDueDate(firstPaymentDate.plusMonths(installment - 1L));
            entry.setOpeningBalance(openingBalance);
            entry.setPrincipalPayment(principalPayment);
            entry.setInterestPayment(interestPayment);
            entry.setTotalPayment(totalPayment);
            entry.setClosingBalance(closingBalance);
            schedule.add(entry);

            remainingBalance = closingBalance;
        }

        return schedule;
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal monthlyRate, int termMonths) {
        if (monthlyRate.signum() == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate, MATH_CONTEXT);
        BigDecimal factor = onePlusRate.pow(termMonths, MATH_CONTEXT);
        return principal.multiply(monthlyRate, MATH_CONTEXT)
                .multiply(factor, MATH_CONTEXT)
                .divide(factor.subtract(BigDecimal.ONE, MATH_CONTEXT), 2, RoundingMode.HALF_UP);
    }
}
