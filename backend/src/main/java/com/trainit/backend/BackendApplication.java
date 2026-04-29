package com.trainit.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punkt wejścia aplikacji backendowej TrainIT opartej o Spring Boot.
 *
 * <p>Uruchamia kontekst Springa, autokonfigurację komponentów oraz osadzony serwer
 * (domyślnie Tomcat) wraz z pełną konfiguracją zabezpieczeń Spring Security.
 *
 * @see com.trainit.backend.controller.AuthController
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SpringBootApplication
public class BackendApplication {

	/**
	 * Konstruktor domyślny używany przez konwencję Spring Boot i narzędzia refleksji.
	 */
	public BackendApplication() {
	}

	/**
	 * Uruchamia aplikację Spring Boot w procesie JVM.
	 *
	 * <p>Przekazuje argumenty linii poleceń do kontekstu (np. profile, właściwości).
	 *
	 * @param args argumenty przekazane z linii poleceń; mogą być puste
	 */
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
