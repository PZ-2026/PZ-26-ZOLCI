package com.trainit.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;

/**
 * Punkt wejścia aplikacji backendowej TrainIT opartej o Spring Boot.
 *
 * <p>Uruchamia kontekst Springa, autokonfigurację komponentów oraz osadzony serwer
 * (domyślnie Tomcat). Wyklucza domyślną automatyczną konfigurację z modułu
 * {@code spring-boot-security}, ponieważ na tym etapie projektu nie stosuje się
 * filtrów Spring Security ani {@code UserDetailsService} — uwierzytelnianie odbywa się
 * własną logiką w {@link com.trainit.backend.service.AuthService}.
 *
 * @see com.trainit.backend.controller.AuthController
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SpringBootApplication(exclude = {
		SecurityAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class,
		ServletWebSecurityAutoConfiguration.class
})
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
