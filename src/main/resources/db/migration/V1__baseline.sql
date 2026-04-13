CREATE TABLE IF NOT EXISTS flyway_bootstrap (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note VARCHAR(255) NOT NULL
);

CREATE TABLE loan_applications (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(32) NOT NULL,
    last_name VARCHAR(32) NOT NULL,
    personal_code VARCHAR(11) NOT NULL,
    birth_date DATE NOT NULL,
    customer_age INTEGER NOT NULL,
    loan_term_months INTEGER NOT NULL CHECK (loan_term_months BETWEEN 6 AND 360),
    interest_margin NUMERIC(8, 3) NOT NULL CHECK (interest_margin >= 0),
    base_interest_rate NUMERIC(8, 3) NOT NULL,
    total_annual_interest_rate NUMERIC(8, 3) NOT NULL,
    loan_amount NUMERIC(15, 2) NOT NULL CHECK (loan_amount >= 5000),
    first_payment_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    rejection_reason VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_loan_applications_personal_code ON loan_applications(personal_code);
CREATE INDEX idx_loan_applications_status ON loan_applications(status);