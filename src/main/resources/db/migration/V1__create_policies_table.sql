-- create_policies_table.sql
-- Create policies table

CREATE TABLE policies (
    id BIGSERIAL PRIMARY KEY,
    policy_number VARCHAR(20) UNIQUE NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(100) NOT NULL,
    policy_type VARCHAR(20) NOT NULL,
    coverage_amount DECIMAL(15, 2) NOT NULL CHECK (coverage_amount > 0),
    premium_amount DECIMAL(15, 2) NOT NULL CHECK (premium_amount > 0),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_dates CHECK (end_date > start_date),
    CONSTRAINT chk_amounts CHECK (coverage_amount > premium_amount)
);

-- Create indexes
CREATE INDEX idx_policy_number ON policies(policy_number);
CREATE INDEX idx_customer_email ON policies(customer_email);
CREATE INDEX idx_status ON policies(status);

-- Add comments
COMMENT ON TABLE policies IS 'Insurance policies with customer information';
COMMENT ON COLUMN policies.policy_number IS 'Unique policy identifier (POL-YYYY-XXXXXX)';
COMMENT ON COLUMN policies.status IS 'Policy status: ACTIVE, EXPIRED, CANCELLED';
