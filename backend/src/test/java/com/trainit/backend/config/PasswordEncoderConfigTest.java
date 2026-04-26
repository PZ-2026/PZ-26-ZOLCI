package com.trainit.backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy jednostkowe konfiguracji {@link PasswordEncoderConfig}.
 *
 * <p>Sprawdzają, że bean enkodera BCrypt jest prawidłowy: tworzy hashe różne od hasła jawnego,
 * pasujące przez metodę {@code matches} oraz zwracające różne wartości dla tego samego hasła
 * (sól losowa).
 */
class PasswordEncoderConfigTest {

	@Test
	@DisplayName("bean nie jest null i jest typu BCryptPasswordEncoder")
	void beanIsNotNull() {
		PasswordEncoderConfig config = new PasswordEncoderConfig();
		BCryptPasswordEncoder encoder = config.passwordEncoder();
		assertThat(encoder).isNotNull();
		assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
	}

	@Test
	@DisplayName("encode zwraca hash różny od hasła jawnego")
	void encode_returnsDifferentValue() {
		BCryptPasswordEncoder encoder = new PasswordEncoderConfig().passwordEncoder();
		String hashed = encoder.encode("Haslo123!");
		assertThat(hashed).isNotEqualTo("Haslo123!");
		assertThat(hashed).startsWith("$2");
	}

	@Test
	@DisplayName("matches zwraca true dla poprawnego hasła i hasha")
	void matches_returnsTrueForCorrectPassword() {
		BCryptPasswordEncoder encoder = new PasswordEncoderConfig().passwordEncoder();
		String hashed = encoder.encode("Haslo123!");
		assertThat(encoder.matches("Haslo123!", hashed)).isTrue();
	}

	@Test
	@DisplayName("matches zwraca false dla błędnego hasła")
	void matches_returnsFalseForWrongPassword() {
		BCryptPasswordEncoder encoder = new PasswordEncoderConfig().passwordEncoder();
		String hashed = encoder.encode("Haslo123!");
		assertThat(encoder.matches("InneHaslo", hashed)).isFalse();
	}

	@Test
	@DisplayName("encode generuje różne hashe dla tego samego hasła (losowa sól)")
	void encode_producesDifferentHashesForSamePassword() {
		BCryptPasswordEncoder encoder = new PasswordEncoderConfig().passwordEncoder();
		String first = encoder.encode("Haslo123!");
		String second = encoder.encode("Haslo123!");
		assertThat(first).isNotEqualTo(second);
	}
}
