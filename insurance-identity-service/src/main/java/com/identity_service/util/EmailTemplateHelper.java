package com.identity_service.util;

public class EmailTemplateHelper {

    /**
     * Format welcome email with HTML
     */
    public static String formatWelcomeEmail(String userName) {
        return """
            <h2 style="color: #667eea; margin-bottom: 20px;">Welcome to RestO'Sure! 🎉</h2>
            
            <p>Dear <strong>%s</strong>,</p>
            
            <p>We are thrilled to welcome you to the RestO'Sure family! Your account has been successfully created.</p>
            
            <div style="background-color: #f8f9fa; padding: 20px; border-left: 4px solid #667eea; margin: 20px 0;">
                <h3 style="margin-top: 0; color: #495057;">What's Next?</h3>
                <ul style="line-height: 1.8;">
                    <li>🏥 Browse our comprehensive insurance plans</li>
                    <li>📝 Complete your profile and upload required documents</li>
                    <li>💳 Choose a plan that fits your needs</li>
                    <li>🛡️ Get instant coverage and peace of mind</li>
                </ul>
            </div>
            
            <p>Our team is here to help you every step of the way. If you have any questions, feel free to reach out to our support team.</p>
            
            <p style="margin-top: 30px;">
                <strong>Best regards,</strong><br>
                <span style="color: #667eea;">The RestO'Sure Team</span>
            </p>
            """.formatted(userName);
    }

    /**
     * Format account created by admin email
     */
    public static String formatAccountCreatedEmail(String userName, String roleName) {
        return """
            <h2 style="color: #667eea; margin-bottom: 20px;">Your Account Has Been Created</h2>
            
            <p>Dear <strong>%s</strong>,</p>
            
            <p>Your <strong>%s</strong> account has been created successfully by our administrator.</p>
            
            <div style="background-color: #e7f3ff; padding: 20px; border-radius: 8px; margin: 20px 0;">
                <p style="margin: 0;"><strong>📧 Login Instructions:</strong></p>
                <p style="margin: 10px 0 0 0;">Please use your registered email address and the password provided to you to login to your account.</p>
            </div>
            
            <p><strong>Important:</strong> For security reasons, we recommend changing your password after your first login.</p>
            
            <p style="margin-top: 30px;">
                <strong>Welcome aboard,</strong><br>
                <span style="color: #667eea;">The RestO'Sure Team</span>
            </p>
            """.formatted(userName, roleName);
    }

    /**
     * Format account suspended email
     */
    public static String formatAccountSuspendedEmail(String userName) {
        return """
            <h2 style="color: #dc3545; margin-bottom: 20px;">Account Suspended</h2>
            
            <p>Dear <strong>%s</strong>,</p>
            
            <p>We regret to inform you that your RestO'Sure account has been temporarily suspended.</p>
            
            <div style="background-color: #fff3cd; padding: 20px; border-left: 4px solid #ffc107; margin: 20px 0;">
                <p style="margin: 0;"><strong>⚠️ What does this mean?</strong></p>
                <p style="margin: 10px 0 0 0;">You will not be able to access your account or services until the suspension is lifted.</p>
            </div>
            
            <p>If you believe this is an error or need more information, please contact our support team immediately:</p>
            
            <div style="background-color: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0;">
                <p style="margin: 0;"><strong>📞 Support:</strong> +91-1800-XXX-XXXX</p>
                <p style="margin: 5px 0 0 0;"><strong>📧 Email:</strong> support@restosure.com</p>
            </div>
            
            <p style="margin-top: 30px;">
                <strong>Sincerely,</strong><br>
                <span style="color: #667eea;">The RestO'Sure Team</span>
            </p>
            """.formatted(userName);
    }

    /**
     * Format account reactivated email
     */
    public static String formatAccountReactivatedEmail(String userName) {
        return """
            <h2 style="color: #28a745; margin-bottom: 20px;">Account Reactivated ✅</h2>
            
            <p>Dear <strong>%s</strong>,</p>
            
            <p>Great news! Your RestO'Sure account has been successfully reactivated.</p>
            
            <div style="background-color: #d4edda; padding: 20px; border-left: 4px solid #28a745; margin: 20px 0;">
                <p style="margin: 0;"><strong>✨ You're All Set!</strong></p>
                <p style="margin: 10px 0 0 0;">You can now login and access all your account features and services.</p>
            </div>
            
            <p>Thank you for your patience and understanding.</p>
            
            <p style="margin-top: 30px;">
                <strong>Welcome back,</strong><br>
                <span style="color: #667eea;">The RestO'Sure Team</span>
            </p>
            """.formatted(userName);
    }
}
