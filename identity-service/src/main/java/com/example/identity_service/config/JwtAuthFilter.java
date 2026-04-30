package com.example.identity_service.config;

import com.example.identity_service.user.UserRepository;
import com.example.identity_service.error.ApiError;
import com.example.identity_service.user.User;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.authcommon.JwtService;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // sprawdzenie czy to JWT request
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //wyciagniecie jwt
        final String jwt = authHeader.substring(7);

        try {
            // dekodowanie jwt, wyciagniecie e-maila
            final String userEmail = jwtService.extractUsername(jwt);

            // czy user jest juz zalogowany
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                User userDetails = userRepository.findByEmail(userEmail)
                        .orElse(null);

                // sprawdzenie pobranego usera i czy jwt token jest wazny
                if (userDetails != null && jwtService.isTokenValid(jwt, userEmail)) {

                    // utworzenie authentication object
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    //security context - spring wie ze user jest zalogowasny od teraz i teraz idziemy do tego security configni sprawdzamy endpinty
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            writeError(response, request, "Token expired");
        } catch (io.jsonwebtoken.JwtException e) {
            writeError(response, request, "Invalid token");
        } catch (Exception e) {
            filterChain.doFilter(request, response);
        }
    }

    private void writeError(HttpServletResponse response,
                            HttpServletRequest request,
                            String message) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        ApiError apiError = new ApiError(
                request.getRequestURI(),
                message,
                HttpServletResponse.SC_UNAUTHORIZED,
                LocalDateTime.now(),
                null
        );

        response.getWriter().write(objectMapper.writeValueAsString(apiError));
    }
}