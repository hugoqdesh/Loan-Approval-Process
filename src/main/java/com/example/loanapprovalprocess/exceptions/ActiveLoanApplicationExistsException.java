package com.example.loanapprovalprocess.exceptions;

import org.springframework.http.HttpStatus;

public class ActiveLoanApplicationExistsException extends BusinessException {

    public ActiveLoanApplicationExistsException() {
        super(HttpStatus.CONFLICT,
                "ACTIVE_LOAN_APPLICATION_EXISTS",
                "Customer already has an active loan application.");
    }
}
