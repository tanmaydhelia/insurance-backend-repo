-- ============================================================================
-- SAMPLE DATA FOR POLICY RENEWAL FEATURE TESTING
-- Date Context: January 8, 2026
-- ============================================================================

USE policy_db;

-- ============================================================================
-- SCENARIO 1: Policy Expiring in 1 Day (Should trigger urgent reminder)
-- ============================================================================
INSERT INTO policy (policy_number, start_date, end_date, premium, remaining_sum_insured, status, user_id, agent_id, plan_id, created_at, updated_at, renewal_requested_at, last_renewal_attempt_at, last_renewal_status)
VALUES 
('POL-EXPIRY-1DAY', '2025-01-09', '2026-01-09', 12000.00, 500000.00, 'ACTIVE', 1, 1, 1, NOW(), NOW(), NULL, NULL, NULL);

-- ============================================================================
-- SCENARIO 2: Policy Expiring in 7 Days (Should trigger urgent reminder)
-- ============================================================================
INSERT INTO policy (policy_number, start_date, end_date, premium, remaining_sum_insured, status, user_id, agent_id, plan_id, created_at, updated_at, renewal_requested_at, last_renewal_attempt_at, last_renewal_status)
VALUES 
('POL-EXPIRY-7DAYS', '2025-01-15', '2026-01-15', 15000.00, 750000.00, 'ACTIVE', 2, 1, 2, NOW(), NOW(), NULL, NULL, NULL);

-- ============================================================================
-- SCENARIO 3: Policy Expiring in 15 Days (Should trigger normal reminder)
-- ============================================================================
INSERT INTO policy (policy_number, start_date, end_date, premium, remaining_sum_insured, status, user_id, agent_id, plan_id, created_at, updated_at, renewal_requested_at, last_renewal_attempt_at, last_renewal_status)
VALUES 
('POL-EXPIRY-15DAYS', '2025-01-23', '2026-01-23', 18000.00, 1000000.00, 'ACTIVE', 3, 2, 3, NOW(), NOW(), NULL, NULL, NULL);

-- ============================================================================
-- SCENARIO 4: Policy Expiring in 30 Days (Should trigger normal reminder)
-- ============================================================================
INSERT INTO policy (policy_number, start_date, end_date, premium, remaining_sum_insured, status, user_id, agent_id, plan_id, created_at, updated_at, renewal_requested_at, last_renewal_attempt_at, last_renewal_status)
VALUES 
('POL-EXPIRY-30DAYS', '2025-02-07', '2026-02-07', 20000.00, 1500000.00, 'ACTIVE', 4, 2, 4, NOW(), NOW(), NULL, NULL, NULL);

-- ============================================================================
-- SCENARIO 5: Policy Expiring in 45 Days (Renewable, no auto-reminder yet)
-- ============================================================================
INSERT INTO policy (policy_number, start_date, end_date, premium, remaining_sum_insured, status, user_id, agent_id, plan_id, created_at, updated_at, renewal_requested_at, last_renewal_attempt_at, last_renewal_status)
VALUES 
('POL-EXPIRY-45DAYS', '2025-02-22', '2026-02-22', 25000.00, 2000000.00, 'ACTIVE', 5, 3, 5, NOW(), NOW(), NULL, NULL, NULL);

-- ============================================================================
-- SCENARIO 6: Policy Expiring in 60 Days (Renewable, no auto-reminder yet)
-- ============================================================================
INSERT INTO policy (policy_number, start_date, end_date, premium, remaining_sum_insured, status, user_id, agent_id, plan_id, created_at, updated_at, renewal_requested_at, last_renewal_attempt_at, last_renewal_status)
VALUES 
('POL-EXPIRY-60DAYS', '2025-03-09', '2026-03-09', 22000.00, 1200000.00, 'ACTIVE', 6, 3, 6, NOW(), NOW(), NULL, NULL, NULL);

-- ============================================================================
-- SCENARIO 7: Policy Expiring in 90 Days (At the renewable threshold)
-- ============================================================================
INSERT INTO policy (policy_number, start_date, end_date, premium, remaining_sum_insured, status, user_id, agent_id, plan_id, created_at, updated_at, renewal_requested_at, last_renewal_attempt_at, last_renewal_status)
VALUES 
('POL-EXPIRY-90DAYS', '2025-04-08', '2026-04-08', 28000.00, 2500000.00, 'ACTIVE', 7, 4, 7, NOW(), NOW(), NULL, NULL, NULL);

