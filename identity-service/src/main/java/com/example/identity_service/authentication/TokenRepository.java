package com.example.identity_service.authentication;

import com.example.identity_service.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, String> {

    void deleteAllByUser(User user);

    Optional<Token> findByRefreshToken(String refreshToken);
}
