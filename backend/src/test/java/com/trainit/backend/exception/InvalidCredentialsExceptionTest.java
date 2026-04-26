package com.trainit.backend.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy jednostkowe wyjątku {@link InvalidCredentialsException}.
 *
 * <p>Wyjątek nie powinien ujawniać szczegółu (czy zły email, czy złe hasło) — sprawdzamy że
 * komunikat jest spójny z bezpieczną treścią używaną przez {@code GlobalExceptionHandler}.
 */
class InvalidCredentialsExceptionTest {

	@Test
	@DisplayName("domyślny komunikat to bezpieczny tekst po polsku")
	void defaultMessageIsSafe() {
		InvalidCredentialsException ex = new InvalidCredentialsException();
		assertThat(ex.getMessage()).isEqualTo("Nieprawidłowy email lub hasło");
	}

	@Test
	@DisplayName("dziedziczy po RuntimeException")
	void isRuntimeException() {
		assertThat(new InvalidCredentialsException()).isInstanceOf(RuntimeException.class);
	}

	@Test
	@DisplayName("każda instancja ma identyczny komunikat")
	void messagesAreEqualBetweenInstances() {
		InvalidCredentialsException a = new InvalidCredentialsException();
		InvalidCredentialsException b = new InvalidCredentialsException();
		assertThat(a.getMessage()).isEqualTo(b.getMessage());
	}
}
