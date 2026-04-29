package com.example.identity_service.email;

import com.example.identity_service.common.email.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void shouldSendWelcomeEmail() {

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendWelcomeEmail("test@test.com", "John");

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals("test@test.com", message.getTo()[0]);
        assertEquals("Welcome!", message.getSubject());
        assertTrue(message.getText().contains("John"));
    }

    @Test
    void shouldSendDeleteConfirmationEmail() {

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendDeleteConfirmationEmail("test@test.com", "John");

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals("test@test.com", message.getTo()[0]);
        assertEquals("Your account was deleted!", message.getSubject());
        assertTrue(message.getText().contains("John"));
        assertTrue(message.getText().contains("successfully deleted"));
    }

    @Test
    void shouldSendPasswordResetEmail() {

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        String link = "http://reset-link";

        emailService.sendPasswordResetEmail("test@test.com", "John", link);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals("test@test.com", message.getTo()[0]);
        assertEquals("Password reset request", message.getSubject());
        assertTrue(message.getText().contains("John"));
        assertTrue(message.getText().contains(link));
        assertTrue(message.getText().contains("reset your password"));
    }
}