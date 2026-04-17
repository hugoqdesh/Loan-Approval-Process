package com.example.loanapprovalprocess.api.error;

public record ApiValidationError(
        String field,
        String message
) {
}
