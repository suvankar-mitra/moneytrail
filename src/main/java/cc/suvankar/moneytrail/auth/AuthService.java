package cc.suvankar.moneytrail.auth;

import cc.suvankar.moneytrail.auth.dto.AuthResponse;
import cc.suvankar.moneytrail.auth.dto.LoginRequest;
import cc.suvankar.moneytrail.auth.dto.RegisterRequest;
import cc.suvankar.moneytrail.auth.exception.EmailAlreadyExistsException;
import cc.suvankar.moneytrail.auth.exception.InvalidCredentialsException;
import cc.suvankar.moneytrail.user.User;
import cc.suvankar.moneytrail.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public void registerUser(RegisterRequest registerRequest) {

        // Check if email already exists
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            log.warn("Email {} is already present.", registerRequest.getEmail());

            throw new EmailAlreadyExistsException("The requested email is already in use.");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setName(registerRequest.getName());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

        // Save user to DB
        userRepository.save(user);

        log.info("Successfully registered User [name: {}, email: {}]",
                registerRequest.getName(), registerRequest.getEmail());

    }

    public AuthResponse loginUser(LoginRequest loginRequest) {

        // Find user by email
        User foundUser = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.warn("User with email {} does not exist.", loginRequest.getEmail());
                    return new InvalidCredentialsException("Invalid credentials.");
                });

        if (!passwordEncoder.matches(loginRequest.getPassword(),
                foundUser.getPasswordHash())) {
            log.warn("Password hash for email {} does not match.", loginRequest.getEmail());
            throw new InvalidCredentialsException("Invalid credentials.");
        }

        // User exists and password hash matches
        String token = jwtUtil.generateTokenWithUserId(foundUser.getEmail(), foundUser.getId());

        // Return the response
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);

        return authResponse;
    }
}
