package com.trainit.backend.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

	@Test
	void generateAndParseToken_roundTripWorks() {
		JwtService service = new JwtService("test-secret-key", 3600);

		String token = service.generateToken(7, "jan@example.com", "USER");
		JwtPrincipal principal = service.parseToken(token);

		assertThat(token).contains(".");
		assertThat(principal.userId()).isEqualTo(7);
		assertThat(principal.email()).isEqualTo("jan@example.com");
		assertThat(principal.role()).isEqualTo("USER");
	}

	@Test
	void parseToken_invalidFormatThrows() {
		JwtService service = new JwtService("test-secret-key", 3600);
		assertThatThrownBy(() -> service.parseToken("broken-token"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("format");
	}

	@Test
	void parseToken_invalidSignatureThrows() {
		JwtService service = new JwtService("test-secret-key", 3600);
		String token = service.generateToken(1, "a@b.com", "USER");
		String tampered = token.substring(0, token.length() - 1) + "x";

		assertThatThrownBy(() -> service.parseToken(tampered))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("podpis");
	}

	@Test
	void parseToken_expiredTokenThrows() {
		JwtService service = new JwtService("test-secret-key", -1);
		String token = service.generateToken(1, "a@b.com", "USER");

		assertThatThrownBy(() -> service.parseToken(token))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("wygas");
	}

	@Test
	void generateToken_escapesSpecialCharactersInPayload() {
		JwtService service = new JwtService("test-secret-key", 3600);
		String token = service.generateToken(12, "ja\"n@ex\\ample.com", "US\"ER");
		JwtPrincipal principal = service.parseToken(token);

		assertThat(principal.userId()).isEqualTo(12);
		assertThat(principal.email()).isNotBlank();
		assertThat(principal.role()).isNotBlank();
	}
}
