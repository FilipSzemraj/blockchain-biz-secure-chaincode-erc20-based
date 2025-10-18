package com.blockchainbiz.app.client_api.controller;

import com.blockchainbiz.app.client_api.config.JwtUtil;
import com.blockchainbiz.app.client_api.model.LoginRequest;
import com.blockchainbiz.app.client_api.model.LoginResponse;
import com.blockchainbiz.app.client_api.model.User;
import com.blockchainbiz.app.client_api.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        var authToken = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        authenticationManager.authenticate(authToken);

        User user = authService.loadUserByUsername(request.getUsername());

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/api/auth/refresh");
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(new LoginResponse(accessToken, user.getId()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue("refreshToken") String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(403).body("Invalid refresh token");
        }
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        User user = authService.loadUserByUsername(username);


        String newAccessToken = jwtUtil.generateAccessToken(username);

        return ResponseEntity.ok(new LoginResponse(newAccessToken, user.getId()));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(
            @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(400).body("Missing or invalid Authorization header");
        }

        String accessToken = authorizationHeader.substring(7); // Usunięcie "Bearer "

        //log.info("Verifying token: {}", accessToken);

        if (jwtUtil.validateToken(accessToken)) {
            return ResponseEntity.ok("Access token is valid");
        }

        //return refresh(refreshToken); // Próbuj odświeżyć token
        return ResponseEntity.status(403).body("Invalid access token");
    }
}
