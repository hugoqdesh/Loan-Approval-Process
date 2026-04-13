package com.example.loanapprovalprocess.api.dto;

import com.example.loanapprovalprocess.enums.RejectionReason;
import jakarta.validation.constraints.NotNull;

public record RejectLoanApplicationRequest(
        @NotNull RejectionReason rejectionReason
) {
}
