package com.example.loanapprovalprocess.exceptions;

import org.springframework.http.HttpStatus;

public class LoanApplicationNotFoundException extends BusinessException {

    public LoanApplicationNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND,
                "LOAN_APPLICATION_NOT_FOUND",
                "Loan application with id %d was not found.".formatted(id));
    }
}
