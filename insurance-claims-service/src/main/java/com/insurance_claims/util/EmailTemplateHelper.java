package com.insurance_claims.util;

public class EmailTemplateHelper {

    /**
     * Format claim submitted email
     */
    public static String formatClaimSubmittedEmail(String diagnosis, Integer claimId) {
        return """
            <h2 style="color: #667eea; margin-bottom: 20px;">Claim Submitted Successfully</h2>
            
            <p>Dear Valued Member,</p>
            
            <p>We have successfully received your insurance claim and our team is reviewing it.</p>
            
            <div style="background-color: #e7f3ff; padding: 20px; border-radius: 8px; margin: 20px 0;">
                <p style="margin: 0;"><strong>📋 Claim Details:</strong></p>
                <p style="margin: 10px 0 0 0;"><strong>Claim ID:</strong> #%d</p>
                <p style="margin: 5px 0 0 0;"><strong>Diagnosis:</strong> %s</p>
                <p style="margin: 5px 0 0 0;"><strong>Status:</strong> <span style="color: #ffc107;">Under Review</span></p>
            </div>
            
            <div style="background-color: #f8f9fa; padding: 20px; border-left: 4px solid #667eea; margin: 20px 0;">
                <h3 style="margin-top: 0; color: #495057;">What Happens Next?</h3>
                <ol style="line-height: 1.8; margin: 0;">
                    <li>Our claims officer will review your submission</li>
                    <li>We may contact you if additional documents are needed</li>
                    <li>You'll receive an update within 3-5 business days</li>
                    <li>Approved claims will be processed immediately</li>
                </ol>
            </div>
            
            <p><strong>Track Your Claim:</strong> Login to your dashboard to check real-time status updates.</p>
            
            <p style="margin-top: 30px;">
                <strong>Best regards,</strong><br>
                <span style="color: #667eea;">The RestO'Sure Claims Team</span>
            </p>
            """.formatted(claimId, diagnosis);
    }

    /**
     * Format claim approved email
     */
    public static String formatClaimApprovedEmail(Integer claimId, Double approvedAmount, String processedBy) {
        return """
            <h2 style="color: #28a745; margin-bottom: 20px;">🎉 Claim Approved!</h2>
            
            <p>Dear Valued Member,</p>
            
            <p>Congratulations! Your insurance claim has been approved.</p>
            
            <div style="background-color: #d4edda; padding: 20px; border-radius: 8px; margin: 20px 0;">
                <p style="margin: 0;"><strong>✅ Approval Details:</strong></p>
                <p style="margin: 10px 0 0 0;"><strong>Claim ID:</strong> #%d</p>
                <p style="margin: 5px 0 0 0;"><strong>Approved Amount:</strong> <span style="color: #28a745; font-size: 18px;">₹%,.2f</span></p>
                <p style="margin: 5px 0 0 0;"><strong>Processed By:</strong> %s</p>
            </div>
            
            <div style="background-color: #f8f9fa; padding: 20px; border-left: 4px solid #28a745; margin: 20px 0;">
                <h3 style="margin-top: 0; color: #495057;">Payment Processing</h3>
                <p style="margin: 0;">Your approved amount will be processed within <strong>2-3 business days</strong>.</p>
                <p style="margin: 10px 0 0 0;">You will receive a separate notification once the payment is initiated.</p>
            </div>
            
            <p>Thank you for choosing RestO'Sure. We're here to protect what matters most to you.</p>
            
            <p style="margin-top: 30px;">
                <strong>Best regards,</strong><br>
                <span style="color: #667eea;">The RestO'Sure Claims Team</span>
            </p>
            """.formatted(claimId, approvedAmount, processedBy);
    }

    /**
     * Format claim rejected email
     */
    public static String formatClaimRejectedEmail(Integer claimId, String rejectionReason, String processedBy) {
        return """
            <h2 style="color: #dc3545; margin-bottom: 20px;">Claim Decision Update</h2>
            
            <p>Dear Valued Member,</p>
            
            <p>After careful review, we regret to inform you that your claim could not be approved at this time.</p>
            
            <div style="background-color: #f8d7da; padding: 20px; border-radius: 8px; margin: 20px 0;">
                <p style="margin: 0;"><strong>❌ Claim Details:</strong></p>
                <p style="margin: 10px 0 0 0;"><strong>Claim ID:</strong> #%d</p>
                <p style="margin: 5px 0 0 0;"><strong>Status:</strong> <span style="color: #dc3545;">Rejected</span></p>
                <p style="margin: 5px 0 0 0;"><strong>Reviewed By:</strong> %s</p>
            </div>
            
            <div style="background-color: #fff3cd; padding: 20px; border-left: 4px solid #ffc107; margin: 20px 0;">
                <p style="margin: 0;"><strong>📝 Reason for Rejection:</strong></p>
                <p style="margin: 10px 0 0 0;">%s</p>
            </div>
            
            <div style="background-color: #e7f3ff; padding: 20px; border-radius: 8px; margin: 20px 0;">
                <h3 style="margin-top: 0; color: #495057;">What You Can Do</h3>
                <ul style="line-height: 1.8; margin: 0;">
                    <li>Review your policy document for coverage details</li>
                    <li>Provide additional documentation if available</li>
                    <li>Contact our support team for clarification</li>
                    <li>File an appeal if you believe this decision is incorrect</li>
                </ul>
            </div>
            
            <p><strong>Need Help?</strong> Our support team is available to discuss this decision and guide you through next steps.</p>
            
            <div style="background-color: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0;">
                <p style="margin: 0;"><strong>📞 Claims Support:</strong> +91-1800-XXX-XXXX</p>
                <p style="margin: 5px 0 0 0;"><strong>📧 Email:</strong> claims@restosure.com</p>
            </div>
            
            <p style="margin-top: 30px;">
                <strong>Sincerely,</strong><br>
                <span style="color: #667eea;">The RestO'Sure Claims Team</span>
            </p>
            """.formatted(claimId, processedBy, rejectionReason);
    }
}
