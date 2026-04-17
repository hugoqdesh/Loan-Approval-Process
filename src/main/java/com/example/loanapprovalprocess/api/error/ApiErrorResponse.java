package com.example.loanapprovalprocess.api.error;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        List<ApiValidationError> details
) {
}
