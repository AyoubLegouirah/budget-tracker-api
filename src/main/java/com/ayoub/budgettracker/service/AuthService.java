package com.ayoub.budgettracker.service;

import com.ayoub.budgettracker.dto.request.LoginRequest;
import com.ayoub.budgettracker.dto.request.RegisterRequest;
import com.ayoub.budgettracker.dto.response.AuthResponse;
import com.ayoub.budgettracker.entity.Category;
import com.ayoub.budgettracker.entity.User;
import com.ayoub.budgettracker.repository.CategoryRepository;
import com.ayoub.budgettracker.repository.UserRepository;
import com.ayoub.budgettracker.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    private static final List<Object[]> DEFAULT_CATEGORIES = List.of(
        new Object[]{"Alimentation", "#22c55e", "EXPENSE", "🛒"},
        new Object[]{"Transport",    "#3b82f6", "EXPENSE", "🚗"},
        new Object[]{"Logement",     "#f97316", "EXPENSE", "🏠"},
        new Object[]{"Loisirs",      "#a855f7", "EXPENSE", "🎬"},
        new Object[]{"Sport",        "#06b6d4", "EXPENSE", "🏋️"},
        new Object[]{"Santé",        "#ef4444", "EXPENSE", "💊"},
        new Object[]{"Shopping",     "#ec4899", "EXPENSE", "🛍️"},
        new Object[]{"Salaire",      "#84cc16", "INCOME",  "💼"},
        new Object[]{"Épargne",      "#eab308", "INCOME",  "🏦"},
        new Object[]{"Divers",       "#6b7280", "EXPENSE", "📦"}
    );

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        DEFAULT_CATEGORIES.forEach(c -> categoryRepository.save(
            Category.builder()
                .name((String) c[0])
                .color((String) c[1])
                .type((String) c[2])
                .icon((String) c[3])
                .user(user)
                .build()
        ));
        String token = jwtTokenProvider.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String token = jwtTokenProvider.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}