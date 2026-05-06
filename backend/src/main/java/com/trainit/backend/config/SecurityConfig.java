package com.trainit.backend.config;

import com.trainit.backend.security.JwtAuthenticationFilter;
import com.trainit.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Konfiguracja Spring Security dla API backendu.
 *
 * <p>Endpointy uwierzytelniania są publiczne, a wszystkie pozostałe wymagają
 * poprawnego tokena Bearer JWT przekazanego w nagłówku Authorization.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/**
	 * Rejestruje łańcuch filtrów bezpieczeństwa.
	 *
	 * @param http obiekt konfiguracji bezpieczeństwa HTTP
	 * @param jwtAuthenticationFilter filtr uwierzytelniający Bearer token
	 * @return skonfigurowany łańcuch filtrów
	 * @throws Exception gdy konfiguracja nie powiedzie się
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
			throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/auth/**").permitAll()
						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	/**
	 * Tworzy serwis JWT z konfiguracji środowiskowej.
	 *
	 * @param secret klucz podpisu JWT
	 * @param expirationSeconds czas ważności tokena w sekundach
	 * @return skonfigurowany serwis JWT
	 */
	@Bean
	public JwtService jwtService(
			@Value("${app.security.jwt.secret:change-this-dev-secret-key}") String secret,
			@Value("${app.security.jwt.expiration-seconds:86400}") long expirationSeconds
	) {
		return new JwtService(secret, expirationSeconds);
	}

	/**
	 * Rejestruje filtr autoryzacji Bearer JWT.
	 *
	 * @param jwtService serwis walidacji tokenów
	 * @return filtr JWT
	 */
	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
		return new JwtAuthenticationFilter(jwtService);
	}
}
