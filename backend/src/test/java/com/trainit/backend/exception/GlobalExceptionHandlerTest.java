package com.trainit.backend.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
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
 * Testy jednostkowe handlera {@link GlobalExceptionHandler}.
 *
 * <p>Każda metoda handlera jest wywoływana bezpośrednio (bez kontekstu MVC) i sprawdzana pod kątem
 * statusu HTTP, struktury ciała oraz zawartości komunikatu. Dla {@code MethodArgumentNotValidException}
 * konstruowany jest sztuczny {@link BindingResult} z dwoma błędami pól.
 */
class GlobalExceptionHandlerTest {

	private GlobalExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new GlobalExceptionHandler();
	}

	@Test
	@DisplayName("HttpMessageNotReadableException → 400 z komunikatem o JSON")
	void notReadable_returns400() {
		HttpInputMessage inputMessage = new HttpInputMessage() {
			@Override
			public InputStream getBody() {
				return new ByteArrayInputStream(new byte[0]);
			}

			@Override
			public org.springframework.http.HttpHeaders getHeaders() {
				return new org.springframework.http.HttpHeaders();
			}
		};
		HttpMessageNotReadableException ex = new HttpMessageNotReadableException("zły JSON", inputMessage);
		ResponseEntity<Map<String, String>> response = handler.handleNotReadable(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).containsEntry("message", "Nieprawidłowy format JSON");
	}

	@Test
	@DisplayName("MethodArgumentNotValidException → 400 z listą błędów pól")
	void validation_returns400WithFieldErrors() throws Exception {
		BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
		bindingResult.addError(new FieldError("target", "email", "Email jest wymagany"));
		bindingResult.addError(new FieldError("target", "password", "Hasło jest wymagane"));

		MethodArgumentNotValidException ex = buildMethodArgumentNotValidException(bindingResult);

		ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).containsKey("message").containsKey("errors");
		assertThat(response.getBody().get("message")).isEqualTo("Błąd walidacji");

		Object errors = response.getBody().get("errors");
		assertThat(errors).isInstanceOf(List.class);
		@SuppressWarnings("unchecked")
		List<Map<String, String>> errorList = (List<Map<String, String>>) errors;
		assertThat(errorList).hasSize(2);
		assertThat(errorList).anyMatch(m -> "email".equals(m.get("field")));
		assertThat(errorList).anyMatch(m -> "password".equals(m.get("field")));
	}

	@Test
	@DisplayName("MethodArgumentNotValidException → null defaultMessage zamieniany na pusty string")
	void validation_nullDefaultMessage_becomesEmpty() throws Exception {
		BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
		bindingResult.addError(new FieldError("target", "email", null, false, null, null, null));

		MethodArgumentNotValidException ex = buildMethodArgumentNotValidException(bindingResult);
		ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

		@SuppressWarnings("unchecked")
		List<Map<String, String>> errors = (List<Map<String, String>>) response.getBody().get("errors");
		assertThat(errors.get(0).get("message")).isEmpty();
	}

	@Test
	@DisplayName("DataIntegrityViolationException → 409 Conflict z ogólnym komunikatem")
	void dataIntegrity_returns409() {
		DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint");
		ResponseEntity<Map<String, String>> response = handler.handleDataIntegrity(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).containsEntry("message", "Konflikt integralności danych");
	}

	@Test
	@DisplayName("IllegalArgumentException → 400 z komunikatem z wyjątku")
	void illegalArgument_returns400WithMessage() {
		IllegalArgumentException ex = new IllegalArgumentException("zły argument");
		ResponseEntity<Map<String, String>> response = handler.handleIllegalArgument(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).containsEntry("message", "zły argument");
	}

	@Test
	@DisplayName("IllegalArgumentException z null wiadomością → 400 z domyślnym komunikatem")
	void illegalArgument_nullMessage_returns400WithDefault() {
		IllegalArgumentException ex = new IllegalArgumentException();
		ResponseEntity<Map<String, String>> response = handler.handleIllegalArgument(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).containsEntry("message", "Nieprawidłowe żądanie");
	}

	@Test
	@DisplayName("EmailAlreadyExistsException → 409 z komunikatem z wyjątku")
	void emailTaken_returns409() {
		EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Email już istnieje");
		ResponseEntity<Map<String, String>> response = handler.handleEmailTaken(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).containsEntry("message", "Email już istnieje");
	}

	@Test
	@DisplayName("InvalidCredentialsException → 401 z domyślnym komunikatem")
	void invalidCredentials_returns401() {
		InvalidCredentialsException ex = new InvalidCredentialsException();
		ResponseEntity<Map<String, String>> response = handler.handleInvalidCredentials(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody()).containsEntry("message", "Nieprawidłowy email lub hasło");
	}

	@Test
	@DisplayName("dowolny inny Exception → 500 z ogólnym komunikatem (bez ujawniania szczegółów)")
	void genericException_returns500() {
		Exception ex = new RuntimeException("internal stacktrace");
		ResponseEntity<Map<String, String>> response = handler.handleGeneric(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).containsEntry("message", "Wystąpił nieoczekiwany błąd");
		assertThat(response.getBody().get("message")).doesNotContain("internal stacktrace");
	}

	/**
	 * Buduje obiekt {@link MethodArgumentNotValidException} korzystając z dowolnej istniejącej metody
	 * jako {@code MethodParameter} (Spring wymaga {@code MethodParameter} w konstruktorze).
	 *
	 * @param bindingResult przygotowany {@link BindingResult} z błędami
	 * @return wyjątek gotowy do przekazania do handlera
	 * @throws NoSuchMethodException gdy metoda referencyjna nie istnieje
	 */
	private MethodArgumentNotValidException buildMethodArgumentNotValidException(BindingResult bindingResult)
			throws NoSuchMethodException {
		Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyMethodForReflection", String.class);
		org.springframework.core.MethodParameter parameter = new org.springframework.core.MethodParameter(method, 0);
		return new MethodArgumentNotValidException(parameter, bindingResult);
	}

	/**
	 * Metoda referencyjna używana wyłącznie do utworzenia {@link org.springframework.core.MethodParameter}
	 * w teście walidacji.
	 *
	 * @param dummy parametr stringowy nieużywany
	 */
	@SuppressWarnings("unused")
	private void dummyMethodForReflection(String dummy) {
	}
}
