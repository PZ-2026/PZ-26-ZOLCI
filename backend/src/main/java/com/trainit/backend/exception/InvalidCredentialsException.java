package com.trainit.backend.exception;

/**
 * Wyjątek sygnalizujący nieudane logowanie z powodu błędnych poświadczeń lub nieaktywnego konta.
 *
 * <p>Rzucany w {@link com.trainit.backend.service.AuthService#login(com.trainit.backend.dto.LoginRequest)}
 * gdy użytkownik nie istnieje, hasło jest niepoprawne lub {@code isActive} nie jest {@code true}.
 * Domyślny komunikat brzmi: „Nieprawidłowy email lub hasło” (bez ujawniania szczegółu przyczyny).
 *
 * <p>{@link GlobalExceptionHandler} mapuje ten wyjątek na odpowiedź {@code 401 Unauthorized}.
 *
 * @see GlobalExceptionHandler#handleInvalidCredentials(InvalidCredentialsException)
 * @see com.trainit.backend.service.AuthService#login(com.trainit.backend.dto.LoginRequest)
 */
public class InvalidCredentialsException extends RuntimeException {

	/**
	 * Tworzy wyjątek z domyślnym, bezpiecznym komunikatem dla klienta.
	 */
	public InvalidCredentialsException() {
		super("Nieprawidłowy email lub hasło");
	}
}