-- ============================================================================
-- SCENARIO 8: Policy ALREADY EXPIRED Yesterday (Should be auto-marked EXPIRED)
-- ============================================================================
INSERT INTO policy (policy_number, start_date, end_date, premium, remaining_sum_insured, status, user_id, agent_id, plan_id, created_at, updated_at, renewal_requested_at, last_renewal_attempt_at, last_renewal_status)
VALUES 
('POL-EXPIRED-YESTERDAY', '2025-01-07', '2026-01-07', 15000.00, 300000.00, 'ACTIVE', 8, 4, 1, NOW(), NOW(), NULL, NULL, NULL);

-- ============================================================================
-- SCENARIO 9: Policy EXPIRED 7 Days Ago (Should be auto-marked EXPIRED)
-- ============================================================================
INSERT INTO policy (policy_number, start_date, end_date, premium, remaining_sum_insured, status, user_id, agent_id, plan_id, created_at, updated_at, renewal_requested_at, last_renewal_attempt_at, last_renewal_status)
VALUES 
('POL-EXPIRED-7DAYS-AGO', '2025-01-01', '2026-01-01', 12000.00, 250000.00, 'ACTIVE', 9, 5, 2, NOW(), NOW(), NULL, NULL, NULL);

-- ============================================================================
-- SCENARIO 10: Policy with Renewal Reminder Already Sent
-- ============================================================================
INSERT INTO policy (policy_number, start_date, end_date, premium, remaining_sum_insured, status, user_id, agent_id, plan_id, created_at, updated_at, renewal_requested_at, last_renewal_attempt_at, last_renewal_status)
VALUES 
('POL-REMINDER-SENT', '2025-01-18', '2026-01-18', 16000.00, 800000.00, 'ACTIVE', 10, 5, 3, NOW(), NOW(), '2026-01-03 10:30:00', NULL, NULL);

-- ============================================================================
-- SCENARIO 11: Policy with Pending Renewal (User initiated but not completed)
-- ============================================================================
INSERT INTO policy (policy_number, start_date, end_date, premium, remaining_sum_insured, status, user_id, agent_id, plan_id, created_at, updated_at, renewal_requested_at, last_renewal_attempt_at, last_renewal_status)
VALUES 
('POL-PENDING-RENEWAL', '2025-01-20', '2026-01-20', 19000.00, 950000.00, 'ACTIVE', 11, 6, 4, NOW(), NOW(), '2026-01-05 14:20:00', '2026-01-06 09:15:00', 'PENDING');

-- ============================================================================
-- SCENARIO 12: Successfully Renewed Policy (Extended dates, full coverage)
-- ============================================================================
INSERT INTO policy (policy_number, start_date, end_date, premium, remaining_sum_insured, status, user_id, agent_id, plan_id, created_at, updated_at, renewal_requested_at, last_renewal_attempt_at, last_renewal_status)
VALUES 
('POL-RENEWED-SUCCESS', '2026-01-02', '2027-01-02', 21000.00, 1500000.00, 'ACTIVE', 12, 6, 5, NOW(), NOW(), '2025-12-15 11:00:00', '2025-12-28 16:45:00', 'SUCCESS');

-- ============================================================================
-- SCENARIO 13: Failed Renewal - Policy Expired
-- ============================================================================
INSERT INTO policy (policy_number, start_date, end_date, premium, remaining_sum_insured, status, user_id, agent_id, plan_id, created_at, updated_at, renewal_requested_at, last_renewal_attempt_at, last_renewal_status)
VALUES 
('POL-RENEWAL-FAILED', '2024-12-25', '2025-12-25', 14000.00, 100000.00, 'EXPIRED', 13, 7, 6, NOW(), NOW(), '2025-12-10 09:00:00', '2025-12-22 14:30:00', 'FAILED');

-- ============================================================================
-- SCENARIO 14: Policy with Multiple Renewal Attempts
-- ============================================================================
INSERT INTO policy (policy_number, start_date, end_date, premium, remaining_sum_insured, status, user_id, agent_id, plan_id, created_at, updated_at, renewal_requested_at, last_renewal_attempt_at, last_renewal_status)
VALUES 
('POL-MULTIPLE-ATTEMPTS', '2025-01-25', '2026-01-25', 17000.00, 850000.00, 'ACTIVE', 14, 7, 7, NOW(), NOW(), '2025-12-28 08:00:00', '2026-01-07 18:20:00', 'PENDING');

