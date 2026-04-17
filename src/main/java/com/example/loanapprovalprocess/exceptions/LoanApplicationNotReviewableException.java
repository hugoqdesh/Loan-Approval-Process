package com.example.loanapprovalprocess.exceptions;

import org.springframework.http.HttpStatus;

public class LoanApplicationNotReviewableException extends BusinessException {

    public LoanApplicationNotReviewableException() {
        super(HttpStatus.CONFLICT,
                "LOAN_APPLICATION_NOT_IN_REVIEW",
                "Only applications in IN_REVIEW status can be reviewed.");
    }
}
