package com.trainit.backend.config;

import com.trainit.backend.security.JwtAuthenticationFilter;
import com.trainit.backend.security.JwtService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

	@Test
	void jwtServiceBean_isCreated() {
		SecurityConfig config = new SecurityConfig();
		JwtService service = config.jwtService("abc", 123);
		assertThat(service).isNotNull();
		assertThat(service.generateToken(1, "a@b.com", "USER")).isNotBlank();
	}

	@Test
	void jwtAuthenticationFilterBean_isCreated() {
		SecurityConfig config = new SecurityConfig();
		JwtAuthenticationFilter filter = config.jwtAuthenticationFilter(new JwtService("abc", 123));
		assertThat(filter).isNotNull();
	}
}
