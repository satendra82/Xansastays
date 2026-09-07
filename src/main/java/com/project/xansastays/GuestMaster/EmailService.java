package com.project.xansastays.GuestMaster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(String email, String otp) {

        try {

            System.out.println("Sending mail to : " + email);

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom("xansastay@gmail.com");
            message.setTo(email);
            message.setSubject("Xansa Stay OTP Verification");
            message.setText(
                    "Welcome to Xansa Stay.\n\n" +
                            "Your OTP is : " + otp +
                            "\n\nValid for 5 minutes."
            );

            mailSender.send(message);

            System.out.println("MAIL SENT SUCCESSFULLY");

        } catch (Exception e) {

            System.out.println("MAIL FAILED");
            e.printStackTrace();
        }
    }

    public void sendResetOtp(String email, String otp) {
        try {
            System.out.println("Sending password reset OTP to : " + email);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("xansastay@gmail.com");
            message.setTo(email);
            message.setSubject("Xansa Stay — Password Reset OTP");
            message.setText(
                    "Hello,\n\n" +
                            "You requested a password reset for your Xansa Stay account.\n\n" +
                            "Your OTP is : " + otp +
                            "\n\nThis code is valid for 5 minutes." +
                            "\n\nIf you did not request this, please ignore this email." +
                            "\n\n— Xansa Stay Team"
            );

            mailSender.send(message);
            System.out.println("RESET OTP MAIL SENT SUCCESSFULLY");

        } catch (Exception e) {
            System.out.println("RESET OTP MAIL FAILED");
            e.printStackTrace();
        }
    }
}