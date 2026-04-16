package com.trainit.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Konfiguracja komponentu kryptograficznego do hashowania haseł.
 *
 * <p>Rejestruje bean typu {@link BCryptPasswordEncoder} w kontenerze Springa. Ten sam enkoder
 * jest wstrzykiwany do {@link com.trainit.backend.service.AuthService} w celu kodowania haseł
 * przy rejestracji oraz weryfikacji przy logowaniu ({@code matches}).
 *
 * @see com.trainit.backend.service.AuthService
 * @see org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
 */
@Configuration
public class PasswordEncoderConfig {

	/**
	 * Konstruktor domyślny; konfiguracja rejestrowana jako bean przez Springa.
	 */
	public PasswordEncoderConfig() {
	}

	/**
	 * Tworzy domyślny enkoder BCrypt (parametry kosztu zgodne z implementacją Spring Security).
	 *
	 * @return nowa instancja {@link BCryptPasswordEncoder} zarządzana jako singleton bean
	 */
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
