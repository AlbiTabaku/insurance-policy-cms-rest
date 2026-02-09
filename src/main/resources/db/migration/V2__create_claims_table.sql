-- create_claims_table.sql
-- Create claims table

CREATE TABLE claims (
    id BIGSERIAL PRIMARY KEY,
    claim_number VARCHAR(40) UNIQUE NOT NULL,
    policy_id BIGINT NOT NULL,
    description VARCHAR(500) NOT NULL,
    claim_amount DECIMAL(15, 2) NOT NULL CHECK (claim_amount > 0),
    incident_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_claim_policy FOREIGN KEY (policy_id) REFERENCES policies(id) ON DELETE RESTRICT
);

-- Create indexes
CREATE INDEX idx_claim_number ON claims(claim_number);
CREATE INDEX idx_policy_id ON claims(policy_id);
CREATE INDEX idx_claim_status ON claims(status);

-- Add comments
COMMENT ON TABLE claims IS 'Insurance claims submitted against policies';
COMMENT ON COLUMN claims.claim_number IS 'Unique claim identifier (CLM-YYYY-XXXXXX)';
COMMENT ON COLUMN claims.status IS 'Claim status: SUBMITTED, APPROVED, REJECTED';
