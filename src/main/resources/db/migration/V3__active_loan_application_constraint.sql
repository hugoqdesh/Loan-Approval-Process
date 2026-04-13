    ALTER TABLE loan_applications
    ADD COLUMN active_personal_code VARCHAR(11);

UPDATE loan_applications
SET active_personal_code = CASE
    WHEN status = 'IN_REVIEW' THEN personal_code
    ELSE NULL
END;

CREATE UNIQUE INDEX uq_loan_applications_active_personal_code
    ON loan_applications(active_personal_code);
