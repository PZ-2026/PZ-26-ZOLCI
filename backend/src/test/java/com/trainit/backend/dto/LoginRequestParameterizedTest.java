package com.trainit.backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Parametryzowane testy walidacji DTO {@link LoginRequest}.
 *
 * <p>Pokrywają szeroki zestaw poprawnych i niepoprawnych wartości pól email i password,
 * uzupełniając zakres testów z {@link LoginRequestTest}.
 */
class LoginRequestParameterizedTest {

	private static ValidatorFactory factory;
	private static Validator validator;

	@BeforeAll
	static void initValidator() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@AfterAll
	static void closeValidator() {
		factory.close();
	}

	/**
	 * Buduje poprawne żądanie logowania do dalszej modyfikacji.
	 *
	 * @return żądanie z poprawnymi danymi
	 */
	private static LoginRequest validRequest() {
		LoginRequest req = new LoginRequest();
		req.setEmail("jan@example.com");
		req.setPassword("Haslo123!");
		return req;
	}

	@ParameterizedTest(name = "email '{0}' akceptowany")
	@ValueSource(strings = {
			"a@b.com",
			"jan.kowalski@example.com",
			"jan+tag@example.com",
			"j_an@example.co.uk",
			"123@xyz.io",
			"long.user.name@long-domain.example",
			"a@b.co"
	})
	@DisplayName("poprawne formaty emaili nie generują naruszeń pola email")
	void validEmails_areAccepted(String email) {
		LoginRequest req = validRequest();
		req.setEmail(email);
		Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
		assertThat(violations).as("dla emaila %s", email)
				.noneMatch(v -> v.getPropertyPath().toString().equals("email"));
	}

	@ParameterizedTest(name = "email '{0}' odrzucony")
	@ValueSource(strings = {
			"plaintext",
			"@example.com",
			"foo@",
			"foo@.com",
			"foo bar@example.com"
	})
	@DisplayName("niepoprawne formaty emaili generują naruszenia pola email")
	void invalidEmails_areRejected(String email) {
		LoginRequest req = validRequest();
		req.setEmail(email);
		Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
		assertThat(violations).as("dla emaila %s", email)
				.anyMatch(v -> v.getPropertyPath().toString().equals("email"));
	}

	@ParameterizedTest(name = "blank/null email odrzucony")
	@NullAndEmptySource
	@ValueSource(strings = {"   ", "\t", "\n"})
	@DisplayName("pusty lub białoznakowy email generuje naruszenie")
	void blankEmail_isRejected(String email) {
		LoginRequest req = validRequest();
		req.setEmail(email);
		Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
	}

	@ParameterizedTest(name = "haslo '{0}' akceptowane (login bez min)")
	@ValueSource(strings = {
			"a",
			"123",
			"verylongpassword",
			"!@#$%",
			"PaSsWoRd123",
			"αβγ"
	})
	@DisplayName("dowolne niepuste hasło jest akceptowane na logowaniu (brak min length)")
	void anyNonBlankPassword_isAccepted(String password) {
		LoginRequest req = validRequest();
		req.setPassword(password);
		Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
		assertThat(violations).as("dla hasła %s", password)
				.noneMatch(v -> v.getPropertyPath().toString().equals("password"));
	}

	@ParameterizedTest(name = "blank/null hasło odrzucone")
	@NullAndEmptySource
	@ValueSource(strings = {"   ", "\t"})
	@DisplayName("puste hasło generuje naruszenie")
	void blankPassword_isRejected(String password) {
		LoginRequest req = validRequest();
		req.setPassword(password);
		Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
	}

	@ParameterizedTest(name = "naruszenia dla błędnego email='{0}', password='{1}'")
	@org.junit.jupiter.params.provider.CsvSource({
			"'',     '',      2",
			"bad,    Haslo,   1",
			"a@b.com,'',      1",
			"a@b.com,Haslo,   0"
	})
	@DisplayName("kombinacje pól generują oczekiwaną liczbę naruszeń")
	void combinedScenarios(String email, String password, int expectedCount) {
		LoginRequest req = new LoginRequest();
		req.setEmail(email);
		req.setPassword(password);
		Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
		assertThat(violations).hasSize(expectedCount);
	}
}
