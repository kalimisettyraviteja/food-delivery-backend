package com.fooddelivery.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.api.url}")
    private String brevoApiUrl;

    @Value("${brevo.sender.email}")
    private String fromEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    @Override
    public void sendVerificationOtp(String toEmail, String name, String otp) {
        sendOtpMail(
                toEmail,
                name,
                otp,
                "Cravyo - Verify Your Email",
                "VERIFY YOUR EMAIL"
        );
    }

    @Override
    public void sendPasswordResetOtp(String toEmail, String name, String otp) {
        sendOtpMail(
                toEmail,
                name,
                otp,
                "Cravyo - Password Reset OTP",
                "RESET YOUR PASSWORD"
        );
    }

    private void sendOtpMail(String toEmail, String name, String otp, String subject, String title) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            String html = """
                    <html>
                    <body style="margin:0; padding:0; background-color:#f8fafc; font-family:Arial, Helvetica, sans-serif;">
                        <div style="width:100%%; padding:24px 0;">
                            <div style="max-width:700px; margin:0 auto; background-color:#ffffff; border:1px solid #e2e8f0; border-radius:18px; overflow:hidden;">

                                <div style="padding:34px 28px 22px 28px; text-align:center; border-bottom:1px solid #f1f5f9;">
                                    <div style="font-size:54px; line-height:54px; margin-bottom:10px;">🛵</div>

                                    <div style="font-size:56px; line-height:62px; font-weight:900; letter-spacing:-2px; margin:0; color:#0f2f22;">
                                        Cravy<span style="color:#ff6b1a;">o</span>
                                    </div>

                                    <div style="margin-top:10px; font-size:18px; color:#334155; font-style:italic; letter-spacing:0.5px;">
                                        <span style="color:#ff6b1a; font-weight:700;">—</span>
                                        Cravings Delivery
                                        <span style="color:#ff6b1a; font-weight:700;">—</span>
                                    </div>

                                    <div style="margin-top:28px; text-align:center; font-size:0;">
                                        <div style="display:inline-block; width:24%%; vertical-align:top;">
                                            <div style="width:68px; height:68px; line-height:68px; margin:0 auto 10px auto; border-radius:50%%; background:#fff1e8; font-size:30px;">🛵</div>
                                            <div style="font-size:14px; font-weight:800; color:#0f172a; letter-spacing:1px;">FAST</div>
                                        </div>

                                        <div style="display:inline-block; width:24%%; vertical-align:top; border-left:1px solid #e2e8f0;">
                                            <div style="width:68px; height:68px; line-height:68px; margin:0 auto 10px auto; border-radius:50%%; background:#eef8e8; font-size:30px;">🍽️</div>
                                            <div style="font-size:14px; font-weight:800; color:#0f172a; letter-spacing:1px;">FRESH</div>
                                        </div>

                                        <div style="display:inline-block; width:24%%; vertical-align:top; border-left:1px solid #e2e8f0;">
                                            <div style="width:68px; height:68px; line-height:68px; margin:0 auto 10px auto; border-radius:50%%; background:#eaf3ff; font-size:30px;">✅</div>
                                            <div style="font-size:14px; font-weight:800; color:#0f172a; letter-spacing:1px;">RELIABLE</div>
                                        </div>

                                        <div style="display:inline-block; width:24%%; vertical-align:top; border-left:1px solid #e2e8f0;">
                                            <div style="width:68px; height:68px; line-height:68px; margin:0 auto 10px auto; border-radius:50%%; background:#fff6dd; font-size:30px;">😊</div>
                                            <div style="font-size:14px; font-weight:800; color:#0f172a; letter-spacing:1px;">HAPPINESS</div>
                                        </div>
                                    </div>
                                </div>

                                <div style="padding:32px 28px; text-align:center;">
                                    <div style="display:inline-block; background-color:#fff7ed; color:#ea580c; font-size:12px; font-weight:800; letter-spacing:2px; padding:8px 14px; border-radius:999px; margin-bottom:18px;">
                                        FOOD DELIVERY SECURITY
                                    </div>

                                    <h1 style="margin:0 0 12px 0; color:#0f172a; font-size:30px; line-height:38px; font-weight:900;">
                                        %s
                                    </h1>

                                    <p style="margin:0 0 10px 0; color:#334155; font-size:17px; line-height:28px;">
                                        Hi <strong>%s</strong>,
                                    </p>

                                    <p style="margin:0 0 28px 0; color:#475569; font-size:16px; line-height:26px;">
                                        Use the OTP below to continue your secure action.
                                    </p>

                                    <div style="margin:0 auto 24px auto; max-width:320px; background-color:#fff7ed; border:1px solid #fdba74; border-radius:16px; padding:18px 22px;">
                                        <div style="font-size:13px; color:#9a3412; letter-spacing:2px; font-weight:800; margin-bottom:10px;">
                                            YOUR OTP CODE
                                        </div>
                                        <div style="font-size:34px; line-height:40px; font-weight:900; letter-spacing:10px; color:#0f172a;">
                                            %s
                                        </div>
                                    </div>

                                    <div style="margin:0 0 22px 0; background-color:#fef2f2; border:1px solid #fca5a5; color:#dc2626; padding:14px 18px; border-radius:12px; font-size:15px; font-weight:800; line-height:24px;">
                                        This OTP is valid for 10 minutes.
                                    </div>

                                    <p style="margin:0 0 8px 0; color:#64748b; font-size:14px; line-height:24px;">
                                        Do not share this OTP with anyone.
                                    </p>

                                    <p style="margin:0; color:#64748b; font-size:14px; line-height:24px;">
                                        If you did not request this, please ignore this email.
                                    </p>
                                </div>

                                <div style="padding:18px 24px; text-align:center; background-color:#f8fafc; border-top:1px solid #e2e8f0;">
                                    <p style="margin:0; color:#94a3b8; font-size:13px; letter-spacing:1px;">
                                        CRAVYO TEAM
                                    </p>
                                </div>
                            </div>
                        </div>
                    </body>
                    </html>
                    """.formatted(title, name, otp);

            Map<String, Object> payload = Map.of(
                    "sender", Map.of(
                            "name", senderName,
                            "email", fromEmail
                    ),
                    "to", List.of(
                            Map.of(
                                    "email", toEmail,
                                    "name", name
                            )
                    ),
                    "subject", subject,
                    "htmlContent", html
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(brevoApiUrl, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Brevo email failed. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to send email.");
            }

            log.info("Brevo email sent successfully to {}", toEmail);

        } catch (Exception e) {
            log.error("Brevo email send failed for {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send email.");
        }
    }
}