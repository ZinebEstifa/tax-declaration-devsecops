package com.example.Declarations.fiscaux.service;

import com.example.Declarations.fiscaux.entity.Utilisateur;
import com.example.Declarations.fiscaux.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String numeroFiscal) throws UsernameNotFoundException {
        Utilisateur utilisateur = userRepository.findByNumeroFiscal(numeroFiscal)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec le numéro fiscal: " + numeroFiscal));

        return new User(
                utilisateur.getNumeroFiscal(),
                utilisateur.getMotDePasseHash(),
                !utilisateur.isCompteBloque(),
                true,
                true,
                true,
                Collections.emptyList()
        );
    }
}
