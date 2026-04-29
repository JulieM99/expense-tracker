package com.example.identity_service.common.event;

import com.example.identity_service.common.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final EmailService emailService;

    @Profile("!test")
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {
        emailService.sendWelcomeEmail(
                event.email(),
                event.firstName()
        );
    }

    @Profile("!test")
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleteUser (UserDeleteEvent event){
        emailService.sendDeleteConfirmationEmail(
                event.email(),
                event.firstName()
        );
    }

    @Profile("!test")
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {

        //todo podstawic poprawny link do zmiany hasla
        String link = "https://frontend.com/reset-password?token=" + event.token();

        emailService.sendPasswordResetEmail(
                event.email(),
                event.firstName(),
                link
        );
    }
}