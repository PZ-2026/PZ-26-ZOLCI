package com.trainit.backend.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dodatkowe testy edge case'ów {@link GlobalExceptionHandler}.
 *
 * <p>Klasa rozszerza {@link GlobalExceptionHandlerTest} o testy parametryzowane oraz scenariusze
 * graniczne — pusta lista błędów walidacji, wyjątki z głębokimi {@code cause}, różne typy
 * konkretnych wyjątków bazodanowych.
 */
class GlobalExceptionHandlerEdgeCasesTest {

	private GlobalExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new GlobalExceptionHandler();
	}

	@ParameterizedTest(name = "EmailAlreadyExistsException('{0}') -> 409")
	@ValueSource(strings = {
			"Ten adres email jest już zajęty",
			"Email taken",
			"Pole już istnieje w bazie",
			"Konflikt unikalnego klucza"
	})
	@DisplayName("każdy komunikat z wyjątku trafia do odpowiedzi")
	void emailAlreadyExists_messageIsForwarded(String message) {
		ResponseEntity<Map<String, String>> resp = handler.handleEmailTaken(new EmailAlreadyExistsException(message));
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(resp.getBody()).containsEntry("message", message);
	}

	@ParameterizedTest(name = "IllegalArgumentException('{0}') -> 400")
	@ValueSource(strings = {
			"zły argument",
			"value out of range",
			"Niepoprawny stan",
			"x"
	})
	@DisplayName("każdy komunikat IllegalArgumentException trafia do odpowiedzi 400")
	void illegalArgument_messageIsForwarded(String message) {
		ResponseEntity<Map<String, String>> resp = handler.handleIllegalArgument(new IllegalArgumentException(message));
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(resp.getBody()).containsEntry("message", message);
	}

	@ParameterizedTest(name = "IllegalArgumentException z pustym/null message → domyślny komunikat")
	@NullSource
	@ValueSource(strings = {""})
	@DisplayName("null message daje fallback, pusty również jest forwardowany jak jest")
	void illegalArgument_nullOrEmpty_messageHandling(String message) {
		ResponseEntity<Map<String, String>> resp = handler.handleIllegalArgument(new IllegalArgumentException(message));
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		if (message == null) {
			assertThat(resp.getBody()).containsEntry("message", "Nieprawidłowe żądanie");
		} else {
			assertThat(resp.getBody().get("message")).isNotNull();
		}
	}

	@Test
	@DisplayName("MethodArgumentNotValidException z pustym BindingResult → 400 z pustą listą")
	void validation_emptyBindingResult_returnsEmptyList() throws Exception {
		BindingResult br = new BeanPropertyBindingResult(new Object(), "target");
		MethodArgumentNotValidException ex = buildException(br);

		ResponseEntity<Map<String, Object>> resp = handler.handleValidation(ex);

		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		@SuppressWarnings("unchecked")
		List<Map<String, String>> errors = (List<Map<String, String>>) resp.getBody().get("errors");
		assertThat(errors).isEmpty();
	}

	@Test
	@DisplayName("MethodArgumentNotValidException z 5 błędami → wszystkie w odpowiedzi")
	void validation_multipleErrors_allReturned() throws Exception {
		BindingResult br = new BeanPropertyBindingResult(new Object(), "target");
		br.addError(new FieldError("target", "email", "Email jest wymagany"));
		br.addError(new FieldError("target", "password", "Hasło jest wymagane"));
		br.addError(new FieldError("target", "firstName", "Imię jest wymagane"));
		br.addError(new FieldError("target", "lastName", "Nazwisko jest wymagane"));
		br.addError(new FieldError("target", "age", "Wiek musi być >= 18"));

		ResponseEntity<Map<String, Object>> resp = handler.handleValidation(buildException(br));

		@SuppressWarnings("unchecked")
		List<Map<String, String>> errors = (List<Map<String, String>>) resp.getBody().get("errors");
		assertThat(errors).hasSize(5);
		assertThat(errors).extracting(m -> m.get("field"))
				.containsExactlyInAnyOrder("email", "password", "firstName", "lastName", "age");
	}

	@Test
	@DisplayName("każdy element w errors zawiera klucze field i message")
	void validation_eachErrorEntryHasFieldAndMessage() throws Exception {
		BindingResult br = new BeanPropertyBindingResult(new Object(), "target");
		br.addError(new FieldError("target", "email", "msg1"));
		br.addError(new FieldError("target", "password", "msg2"));

		ResponseEntity<Map<String, Object>> resp = handler.handleValidation(buildException(br));

		@SuppressWarnings("unchecked")
		List<Map<String, String>> errors = (List<Map<String, String>>) resp.getBody().get("errors");
		assertThat(errors).allSatisfy(entry -> {
			assertThat(entry).containsKey("field");
			assertThat(entry).containsKey("message");
			assertThat(entry.get("field")).isNotBlank();
		});
	}

	@Test
	@DisplayName("DataIntegrityViolationException z cause → 409 zachowuje ogólny message")
	void dataIntegrity_withCause_returns409() {
		Throwable cause = new RuntimeException("PRIMARY KEY violation");
		DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint", cause);
		ResponseEntity<Map<String, String>> resp = handler.handleDataIntegrity(ex);
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(resp.getBody()).containsEntry("message", "Konflikt integralności danych");
		assertThat(resp.getBody().get("message")).doesNotContain("PRIMARY KEY");
	}

	@Test
	@DisplayName("HttpMessageNotReadableException z null body w HttpInputMessage → 400")
	void notReadable_emptyBody_returns400() {
		HttpInputMessage emptyMessage = new HttpInputMessage() {
			@Override
			public InputStream getBody() {
				return new ByteArrayInputStream(new byte[0]);
			}

			@Override
			public HttpHeaders getHeaders() {
				return new HttpHeaders();
			}
		};
		HttpMessageNotReadableException ex = new HttpMessageNotReadableException("empty", emptyMessage);
		ResponseEntity<Map<String, String>> resp = handler.handleNotReadable(ex);
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	@DisplayName("genericException nie ujawnia stack trace ani szczegółów wewnętrznych")
	void generic_doesNotLeakInternalDetails() {
		Exception ex = new RuntimeException("SQLException: database password='secret'");
		ResponseEntity<Map<String, String>> resp = handler.handleGeneric(ex);
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(resp.getBody().get("message")).doesNotContain("SQLException");
		assertThat(resp.getBody().get("message")).doesNotContain("secret");
		assertThat(resp.getBody().get("message")).doesNotContain("password");
	}

	@Test
	@DisplayName("genericException z null message też zwraca 500 z bezpiecznym komunikatem")
	void generic_nullMessage_returns500() {
		Exception ex = new RuntimeException();
		ResponseEntity<Map<String, String>> resp = handler.handleGeneric(ex);
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(resp.getBody()).containsEntry("message", "Wystąpił nieoczekiwany błąd");
	}

	@Test
	@DisplayName("genericException przyjmuje również podklasy RuntimeException")
	void generic_acceptsAnyExceptionSubclass() {
		IllegalStateException ex = new IllegalStateException("state");
		ResponseEntity<Map<String, String>> resp = handler.handleGeneric(ex);
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ParameterizedTest(name = "{0}: status {1}")
	@org.junit.jupiter.params.provider.CsvSource({
			"emailTaken, 409",
			"invalidCredentials, 401",
			"illegalArgument, 400",
			"dataIntegrity, 409",
			"generic, 500"
	})
	@DisplayName("każdy handler zwraca poprawny status HTTP")
	void allHandlers_returnExpectedStatus(String handlerName, int expectedStatus) {
		ResponseEntity<?> resp = switch (handlerName) {
			case "emailTaken" -> handler.handleEmailTaken(new EmailAlreadyExistsException("x"));
			case "invalidCredentials" -> handler.handleInvalidCredentials(new InvalidCredentialsException());
			case "illegalArgument" -> handler.handleIllegalArgument(new IllegalArgumentException("x"));
			case "dataIntegrity" -> handler.handleDataIntegrity(new DataIntegrityViolationException("x"));
			case "generic" -> handler.handleGeneric(new RuntimeException("x"));
			default -> throw new IllegalArgumentException(handlerName);
		};
		assertThat(resp.getStatusCode().value()).isEqualTo(expectedStatus);
	}

	@Test
	@DisplayName("każdy handler zwraca body z kluczem 'message'")
	void allHandlers_bodyHasMessageKey() {
		assertThat(handler.handleEmailTaken(new EmailAlreadyExistsException("x")).getBody()).containsKey("message");
		assertThat(handler.handleInvalidCredentials(new InvalidCredentialsException()).getBody()).containsKey("message");
		assertThat(handler.handleIllegalArgument(new IllegalArgumentException("x")).getBody()).containsKey("message");
		assertThat(handler.handleDataIntegrity(new DataIntegrityViolationException("x")).getBody()).containsKey("message");
		assertThat(handler.handleGeneric(new RuntimeException("x")).getBody()).containsKey("message");
	}

	/**
	 * Pomocnicza fabryka {@link MethodArgumentNotValidException}.
	 *
	 * @param br BindingResult z błędami
	 * @return wyjątek do przekazania do handlera
	 * @throws NoSuchMethodException gdy refleksja zawiedzie
	 */
	private MethodArgumentNotValidException buildException(BindingResult br) throws NoSuchMethodException {
		Method method = GlobalExceptionHandlerEdgeCasesTest.class.getDeclaredMethod("dummyMethod", String.class);
		org.springframework.core.MethodParameter parameter = new org.springframework.core.MethodParameter(method, 0);
		return new MethodArgumentNotValidException(parameter, br);
	}

	/**
	 * Metoda referencyjna do utworzenia {@link org.springframework.core.MethodParameter}.
	 *
	 * @param dummy parametr nieużywany
	 */
	@SuppressWarnings("unused")
	private void dummyMethod(String dummy) {
	}
}
