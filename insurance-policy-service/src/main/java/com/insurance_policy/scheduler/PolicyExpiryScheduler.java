package com.insurance_policy.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.insurance_policy.dto.NotificationEvent;
import com.insurance_policy.model.Policy;
import com.insurance_policy.model.PolicyStatus;
import com.insurance_policy.repository.PolicyRepository;

/**
 * Scheduler to handle policy expiry and auto-renewal reminders.
 * Runs daily at midnight to:
 * 1. Mark expired policies as EXPIRED
 * 2. Send automated reminders for policies expiring soon
 */
@Component
public class PolicyExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PolicyExpiryScheduler.class);

    private final PolicyRepository policyRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public PolicyExpiryScheduler(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    /**
     * Runs daily at midnight to mark expired policies
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void markExpiredPolicies() {
        logger.info("Running scheduled task: Mark expired policies");
        
        LocalDate today = LocalDate.now();
        List<Policy> expiredPolicies = policyRepository.findByEndDateBeforeAndStatus(today, PolicyStatus.ACTIVE);
        
        for (Policy policy : expiredPolicies) {
            policy.setStatus(PolicyStatus.EXPIRED);
            policyRepository.save(policy);
            
            // Send expiry notification
            String message = buildExpiryNotificationEmail(policy);
            NotificationEvent event = new NotificationEvent(
                policy.getUserId(),
                "Policy Expired - RestO'Sure",
                message
            );
            kafkaTemplate.send("notification_topic", event);
            
            logger.info("Policy {} marked as EXPIRED for user {}", policy.getId(), policy.getUserId());
        }
        
        logger.info("Completed marking {} policies as expired", expiredPolicies.size());
    }

    /**
     * Runs daily at 9 AM to send reminders for policies expiring in 30, 15, 7, and 1 day(s)
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendExpiryReminders() {
        logger.info("Running scheduled task: Send expiry reminders");
        
        int[] reminderDays = {30, 15, 7, 1};
        
        for (int days : reminderDays) {
            LocalDate targetDate = LocalDate.now().plusDays(days);
            List<Policy> expiringPolicies = policyRepository.findByEndDateAndStatus(targetDate, PolicyStatus.ACTIVE);
            
            for (Policy policy : expiringPolicies) {
                String message = buildReminderEmail(policy, days);
                NotificationEvent event = new NotificationEvent(
                    policy.getUserId(),
                    "Policy Expiring in " + days + " Day(s) - RestO'Sure",
                    message
                );
                kafkaTemplate.send("notification_topic", event);
                
                logger.info("Sent {}-day reminder for policy {} to user {}", days, policy.getId(), policy.getUserId());
            }
        }
    }
    
    private String buildExpiryNotificationEmail(Policy policy) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                    .header { background: linear-gradient(135deg, #1e3a5f 0%%, #2d5a87 100%%); padding: 30px; text-align: center; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 28px; }
                    .header p { color: #b8d4e8; margin: 5px 0 0 0; font-size: 14px; }
                    .content { padding: 40px 30px; }
                    .alert-box { background: linear-gradient(135deg, #dc3545 0%%, #c82333 100%%); border-radius: 10px; padding: 30px; text-align: center; margin-bottom: 30px; }
                    .alert-box h2 { color: #ffffff; margin: 0; font-size: 24px; }
                    .alert-box p { color: #f8d7da; margin: 10px 0 0 0; }
                    .warning-icon { font-size: 60px; margin-bottom: 15px; }
                    .info-box { background-color: #fff3cd; border-radius: 10px; padding: 20px; margin-bottom: 30px; border-left: 4px solid #ffc107; }
                    .info-box p { color: #856404; margin: 0; }
                    .renew-btn { display: block; background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); color: #ffffff; text-decoration: none; padding: 15px 30px; border-radius: 8px; text-align: center; font-size: 18px; font-weight: bold; margin: 20px 0; }
                    .footer { background-color: #1e3a5f; padding: 25px; text-align: center; }
                    .footer p { color: #b8d4e8; margin: 5px 0; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>RestO'Sure</h1>
                        <p>Smart Health Insurance</p>
                    </div>
                    <div class="content">
                        <div class="alert-box">
                            <div class="warning-icon">⚠️</div>
                            <h2>Your Policy Has Expired</h2>
                            <p>Policy ID: #%d</p>
                        </div>
                        <div class="info-box">
                            <p><strong>Important:</strong> Your health insurance coverage has ended. You are no longer covered for medical expenses under this policy.</p>
                        </div>
                        <p style="text-align: center; color: #6c757d;">To restore your coverage, please renew your policy or contact our support team.</p>
                        <a href="#" class="renew-btn">🔄 Renew Now</a>
                    </div>
                    <div class="footer">
                        <p>© 2024 RestO'Sure. All rights reserved.</p>
                        <p>For immediate assistance, call 1800-XXX-XXXX</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(policy.getId());
    }
    
    private String buildReminderEmail(Policy policy, int daysRemaining) {
        String urgencyColor = daysRemaining <= 7 ? "#dc3545" : "#ffc107";
        String urgencyText = daysRemaining <= 7 ? "Urgent Action Required!" : "Renewal Reminder";
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                    .header { background: linear-gradient(135deg, #1e3a5f 0%%, #2d5a87 100%%); padding: 30px; text-align: center; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 28px; }
                    .header p { color: #b8d4e8; margin: 5px 0 0 0; font-size: 14px; }
                    .content { padding: 40px 30px; }
                    .alert-box { background: %s; border-radius: 10px; padding: 25px; text-align: center; margin-bottom: 30px; }
                    .alert-box h2 { color: #ffffff; margin: 0 0 10px 0; font-size: 22px; }
                    .alert-box .days { color: #ffffff; font-size: 48px; font-weight: bold; }
                    .alert-box p { color: rgba(255,255,255,0.9); margin: 10px 0 0 0; }
                    .renew-btn { display: block; background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); color: #ffffff; text-decoration: none; padding: 15px 30px; border-radius: 8px; text-align: center; font-size: 18px; font-weight: bold; margin: 20px 0; }
                    .footer { background-color: #1e3a5f; padding: 25px; text-align: center; }
                    .footer p { color: #b8d4e8; margin: 5px 0; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>RestO'Sure</h1>
                        <p>Smart Health Insurance</p>
                    </div>
                    <div class="content">
                        <div class="alert-box">
                            <h2>%s</h2>
                            <div class="days">%d</div>
                            <p>day(s) until your policy expires</p>
                        </div>
                        <p style="text-align: center; color: #333;">Policy ID: <strong>#%d</strong></p>
                        <p style="text-align: center; color: #6c757d;">Don't let your coverage lapse! Renew now to continue enjoying uninterrupted health protection.</p>
                        <a href="#" class="renew-btn">🔄 Renew My Policy</a>
                    </div>
                    <div class="footer">
                        <p>© 2024 RestO'Sure. All rights reserved.</p>
                        <p>This is an automated reminder.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(urgencyColor, urgencyText, daysRemaining, policy.getId());
    }
}
