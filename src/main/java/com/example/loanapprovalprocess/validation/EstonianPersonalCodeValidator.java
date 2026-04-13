package com.example.loanapprovalprocess.validation;

import com.example.loanapprovalprocess.support.PersonalCodeSupport;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EstonianPersonalCodeValidator implements ConstraintValidator<ValidEstonianPersonalCode, String> {

    private final PersonalCodeSupport personalCodeSupport;

    public EstonianPersonalCodeValidator(PersonalCodeSupport personalCodeSupport) {
        this.personalCodeSupport = personalCodeSupport;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || personalCodeSupport.isValid(value);
    }
}
