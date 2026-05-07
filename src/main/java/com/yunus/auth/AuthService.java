package com.yunus.auth;
import com.yunus.auth.dto.AuthResponse;
import com.yunus.auth.dto.LoginRequest;
import com.yunus.auth.dto.RegisterRequest;
import com.yunus.auth.entity.Role;
import com.yunus.auth.entity.User;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import com.yunus.auth.repository.RoleRepository;
import com.yunus.auth.repository.UserRepository;
import com.yunus.security.CustomUserDetailsService;
import com.yunus.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorType.DUPLICATE_ENTRY, "Email already exists");
        }

        Role role = roleRepository.findByName(Role.RoleName.AGENT)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "Role not found"));

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(role)
                .build();


        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, user.getEmail(), "User registered successfully");

    }

    public AuthResponse login(LoginRequest request) {


        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, request.email(), "User logged in successfully");
    }

}
