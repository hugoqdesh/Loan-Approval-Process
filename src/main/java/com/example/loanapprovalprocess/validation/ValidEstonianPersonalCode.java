package com.example.loanapprovalprocess.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Retention(RUNTIME)
@Constraint(validatedBy = EstonianPersonalCodeValidator.class)
public @interface ValidEstonianPersonalCode {

    String message() default "must be a valid Estonian personal code";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
