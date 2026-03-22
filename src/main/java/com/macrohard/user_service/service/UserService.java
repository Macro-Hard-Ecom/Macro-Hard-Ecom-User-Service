package com.macrohard.user_service.service;
import com.macrohard.user_service.dto.ProfileResponse;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import com.macrohard.user_service.dto.AuthResponse;
import com.macrohard.user_service.dto.LoginRequest;
import com.macrohard.user_service.dto.RegisterRequest;
import com.macrohard.user_service.model.User;
import com.macrohard.user_service.repository.UserRepository;
import com.macrohard.user_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${product.service.url}")
    private String productServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(),user.getId());
        return new AuthResponse(user.getId(), token, user.getEmail(), user.getName(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(),user.getId());
        return new AuthResponse(user.getId(), token, user.getEmail(), user.getName(), user.getRole());
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public boolean validateToken(String token) {
        return jwtUtil.isTokenValid(token);
    }

    public ProfileResponse getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int totalListings = 0;
        try {
            String url = productServiceUrl + "/api/products/count/" + id;
            Integer count = restTemplate.getForObject(url, Integer.class);
            if (count != null) totalListings = count;
        } catch (Exception e) {
            // if product service is down, return 0
            totalListings = 0;
        }

        return new ProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                totalListings
        );
    }

}
