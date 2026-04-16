package com.trainit.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Konfiguracja Spring MVC dla aplikacji webowej.
 *
 * <p>Rejestruje reguły CORS, aby klienty (np. aplikacja Android z emulatora lub przeglądarka)
 * mogły wywoływać API z innego originu w środowisku developerskim. Mapowanie obejmuje
 * wszystkie ścieżki ({@code /**}), dozwolone są wszystkie metody HTTP i nagłówki;
 * wzorce originów obejmują localhost z dowolnym portem oraz wzorzec ogólny {@code *}.
 *
 * <p>W produkcji zestaw dozwolonych originów należy zwęzić do zaufanych domen.
 *
 * @see WebMvcConfigurer#addCorsMappings(CorsRegistry)
 * @see org.springframework.web.servlet.config.annotation.CorsRegistry
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	/**
	 * Konstruktor domyślny; konfiguracja rejestrowana jako bean przez Springa.
	 */
	public WebConfig() {
	}

	/**
	 * Dodaje globalne mapowanie CORS do rejestru Spring MVC.
	 *
	 * <p>Umożliwia żądania cross-origin zgodnie z ustawionymi wzorcami originów i dozwolonymi metodami.
	 *
	 * @param registry rejestr CORS dostarczony przez framework; nie może być {@code null}
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOriginPatterns("http://localhost:*", "*")
				.allowedMethods("*")
				.allowedHeaders("*");
	}
}
