package com.blockchainbiz.app.client_api.service;

import com.blockchainbiz.app.client_api.exception.CustomException;
import com.blockchainbiz.app.client_api.model.User;
import com.blockchainbiz.app.client_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new CustomException("Użytkownik o podanej nazwie już istnieje.");
        }
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException("Użytkownik o podanym ID nie istnieje."));
    }

    // inne metody np. update user, delete user...
}