package com.example.identity_service.common.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;


//todo: zrobic entity do tokenow z reset password i wysylke maila z tym resetem
//testy tego wszytewkigo
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendWelcomeEmail(String email, String name) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Welcome!");
        message.setText("Hi " + name + ", welcome to our app!");

        mailSender.send(message);
    }

    public void sendDeleteConfirmationEmail(String email, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your account was deleted!");
        message.setText("Hi " + name + ", you data was successfully deleted form the application!");

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String email, String name, String link) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password reset request");

        message.setText(
                "Hi " + name + ",\n\n" +
                        "Click the link below to reset your password:\n" +
                        link + "\n\n" +
                        "If you didn't request this, ignore this email."
        );

        mailSender.send(message);
    }
}
