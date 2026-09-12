package com.CodeGraph.notification.email.template;

public final class OtpEmailTemplate {

    private OtpEmailTemplate() {
    }

    public static String subject() {
        return "CodeGraph - Email Verification Code";
    }

    public static String body(String otp) {
        return """
                Your CodeGraph verification code is:

                %s

                This code will expire in 5 minutes.

                If you did not request this code, you can ignore this email.

                Regards,
                CodeGraph
                """.formatted(otp);
    }
}