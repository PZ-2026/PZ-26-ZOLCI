package com.trainit.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Globalny handler wyjątków dla warstwy REST.
 *
 * <p>Mapuje wyjątki aplikacyjne i techniczne na odpowiedzi HTTP z jednolitym formatem
 * (pole {@code message} lub struktura z listą błędów pól). Dzięki adnotacji
 * {@link RestControllerAdvice} metody {@link ExceptionHandler} obejmują wszystkie kontrolery
 * bez powielania kodu w każdej klasie.
 *
 * <p>Statusy m.in.: {@code 400} (walidacja, zły JSON), {@code 401} ({@link InvalidCredentialsException}),
 * {@code 409} (konflikt emaila, integralność), {@code 500} (nieobsłużone błędy z logowaniem po stronie serwera).
 *
 * @see EmailAlreadyExistsException
 * @see InvalidCredentialsException
 * @see org.springframework.web.bind.annotation.RestControllerAdvice
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Konstruktor domyślny; bean tworzony przez kontener Springa.
	 */
	public GlobalExceptionHandler() {
	}

	/** Logger do rejestrowania nieoczekiwanych błędów serwera. */
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/** Klucz JSON dla głównego komunikatu tekstowego błędu. */
	private static final String MESSAGE_KEY = "message";

	/** Klucz JSON dla listy szczegółowych błędów walidacji pól. */
	private static final String ERRORS_KEY = "errors";

	/**
	 * Obsługuje brak możliwości odczytania ciała żądania jako JSON (np. niepoprawna składnia).
	 *
	 * @param ex wyjątek Spring MVC z informacją o problemie z parsowaniem
	 * @return odpowiedź {@code 400 Bad Request} z krótkim komunikatem po polsku
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, String>> handleNotReadable(HttpMessageNotReadableException ex) {
		return ResponseEntity.badRequest().body(Map.of(MESSAGE_KEY, "Nieprawidłowy format JSON"));
	}

	/**
	 * Obsługuje błędy walidacji Bean Validation po adnotacji {@code @Valid} na parametrach kontrolera.
	 *
	 * <p>Zwraca listę obiektów z polami {@code field} i {@code message} dla każdego błędu wiązania.
	 *
	 * @param ex wyjątek zawierający {@link org.springframework.validation.BindingResult}
	 * @return odpowiedź {@code 400} z mapą zawierającą {@value #MESSAGE_KEY} oraz {@value #ERRORS_KEY}
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> {
					Map<String, String> err = new HashMap<>();
					err.put("field", fe.getField());
					err.put("message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "");
					return err;
				})
				.collect(Collectors.toList());
		Map<String, Object> body = new HashMap<>();
		body.put(MESSAGE_KEY, "Błąd walidacji");
		body.put(ERRORS_KEY, fieldErrors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	/**
	 * Obsługuje naruszenia integralności danych po stronie bazy (np. unikalność, klucze obce).
	 *
	 * @param ex wyjątek Spring Data / JDBC opakowujący błąd bazy
	 * @return odpowiedź {@code 409 Conflict} z ogólnym komunikatem
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(MESSAGE_KEY, "Konflikt integralności danych"));
	}

	/**
	 * Obsługuje błędy argumentów oznaczone jako {@link IllegalArgumentException}.
	 *
	 * @param ex wyjątek z opcjonalnym komunikatem
	 * @return odpowiedź {@code 400} z treścią {@code message} z wyjątku lub domyślną
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
		String msg = ex.getMessage() != null ? ex.getMessage() : "Nieprawidłowe żądanie";
		return ResponseEntity.badRequest().body(Map.of(MESSAGE_KEY, msg));
	}

	/**
	 * Obsługuje próbę rejestracji na zajęty email.
	 *
	 * @param ex wyjątek z komunikatem biznesowym
	 * @return odpowiedź {@code 409 Conflict} z {@link EmailAlreadyExistsException#getMessage()}
	 */
	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<Map<String, String>> handleEmailTaken(EmailAlreadyExistsException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(MESSAGE_KEY, ex.getMessage()));
	}

	/**
	 * Obsługuje nieudane logowanie (błędne dane lub konto nieaktywne).
	 *
	 * @param ex wyjątek z bezpiecznym komunikatem dla klienta
	 * @return odpowiedź {@code 401 Unauthorized}
	 */
	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(MESSAGE_KEY, ex.getMessage()));
	}

	/**
	 * Obsługuje dowolny inny, nieprzewidziany wyjątek w warstwie REST.
	 *
	 * <p>Loguje pełny stack trace po poziomie ERROR i zwraca ogólny komunikat bez ujawniania szczegółów wewnętrznych.
	 *
	 * @param ex dowolny wyjątek nieobsłużony wyżej
	 * @return odpowiedź {@code 500 Internal Server Error}
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
		log.error("Unexpected error", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of(MESSAGE_KEY, "Wystąpił nieoczekiwany błąd"));
	}
}
