package com.nefdev.ecommerce.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.nefdev.ecommerce.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JWTService {


        /** La chiave segreta per criptare i JWT. */
        @Value("${jwt.algorithm.key}")
        private String algorithmKey;

        /** L'emittente con cui viene firmato il JWT. */
        @Value("${jwt.issuer}")
        private String issuer;

        /** Dopo quanti secondi dalla generazione il JWT scade. */
        @Value("${jwt.expiryInSeconds}")
        private int expiryInSeconds;

        /** L'algoritmo generato dopo la costruzione. */
        private Algorithm algorithm;

        /** La chiave del claim JWT per lo username. */
        private static final String USERNAME_KEY = "USERNAME";
        private static final String EMAIL_KEY = "EMAIL";

        /**
         * Inizializza l'algoritmo per la firma del JWT.
         * Questo metodo viene eseguito dopo che il bean è stato costruito e le dipendenze sono state iniettate.
         */
        @PostConstruct
        public void postConstruct() {
            algorithm = Algorithm.HMAC256(algorithmKey);
        }

        /**
         * Genera un JWT basato sull'utente fornito.
         * @param user L'utente per cui generare il JWT.
         * @return Il JWT generato.
         */
        public String generateJWT(User user) {
            return JWT.create()
                    .withClaim(USERNAME_KEY, user.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * expiryInSeconds)))
                    .withIssuer(issuer)
                    .sign(algorithm);
        }

        /**
         * Genera un token  per la verifica dell'email.
         * @param user L'utente per cui generare il token.
         * @return Il token generato.
         */
        public String generateVerificationJWT(User user) {
            return JWT.create()
                    .withClaim(EMAIL_KEY, user.getEMail())
                    .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * expiryInSeconds)))
                    .withIssuer(issuer)
                    .sign(algorithm);
        }

        /**
         * Recupera lo username da un JWT.
         * @param token Il JWT da decodificare.
         * @return Lo username memorizzato nel JWT.
         */
        public String getUsername(String token) {
            return JWT.decode(token).getClaim(USERNAME_KEY).asString();
        }
    }

