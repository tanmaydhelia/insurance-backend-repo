package com.insurance_notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
	@Autowired
    private JavaMailSender javaMailSender;

    private String fromEmail = "noreply.restosure@gmail.com";

    public void sendEmail(String to, String subject, String body) {
        try {
            log.info("Sending email to: {}", to);
            
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            
            // Wrap the body content with professional HTML template
            String htmlContent = buildHtmlTemplate(body);
            helper.setText(htmlContent, true); // true indicates HTML

            javaMailSender.send(mimeMessage);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
    
    private String buildHtmlTemplate(String body) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #f4f7fa;
                        margin: 0;
                        padding: 0;
                    }
                    .email-container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        padding: 30px 20px;
                        text-align: center;
                        color: white;
                    }
                    .logo {
                        font-size: 32px;
                        font-weight: bold;
                        letter-spacing: 1px;
                        margin: 0;
                    }
                    .tagline {
                        font-size: 14px;
                        margin: 5px 0 0 0;
                        opacity: 0.9;
                    }
                    .content {
                        padding: 40px 30px;
                        line-height: 1.8;
                        color: #333333;
                        font-size: 15px;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 25px 30px;
                        border-top: 1px solid #e9ecef;
                    }
                    .company-info {
                        text-align: center;
                        color: #6c757d;
                        font-size: 13px;
                        margin-bottom: 15px;
                    }
                    .company-name {
                        font-weight: bold;
                        color: #667eea;
                        font-size: 16px;
                    }
                    .privacy-section {
                        border-top: 1px solid #dee2e6;
                        padding-top: 15px;
                        margin-top: 15px;
                    }
                    .privacy-title {
                        font-weight: bold;
                        color: #495057;
                        font-size: 12px;
                        margin-bottom: 8px;
                    }
                    .privacy-text {
                        color: #6c757d;
                        font-size: 11px;
                        line-height: 1.6;
                        margin: 5px 0;
                    }
                    .social-links {
                        text-align: center;
                        margin-top: 15px;
                        padding-top: 15px;
                        border-top: 1px solid #dee2e6;
                    }
                    .social-links a {
                        color: #667eea;
                        text-decoration: none;
                        margin: 0 10px;
                        font-size: 12px;
                    }
                    .divider {
                        height: 1px;
                        background: linear-gradient(to right, transparent, #667eea, transparent);
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <!-- Header -->
                    <div class="header">
                        <h1 class="logo">RestO'Sure</h1>
                        <p class="tagline">Your Trusted Health Insurance Partner</p>
                    </div>
                    
                    <!-- Content -->
                    <div class="content">
                        """ + body + """
                    </div>
                    
                    <!-- Footer -->
                    <div class="footer">
                        <div class="company-info">
                            <p class="company-name">RestO'Sure Insurance Services</p>
                            <p style="margin: 5px 0; font-size: 12px;">Making Healthcare Accessible and Affordable</p>
                        </div>
                        
                        <div class="divider"></div>
                        
                        <div class="privacy-section">
                            <p class="privacy-title">Privacy & Security</p>
                            <p class="privacy-text">
                                This email contains confidential information intended solely for the recipient. 
                                RestO'Sure is committed to protecting your personal data in compliance with applicable 
                                data protection regulations. We never share your information with third parties without 
                                your explicit consent.
                            </p>
                            <p class="privacy-text">
                                <strong>Your Rights:</strong> You have the right to access, modify, or delete your personal 
                                information at any time. Contact our privacy team at privacy@restosure.com for assistance.
                            </p>
                            <p class="privacy-text">
                                <strong>Security:</strong> All communications are encrypted and stored securely. We employ 
                                industry-standard security measures to protect your data.
                            </p>
                        </div>
                        
                        <div class="social-links">
                            <a href="#">Terms of Service</a> | 
                            <a href="#">Privacy Policy</a> | 
                            <a href="#">Contact Support</a> | 
                            <a href="#">Unsubscribe</a>
                        </div>
                        
                        <p style="text-align: center; color: #adb5bd; font-size: 11px; margin-top: 15px;">
                            © 2026 RestO'Sure Insurance Services. All rights reserved.<br>
                            This is an automated message. Please do not reply to this email.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}
