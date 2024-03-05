package com.nefdev.ecommerce.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
public class WebSecurityConfig {
        private JWTRequestFilter jwtRequestFilter;

        public WebSecurityConfig(JWTRequestFilter jwtRequestFilter) {
            this.jwtRequestFilter = jwtRequestFilter;
        }

        /**
         * Configura la catena dei filtri per la sicurezza.
         * @param http L'oggetto di sicurezza.
         * @return La catena costruita.
         * @throws Exception Lanciata in caso di errore nella configurazione.
         */
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            // Disabilita CSRF e CORS
            http.csrf().disable().cors().disable();

            // Assicura che il filtro di autenticazione venga eseguito prima del filtro di richiesta HTTP
            http.addFilterBefore(jwtRequestFilter, AuthorizationFilter.class);

            // Configura le autorizzazioni delle richieste HTTP
            http.authorizeHttpRequests()
                    // Permette l'accesso pubblico a endpoint specifici
                    .requestMatchers("/product", "/auth/register", "/auth/login", "/auth/verify").permitAll()
                    // Richiede l'autenticazione per tutte le altre richieste
                    .anyRequest().authenticated();

            // Restituisce la catena di filtri costruita
            return http.build();
        }

}
