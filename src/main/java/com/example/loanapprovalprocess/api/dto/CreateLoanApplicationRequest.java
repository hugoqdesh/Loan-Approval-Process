package com.example.loanapprovalprocess.api.dto;

import com.example.loanapprovalprocess.validation.ValidEstonianPersonalCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateLoanApplicationRequest(
        @NotBlank @Size(max = 32) String firstName,
        @NotBlank @Size(max = 32) String lastName,
        @NotBlank @Size(min = 11, max = 11) @ValidEstonianPersonalCode String personalCode,
        @NotNull @Min(6) @Max(360) Integer loanTermMonths,
        @NotNull @DecimalMin(value = "0.000") @Digits(integer = 5, fraction = 3) BigDecimal interestMargin,
        @NotNull @Digits(integer = 5, fraction = 3) BigDecimal baseInterestRate,
        @NotNull @DecimalMin(value = "5000.00") @Digits(integer = 13, fraction = 2) BigDecimal loanAmount
) {
}
