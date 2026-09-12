package com.CodeGraph.notification.email;

import com.CodeGraph.notification.email.template.OtpEmailTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtp(String email, String otp) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);
        message.setSubject(
                OtpEmailTemplate.subject()
        );
        message.setText(
                OtpEmailTemplate.body(otp)
        );

        mailSender.send(message);
    }
}