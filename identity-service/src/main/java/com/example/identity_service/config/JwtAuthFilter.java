package com.example.identity_service.config;

import com.example.identity_service.authentication.JwtService;
import com.example.identity_service.authentication.UserRepository;
import com.example.identity_service.error.ApiError;
import com.example.identity_service.authentication.User;

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

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                User userDetails = userRepository.findByEmail(userEmail)
                        .orElse(null);

                if (userDetails != null && jwtService.isTokenValid(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            writeError(response, request, "Token expired");
        } catch (Exception e) {
            writeError(response, request, "Invalid or malformed token");
        }
    }

    private void writeError(HttpServletResponse response, HttpServletRequest request, String message)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        ApiError apiError = new ApiError(
                request.getRequestURI(),
                message,
                HttpServletResponse.SC_UNAUTHORIZED,
                LocalDateTime.now()
        );

        String json = """
            {
              "path": "%s",
              "message": "%s",
              "status": %d,
              "timestamp": "%s"
            }
            """.formatted(
                apiError.path(),
                apiError.message(),
                apiError.status(),
                apiError.timestamp()
        );

        response.getWriter().write(json);
        response.getWriter().flush();
    }
}