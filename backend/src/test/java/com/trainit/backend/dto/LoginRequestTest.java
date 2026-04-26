package com.trainit.backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy walidacji oraz akcesorów DTO {@link LoginRequest}.
 *
 * <p>Walidacja jest realizowana przez Bean Validation (Hibernate Validator). Każdy test
 * tworzy instancję żądania, uruchamia walidator i sprawdza obecność lub brak naruszeń.
 *
 * @see LoginRequest
 */
class LoginRequestTest {

	private static ValidatorFactory factory;
	private static Validator validator;

	/**
	 * Inicjalizuje fabrykę walidatora raz dla całej klasy testów.
	 */
	@BeforeAll
	static void initValidator() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	/**
	 * Zamyka fabrykę walidatora po wszystkich testach.
	 */
	@AfterAll
	static void closeValidator() {
		factory.close();
	}

	/**
	 * Buduje poprawne żądanie logowania do dalszej modyfikacji w testach.
	 *
	 * @return żądanie z poprawnym emailem i hasłem
	 */
	private static LoginRequest validRequest() {
		LoginRequest req = new LoginRequest();
		req.setEmail("jan@example.com");
		req.setPassword("Haslo123!");
		return req;
	}

	@Test
	@DisplayName("poprawne żądanie nie generuje naruszeń walidacji")
	void valid_noViolations() {
		Set<ConstraintViolation<LoginRequest>> violations = validator.validate(validRequest());
		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("blank email generuje naruszenie")
	void blankEmail_violation() {
		LoginRequest req = validRequest();
		req.setEmail("");
		Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
		assertThat(violations).isNotEmpty();
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
	}

	@Test
	@DisplayName("null email generuje naruszenie")
	void nullEmail_violation() {
		LoginRequest req = validRequest();
		req.setEmail(null);
		Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
	}

	@Test
	@DisplayName("niepoprawny format emaila generuje naruszenie")
	void invalidEmailFormat_violation() {
		LoginRequest req = validRequest();
		req.setEmail("nie-email");
		Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
	}

	@Test
	@DisplayName("blank hasło generuje naruszenie")
	void blankPassword_violation() {
		LoginRequest req = validRequest();
		req.setPassword("");
		Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
	}

	@Test
	@DisplayName("null hasło generuje naruszenie")
	void nullPassword_violation() {
		LoginRequest req = validRequest();
		req.setPassword(null);
		Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
	}

	@Test
	@DisplayName("getter/setter email zachowuje wartość")
	void emailAccessor() {
		LoginRequest req = new LoginRequest();
		req.setEmail("a@b.com");
		assertThat(req.getEmail()).isEqualTo("a@b.com");
	}

	@Test
	@DisplayName("getter/setter password zachowuje wartość")
	void passwordAccessor() {
		LoginRequest req = new LoginRequest();
		req.setPassword("secret");
		assertThat(req.getPassword()).isEqualTo("secret");
	}

	@Test
	@DisplayName("nowy obiekt ma null email i password")
	void defaultValues_areNull() {
		LoginRequest req = new LoginRequest();
		assertThat(req.getEmail()).isNull();
		assertThat(req.getPassword()).isNull();
	}
}
