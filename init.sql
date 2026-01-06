-- =====================================================
-- Insurance Application - Database Initialization
-- =====================================================

-- Create all required databases
CREATE DATABASE IF NOT EXISTS insurance_identity_db;
CREATE DATABASE IF NOT EXISTS insurance_policy_db;
CREATE DATABASE IF NOT EXISTS insurance_claim_db;
CREATE DATABASE IF NOT EXISTS insurance_provider_db;
CREATE DATABASE IF NOT EXISTS insurance_billing_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON insurance_identity_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON insurance_policy_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON insurance_claim_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON insurance_provider_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON insurance_billing_db.* TO 'root'@'%';

FLUSH PRIVILEGES;
