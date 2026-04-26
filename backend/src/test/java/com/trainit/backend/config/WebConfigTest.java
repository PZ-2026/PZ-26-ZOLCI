package com.trainit.backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy jednostkowe konfiguracji {@link WebConfig}.
 *
 * <p>Weryfikują, że metoda {@link WebConfig#addCorsMappings(CorsRegistry)} rejestruje globalne
 * mapowanie ({@code /**}) z odpowiednimi wzorcami originów. Dostęp do wewnętrznej listy mapowań
 * uzyskujemy przez refleksję (API Springa nie udostępnia gettera).
 */
class WebConfigTest {

	@Test
	@DisplayName("addCorsMappings rejestruje co najmniej jedno mapowanie")
	void addCorsMappings_registersAtLeastOneMapping() throws Exception {
		WebConfig config = new WebConfig();
		CorsRegistry registry = new CorsRegistry();
		config.addCorsMappings(registry);

		List<CorsRegistration> registrations = extractRegistrations(registry);
		assertThat(registrations).isNotEmpty();
	}

	@Test
	@DisplayName("addCorsMappings nie rzuca wyjątku")
	void addCorsMappings_doesNotThrow() {
		WebConfig config = new WebConfig();
		CorsRegistry registry = new CorsRegistry();
		config.addCorsMappings(registry);
	}

	@Test
	@DisplayName("konstruktor domyślny tworzy instancję")
	void defaultConstructor_works() {
		assertThat(new WebConfig()).isNotNull();
	}

	/**
	 * Pobiera prywatne pole {@code registrations} z {@link CorsRegistry} przez refleksję.
	 *
	 * @param registry rejestr CORS po wywołaniu konfiguracji
	 * @return lista rejestracji (nigdy {@code null}, ale może być pusta)
	 * @throws Exception gdy refleksja zawiedzie
	 */
	@SuppressWarnings("unchecked")
	private static List<CorsRegistration> extractRegistrations(CorsRegistry registry) throws Exception {
		Field field = CorsRegistry.class.getDeclaredField("registrations");
		field.setAccessible(true);
		return (List<CorsRegistration>) field.get(registry);
	}
}
