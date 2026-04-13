CREATE TABLE payment_schedule_entries (
    id BIGSERIAL PRIMARY KEY,
    loan_application_id BIGINT NOT NULL,
    installment_number INTEGER NOT NULL,
    due_date DATE NOT NULL,
    opening_balance NUMERIC(15, 2) NOT NULL,
    principal_payment NUMERIC(15, 2) NOT NULL,
    interest_payment NUMERIC(15, 2) NOT NULL,
    total_payment NUMERIC(15, 2) NOT NULL,
    closing_balance NUMERIC(15, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_schedule_entries_loan_application
        FOREIGN KEY (loan_application_id) REFERENCES loan_applications(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_payment_schedule_entries_installment
    ON payment_schedule_entries (loan_application_id, installment_number);

CREATE INDEX idx_payment_schedule_entries_application
    ON payment_schedule_entries (loan_application_id);