-- ============================================================================
-- SCENARIO 15: Policy Not Yet Renewable (Expires in 120 days)
-- ============================================================================
INSERT INTO policy (policy_number, start_date, end_date, premium, remaining_sum_insured, status, user_id, agent_id, plan_id, created_at, updated_at, renewal_requested_at, last_renewal_attempt_at, last_renewal_status)
VALUES 
('POL-NOT-YET-RENEWABLE', '2025-05-08', '2026-05-08', 24000.00, 1800000.00, 'ACTIVE', 15, 8, 8, NOW(), NOW(), NULL, NULL, NULL);

-- ============================================================================
-- QUERY TO VIEW ALL RENEWAL SCENARIOS
-- ============================================================================
-- Run this to see all policies with days remaining calculation:

SELECT 
    p.id,
    p.policy_number,
    p.status,
    p.end_date,
    DATEDIFF(p.end_date, CURDATE()) AS days_remaining,
    CASE 
        WHEN p.status = 'ACTIVE' AND DATEDIFF(p.end_date, CURDATE()) <= 90 THEN 'YES'
        ELSE 'NO'
    END AS renewable,
    p.renewal_requested_at,
    p.last_renewal_attempt_at,
    p.last_renewal_status,
    CONCAT(ip.name, ' (₹', FORMAT(ip.coverage_amount, 0), ')') AS plan_details,
    p.remaining_sum_insured,
    u.username AS user_name,
    a.username AS agent_name
FROM policy p
JOIN insurance_plan ip ON p.plan_id = ip.id
LEFT JOIN identity_db.user u ON p.user_id = u.id
LEFT JOIN identity_db.user a ON p.agent_id = a.id
WHERE p.policy_number LIKE 'POL-%'
ORDER BY 
    CASE 
        WHEN DATEDIFF(p.end_date, CURDATE()) < 0 THEN 0
        ELSE DATEDIFF(p.end_date, CURDATE())
    END ASC;

-- ============================================================================
-- TEST ENDPOINTS AFTER ADDING DATA
-- ============================================================================

-- 1. GET EXPIRING POLICIES (All policies expiring in 30 days)
-- GET http://localhost:9000/policy/policies/expiring?days=30

-- 2. GET EXPIRING POLICIES BY AGENT (Agent ID 1's expiring policies)
-- GET http://localhost:9000/policy/policies/expiring/agent/1?days=30

-- 3. SEND RENEWAL REMINDER (Agent sends reminder for policy expiring in 1 day)
-- POST http://localhost:9000/policy/policies/{policyId}/renewal-reminder
-- Body: {"agentId": 1}

-- 4. INITIATE RENEWAL (User starts renewal process)
-- POST http://localhost:9000/policy/policies/{policyId}/renew

-- 5. CONFIRM RENEWAL SUCCESS
-- POST http://localhost:9000/policy/policies/{policyId}/renew/confirm
-- Body: {"razorpayOrderId": "order_123", "razorpayPaymentId": "pay_456", "success": true}

-- 6. CONFIRM RENEWAL FAILURE
-- POST http://localhost:9000/policy/policies/{policyId}/renew/confirm
-- Body: {"razorpayOrderId": "order_123", "razorpayPaymentId": "pay_456", "success": false}

-- ============================================================================
-- SCHEDULER BEHAVIOR
-- ============================================================================

-- MIDNIGHT SCHEDULER (00:00):
-- - Will mark POL-EXPIRED-YESTERDAY and POL-EXPIRED-7DAYS-AGO as EXPIRED
-- - Will send expiry notification emails

-- 9 AM SCHEDULER (09:00):
-- - Sends reminder for POL-EXPIRY-1DAY (1 day remaining)
-- - Sends reminder for POL-EXPIRY-7DAYS (7 days remaining)
-- - Sends reminder for POL-EXPIRY-15DAYS (15 days remaining)
-- - Sends reminder for POL-EXPIRY-30DAYS (30 days remaining)

-- ============================================================================
-- CLEANUP SCRIPT (Run to remove test data)
-- ============================================================================
-- DELETE FROM policy WHERE policy_number LIKE 'POL-%';
