package com.example.Declarations.fiscaux.service;

import com.example.Declarations.fiscaux.config.JwtUtils;
import com.example.Declarations.fiscaux.dto.LoginRequest;
import com.example.Declarations.fiscaux.dto.LoginResponse;
import com.example.Declarations.fiscaux.entity.Utilisateur;
import com.example.Declarations.fiscaux.repository.UserRepository;
import com.example.Declarations.fiscaux.security.Argon2idPasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Argon2idPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private Utilisateur user;

    @BeforeEach
    void setUp() {
        user = Utilisateur.builder()
                .id(1L)
                .numeroFiscal("12345678")
                .motDePasseHash("encodedPass")
                .tentativesEchouees(0)
                .compteBloque(false)
                .build();
    }

    @Test
    @DisplayName("Connexion réussie")
    void testLogin_Success() {
        when(userRepository.findByNumeroFiscal("12345678")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "encodedPass")).thenReturn(true);
        when(jwtUtils.generateJwtToken("12345678")).thenReturn("mock-jwt-token");

        LoginRequest req = new LoginRequest("12345678", "Password123!");
        LoginResponse res = authService.login(req);

        assertTrue(res.isSuccess());
        assertEquals("mock-jwt-token", res.getToken());
        assertEquals(0, user.getTentativesEchouees());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Mot de passe incorrect - incrémentation des échecs (1/3)")
    void testLogin_WrongPassword() {
        when(userRepository.findByNumeroFiscal("12345678")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass", "encodedPass")).thenReturn(false);

        LoginRequest req = new LoginRequest("12345678", "WrongPass");
        LoginResponse res = authService.login(req);

        assertFalse(res.isSuccess());
        assertEquals(1, user.getTentativesEchouees());
        assertFalse(user.isCompteBloque());
        assertTrue(res.getError().contains("2 tentative(s)"));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Verrouillage automatique du compte après 3 échecs")
    void testLogin_LockAccountOnThirdFailure() {
        user.setTentativesEchouees(2);
        when(userRepository.findByNumeroFiscal("12345678")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass", "encodedPass")).thenReturn(false);

        LoginRequest req = new LoginRequest("12345678", "WrongPass");
        LoginResponse res = authService.login(req);

        assertFalse(res.isSuccess());
        assertEquals(3, user.getTentativesEchouees());
        assertTrue(user.isCompteBloque());
        assertTrue(res.getError().contains("bloqué"));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Refus de connexion pour un compte déjà bloqué")
    void testLogin_AlreadyLockedAccount() {
        user.setCompteBloque(true);
        when(userRepository.findByNumeroFiscal("12345678")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest("12345678", "Password123!");
        LoginResponse res = authService.login(req);

        assertFalse(res.isSuccess());
        assertTrue(res.getError().contains("bloqué"));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
