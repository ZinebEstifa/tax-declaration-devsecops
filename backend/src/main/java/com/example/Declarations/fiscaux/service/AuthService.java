package com.example.Declarations.fiscaux.service;

import com.example.Declarations.fiscaux.config.JwtUtils;
import com.example.Declarations.fiscaux.dto.LoginRequest;
import com.example.Declarations.fiscaux.dto.LoginResponse;
import com.example.Declarations.fiscaux.dto.RegisterRequest;
import com.example.Declarations.fiscaux.entity.Utilisateur;
import com.example.Declarations.fiscaux.repository.UserRepository;
import com.example.Declarations.fiscaux.security.Argon2idPasswordEncoder;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Argon2idPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostConstruct
    @Transactional
    public void seedTestUser() {
        String testNumFiscal = "12345678";
        if (userRepository.findByNumeroFiscal(testNumFiscal).isEmpty()) {
            logger.info("Initialisation de l'utilisateur de test: {}", testNumFiscal);
            String rawPassword = "Password123!";
            String encodedPassword = passwordEncoder.encode(rawPassword);
            String[] parts = encodedPassword.split(":");
            String salt = parts[0];

            Utilisateur user = Utilisateur.builder()
                    .numeroFiscal(testNumFiscal)
                    .motDePasseHash(encodedPassword)
                    .salt(salt)
                    .raisonSociale("DGI Test Company S.A.R.L")
                    .tentativesEchouees(0)
                    .compteBloque(false)
                    .build();

            userRepository.save(user);
            logger.info("Utilisateur de test initialisé avec succès.");
        }
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String numFiscal = request.getNumFiscal();
        if (userRepository.findByNumeroFiscal(numFiscal).isPresent()) {
            return LoginResponse.builder()
                    .success(false)
                    .error("Ce numéro fiscal est déjà enregistré dans le système SIMPL.")
                    .numeroFiscal(numFiscal)
                    .build();
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String[] parts = encodedPassword.split(":");
        String salt = parts[0];

        Utilisateur user = Utilisateur.builder()
                .numeroFiscal(numFiscal)
                .motDePasseHash(encodedPassword)
                .salt(salt)
                .raisonSociale(request.getRaisonSociale() != null && !request.getRaisonSociale().isBlank() ? request.getRaisonSociale() : "Société IF " + numFiscal)
                .tentativesEchouees(0)
                .compteBloque(false)
                .build();

        userRepository.save(user);
        logger.info("Nouveau contribuable inscrit avec succès: {}", numFiscal);

        String token = jwtUtils.generateJwtToken(numFiscal);
        return LoginResponse.builder()
                .success(true)
                .token(token)
                .numeroFiscal(numFiscal)
                .build();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String numFiscal = request.getNumFiscal();
        String password = request.getPassword();

        Utilisateur user = userRepository.findByNumeroFiscal(numFiscal).orElse(null);
        
        // Auto-inscription dynamique si l'identifiant fiscal n'existe pas encore en base de données
        if (user == null) {
            logger.info("Création automatique et authentification du contribuable IF: {}", numFiscal);
            String encodedPassword = passwordEncoder.encode(password);
            String[] parts = encodedPassword.split(":");
            String salt = parts[0];

            user = Utilisateur.builder()
                    .numeroFiscal(numFiscal)
                    .motDePasseHash(encodedPassword)
                    .salt(salt)
                    .raisonSociale("Société IF " + numFiscal)
                    .tentativesEchouees(0)
                    .compteBloque(false)
                    .build();

            userRepository.save(user);

            String token = jwtUtils.generateJwtToken(numFiscal);
            return LoginResponse.builder()
                    .success(true)
                    .token(token)
                    .numeroFiscal(numFiscal)
                    .build();
        }

        if (user.isCompteBloque()) {
            logger.warn("Tentative de connexion à un compte bloqué: {}", numFiscal);
            return LoginResponse.builder()
                    .success(false)
                    .error("Compte bloqué suite à 3 tentatives échouées.")
                    .numeroFiscal(numFiscal)
                    .build();
        }

        boolean matches = passwordEncoder.matches(password, user.getMotDePasseHash());
        if (matches) {
            user.setTentativesEchouees(0);
            user.setDerniereConnexion(java.time.LocalDateTime.now());
            userRepository.save(user);

            String token = jwtUtils.generateJwtToken(numFiscal);
            logger.info("Connexion réussie pour le contribuable {}", numFiscal);
            return LoginResponse.builder()
                    .success(true)
                    .token(token)
                    .numeroFiscal(numFiscal)
                    .build();
        } else {
            int attempts = user.getTentativesEchouees() + 1;
            user.setTentativesEchouees(attempts);
            if (attempts >= 3) {
                user.setCompteBloque(true);
                logger.error("Le compte avec le numéro fiscal {} a été BLOQUÉ suite à 3 tentatives échouées", numFiscal);
            } else {
                logger.warn("Tentative de connexion échouée ({}/3) pour le numéro fiscal {}", attempts, numFiscal);
            }
            userRepository.save(user);

            if (user.isCompteBloque()) {
                return LoginResponse.builder()
                        .success(false)
                        .error("Compte bloqué suite à 3 tentatives échouées.")
                        .numeroFiscal(numFiscal)
                        .build();
            } else {
                return LoginResponse.builder()
                        .success(false)
                        .error("Identifiants de connexion invalides. Il vous reste " + (3 - attempts) + " tentative(s).")
                        .numeroFiscal(numFiscal)
                        .build();
            }
        }
    }
}
