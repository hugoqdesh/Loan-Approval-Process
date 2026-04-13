package com.example.loanapprovalprocess.api.dto;

import com.example.loanapprovalprocess.enums.LoanApplicationStatus;
import com.example.loanapprovalprocess.enums.RejectionReason;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record LoanApplicationResponse(
        Long id,
        String firstName,
        String lastName,
        String personalCode,
        LocalDate birthDate,
        Integer customerAge,
        Integer loanTermMonths,
        BigDecimal interestMargin,
        BigDecimal baseInterestRate,
        BigDecimal totalAnnualInterestRate,
        BigDecimal loanAmount,
        LocalDate firstPaymentDate,
        LoanApplicationStatus status,
        RejectionReason rejectionReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime reviewedAt,
        List<PaymentScheduleEntryResponse> paymentSchedule
) {
}
