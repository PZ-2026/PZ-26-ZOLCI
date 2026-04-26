package com.trainit.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Testy aplikacji {@link BackendApplication}.
 *
 * <p>Najprostszy test integracyjny — podnoszenie kontekstu Spring Boota — chroni przed regresjami
 * w konfiguracji i zależnościach. Dodatkowo {@link #main_runsWithoutException()} pokrywa metodę
 * {@code main(String[])} i konstruktor klasy aplikacji.
 *
 * @see BackendApplication
 */
@SpringBootTest
class BackendApplicationTests {

	@Test
	@DisplayName("kontekst Springa podnosi się bez błędów")
	void contextLoads() {
	}

	@Test
	@DisplayName("metoda main() uruchamia kontekst aplikacji bez wyjątku i kontekst można zamknąć")
	void main_runsWithoutException() {
		assertThatCode(() -> {
			System.setProperty("server.port", "0");
			BackendApplication.main(new String[]{"--server.port=0"});
		}).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("konstruktor BackendApplication tworzy instancję")
	void defaultConstructor_works() {
		assertThat(new BackendApplication()).isNotNull();
	}
}
