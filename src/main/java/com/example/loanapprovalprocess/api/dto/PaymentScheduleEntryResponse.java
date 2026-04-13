package com.example.loanapprovalprocess.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentScheduleEntryResponse(
        Integer installmentNumber,
        LocalDate dueDate,
        BigDecimal openingBalance,
        BigDecimal principalPayment,
        BigDecimal interestPayment,
        BigDecimal totalPayment,
        BigDecimal closingBalance
) {
}
