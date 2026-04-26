package com.trainit.backend.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy jednostkowe wyjątku biznesowego {@link EmailAlreadyExistsException}.
 *
 * <p>Wyjątek musi przekazywać komunikat dalej do {@link RuntimeException#getMessage()},
 * gdyż jest on serializowany do odpowiedzi HTTP {@code 409 Conflict} przez globalny handler.
 */
class EmailAlreadyExistsExceptionTest {

	@Test
	@DisplayName("przekazuje komunikat z konstruktora do getMessage")
	void messagePassedThrough() {
		EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Email zajęty");
		assertThat(ex.getMessage()).isEqualTo("Email zajęty");
	}

	@Test
	@DisplayName("dziedziczy po RuntimeException")
	void isRuntimeException() {
		EmailAlreadyExistsException ex = new EmailAlreadyExistsException("x");
		assertThat(ex).isInstanceOf(RuntimeException.class);
	}

	@Test
	@DisplayName("akceptuje pusty komunikat")
	void emptyMessageAllowed() {
		EmailAlreadyExistsException ex = new EmailAlreadyExistsException("");
		assertThat(ex.getMessage()).isEmpty();
	}

	@Test
	@DisplayName("akceptuje null jako komunikat")
	void nullMessageAllowed() {
		EmailAlreadyExistsException ex = new EmailAlreadyExistsException(null);
		assertThat(ex.getMessage()).isNull();
	}
}
