package com.trainit.backend.exception;

/**
 * Wyjątek biznesowy sygnalizujący próbę rejestracji na adres email już zajęty w bazie.
 *
 * <p>Rzucany w {@link com.trainit.backend.service.AuthService#register(com.trainit.backend.dto.RegisterRequest)}
 * po wykryciu duplikatu. Obsługiwany przez {@link GlobalExceptionHandler} i mapowany na odpowiedź
 * HTTP {@code 409 Conflict} z komunikatem przekazanym w konstruktorze.
 *
 * @see GlobalExceptionHandler#handleEmailTaken(EmailAlreadyExistsException)
 * @see com.trainit.backend.service.AuthService
 */
public class EmailAlreadyExistsException extends RuntimeException {

	/**
	 * Tworzy wyjątek z komunikatem dla klienta API.
	 *
	 * @param message treść błędu (np. po polsku), trafia do pola {@code message} w JSON
	 */
	public EmailAlreadyExistsException(String message) {
		super(message);
	}
}
