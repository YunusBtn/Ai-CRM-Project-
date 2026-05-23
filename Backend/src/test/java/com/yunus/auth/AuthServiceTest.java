package com.yunus.auth;

import com.yunus.auth.dto.AuthResponse;
import com.yunus.auth.dto.LoginRequest;
import com.yunus.auth.dto.RegisterRequest;
import com.yunus.auth.dto.UserRoleUpdateRequest;
import com.yunus.auth.entity.Role;
import com.yunus.auth.entity.User;
import com.yunus.auth.repository.RoleRepository;
import com.yunus.auth.repository.UserRepository;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import com.yunus.security.CustomUserDetailsService;
import com.yunus.security.JwtService;
import com.yunus.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private Role agentRole;
    private User savedUser;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        agentRole = new Role();
        agentRole.setName(Role.RoleName.AGENT);

        savedUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Ali")
                .lastName("Veli")
                .isActive(true)
                .isDeleted(false)
                .role(agentRole)
                .build();

        // UserPrincipal ile gerçek UserDetails nesnesi oluşturuyoruz
        userDetails = new UserPrincipal(savedUser);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // register
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register başarılı olduğunda password encode edilmeli, user kaydedilmeli ve JWT üretilmeli")
    void register_WhenSuccess_ShouldEncodePasswordSaveUserAndGenerateToken() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "password123", "Ali", "Veli"
        );

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.AGENT)).thenReturn(Optional.of(agentRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("test@example.com");

        // Password encode edildi mi?
        verify(passwordEncoder).encode("password123");

        // User kaydedildi mi?
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encodedPassword");
        assertThat(userCaptor.getValue().getRole()).isSameAs(agentRole);

        // JWT üretildi mi?
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    @DisplayName("register email duplicate ise DUPLICATE_ENTRY fırlatılmalı")
    void register_WhenEmailAlreadyExists_ShouldThrowDuplicateEntry() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "duplicate@example.com", "password123", "Ali", "Veli"
        );
        when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.DUPLICATE_ENTRY));

        // Hiçbir kayıt yapılmamalı
        verify(userRepository, never()).save(any());
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("register AGENT rolü bulunamazsa NOT_FOUND fırlatılmalı")
    void register_WhenAgentRoleNotFound_ShouldThrowNotFound() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "password123", "Ali", "Veli"
        );
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.AGENT)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.NOT_FOUND));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register başarılı olduğunda AuthResponse dönmeli")
    void register_WhenSuccess_ShouldReturnAuthResponse() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "password123", "Ali", "Veli"
        );

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.AGENT)).thenReturn(Optional.of(agentRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response.message()).isNotBlank();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // login
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login başarılı olduğunda authenticationManager, userDetailsService ve jwtService çağrılmalı")
    void login_WhenSuccess_ShouldAuthenticateLoadUserAndGenerateToken() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // authenticate başarılıysa null döner (exception fırlatmaz)
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("test@example.com");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername("test@example.com");
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    @DisplayName("login başarılı olduğunda AuthResponse dönmeli ve mesaj içermeli")
    void login_WhenSuccess_ShouldReturnAuthResponseWithMessage() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response.message()).isNotBlank();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateUserRole
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateUserRole başarılı olduğunda user'ın rolü güncellenmeli ve save çağrılmalı")
    void updateUserRole_WhenSuccess_ShouldUpdateRoleAndSave() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Role adminRole = new Role();
        adminRole.setName(Role.RoleName.ADMIN);

        UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.RoleName.ADMIN);

        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(savedUser));
        when(roleRepository.findByName(Role.RoleName.ADMIN)).thenReturn(Optional.of(adminRole));

        // Act
        authService.updateUserRole(userId, request);

        // Assert
        assertThat(savedUser.getRole()).isSameAs(adminRole);
        verify(userRepository).save(savedUser);
    }

    @Test
    @DisplayName("updateUserRole user bulunamazsa NOT_FOUND fırlatılmalı")
    void updateUserRole_WhenUserNotFound_ShouldThrowNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.RoleName.ADMIN);

        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.updateUserRole(userId, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.NOT_FOUND));

        verifyNoInteractions(roleRepository);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUserRole role bulunamazsa NOT_FOUND fırlatılmalı")
    void updateUserRole_WhenRoleNotFound_ShouldThrowNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.RoleName.ADMIN);

        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(savedUser));
        when(roleRepository.findByName(Role.RoleName.ADMIN)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.updateUserRole(userId, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.NOT_FOUND));

        verify(userRepository, never()).save(any());
    }
}
