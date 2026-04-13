package com.example.loanapprovalprocess.support;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class PersonalCodeSupport {

    private static final int[] FIRST_STAGE_WEIGHTS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 1};
    private static final int[] SECOND_STAGE_WEIGHTS = {3, 4, 5, 6, 7, 8, 9, 1, 2, 3};

    public boolean isValid(String personalCode) {
        if (personalCode == null || !personalCode.matches("\\d{11}")) {
            return false;
        }

        try {
            extractBirthDate(personalCode);
        } catch (RuntimeException exception) {
            return false;
        }

        return hasValidChecksum(personalCode);
    }

    public LocalDate extractBirthDate(String personalCode) {
        if (personalCode == null || personalCode.length() != 11) {
            throw new IllegalArgumentException("Personal code must be exactly 11 digits.");
        }

        int century = switch (personalCode.charAt(0)) {
            case '1', '2' -> 1800;
            case '3', '4' -> 1900;
            case '5', '6' -> 2000;
            case '7', '8' -> 2100;
            default -> throw new IllegalArgumentException("Unsupported Estonian personal code century digit.");
        };

        int year = century + Integer.parseInt(personalCode.substring(1, 3));
        int month = Integer.parseInt(personalCode.substring(3, 5));
        int day = Integer.parseInt(personalCode.substring(5, 7));
        return LocalDate.of(year, month, day);
    }

    public int calculateAge(String personalCode, LocalDate onDate) {
        return Period.between(extractBirthDate(personalCode), onDate).getYears();
    }

    private boolean hasValidChecksum(String personalCode) {
        int controlNumber = Character.digit(personalCode.charAt(10), 10);
        int calculated = checksumFor(personalCode, FIRST_STAGE_WEIGHTS);
        if (calculated == 10) {
            calculated = checksumFor(personalCode, SECOND_STAGE_WEIGHTS);
        }
        if (calculated == 10) {
            calculated = 0;
        }
        return calculated == controlNumber;
    }

    private int checksumFor(String personalCode, int[] weights) {
        int sum = 0;
        for (int index = 0; index < 10; index++) {
            sum += Character.digit(personalCode.charAt(index), 10) * weights[index];
        }
        return sum % 11;
    }
}
