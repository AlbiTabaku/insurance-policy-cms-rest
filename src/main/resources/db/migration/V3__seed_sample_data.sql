-- seed_sample_data.sql
-- Insert sample policies

INSERT INTO policies (policy_number, customer_name, customer_email, policy_type, coverage_amount, premium_amount, start_date, end_date, status, created_at, updated_at) VALUES
('POL-2024-100001', 'John Doe', 'john.doe@email.com', 'HEALTH', 100000.00, 5000.00, '2024-01-01', '2025-01-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('POL-2024-100002', 'Jane Smith', 'jane.smith@email.com', 'AUTO', 50000.00, 2500.00, '2024-02-01', '2025-02-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('POL-2024-100003', 'Bob Johnson', 'bob.johnson@email.com', 'HOME', 300000.00, 12000.00, '2024-03-01', '2025-03-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('POL-2023-100004', 'Alice Williams', 'alice.williams@email.com', 'LIFE', 500000.00, 15000.00, '2023-01-01', '2024-01-01', 'EXPIRED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('POL-2024-100005', 'Charlie Brown', 'charlie.brown@email.com', 'HEALTH', 75000.00, 3750.00, '2024-04-01', '2024-10-01', 'CANCELLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample claims
INSERT INTO claims (claim_number, policy_id, description, claim_amount, incident_date, status, rejection_reason, created_at, updated_at) VALUES
('CLM-2024-200001', 1, 'Medical treatment for broken arm', 8000.00, '2024-06-15', 'APPROVED', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CLM-2024-200002', 2, 'Car accident repair', 15000.00, '2024-07-20', 'SUBMITTED', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CLM-2024-200003', 3, 'Water damage from burst pipe', 25000.00, '2024-08-10', 'APPROVED', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CLM-2024-200004', 1, 'Dental surgery', 3500.00, '2024-09-05', 'REJECTED', 'Not covered under policy terms', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
