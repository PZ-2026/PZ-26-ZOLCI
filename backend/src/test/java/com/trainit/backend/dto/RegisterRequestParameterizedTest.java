package com.trainit.backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Parametryzowane testy walidacji DTO {@link RegisterRequest}.
 *
 * <p>Pokrywają szeroki zestaw poprawnych i niepoprawnych wartości każdego pola, weryfikują
 * komunikaty błędów Bean Validation (klucze {@code message}) oraz interakcje między polami.
 * Klasa rozszerza zakres testów wyznaczony w {@link RegisterRequestTest}.
 */
class RegisterRequestParameterizedTest {

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
	 * Buduje poprawne żądanie rejestracji do dalszej modyfikacji w testach.
	 *
	 * @return żądanie z wszystkimi polami wypełnionymi poprawnymi wartościami
	 */
	private static RegisterRequest validRequest() {
		RegisterRequest req = new RegisterRequest();
		req.setEmail("jan@example.com");
		req.setPassword("Haslo123!");
		req.setFirstName("Jan");
		req.setLastName("Kowalski");
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
	@DisplayName("poprawne formaty emaili nie generują naruszeń")
	void validEmails_areAccepted(String email) {
		RegisterRequest req = validRequest();
		req.setEmail(email);
		Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
		assertThat(violations).as("dla emaila %s", email)
				.noneMatch(v -> v.getPropertyPath().toString().equals("email"));
	}

	@ParameterizedTest(name = "email '{0}' odrzucony")
	@ValueSource(strings = {
			"plaintext",
			"@example.com",
			"foo@",
			"foo@.com",
			"foo bar@example.com",
			"a..b@example.com"
	})
	@DisplayName("niepoprawne formaty emaili generują naruszenia walidacji")
	void invalidEmails_areRejected(String email) {
		RegisterRequest req = validRequest();
		req.setEmail(email);
		Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
		assertThat(violations).as("dla emaila %s", email)
				.anyMatch(v -> v.getPropertyPath().toString().equals("email"));
	}

	@ParameterizedTest(name = "blank/null email odrzucony")
	@NullAndEmptySource
	@ValueSource(strings = {"   ", "\t", "\n"})
	@DisplayName("pusty lub białoznakowy email generuje naruszenie")
	void blankEmail_isRejected(String email) {
		RegisterRequest req = validRequest();
		req.setEmail(email);
		Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
	}

	@ParameterizedTest(name = "haslo '{0}' akceptowane")
	@ValueSource(strings = {
			"12345678",
			"Haslo123!",
			"verylongpasswordwithmorethanenoughcharacters",
			"!@#$%^&*",
			"PaSsWoRd",
			"αβγδεζηθ"
	})
	@DisplayName("hasła o długości >= 8 są akceptowane")
	void validPasswords_areAccepted(String password) {
		RegisterRequest req = validRequest();
		req.setPassword(password);
		Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
		assertThat(violations).as("dla hasła %s", password)
				.noneMatch(v -> v.getPropertyPath().toString().equals("password"));
	}

	@ParameterizedTest(name = "haslo '{0}' odrzucone (krotkie)")
	@ValueSource(strings = {
			"a",
			"ab",
			"abc",
			"abcd",
			"abcde",
			"abcdef",
			"abcdefg"
	})
	@DisplayName("hasła krótsze niż 8 znaków generują naruszenie")
	void shortPasswords_areRejected(String password) {
		RegisterRequest req = validRequest();
		req.setPassword(password);
		Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
		assertThat(violations).as("dla hasła %s", password)
				.anyMatch(v -> v.getPropertyPath().toString().equals("password"));
	}

	@ParameterizedTest(name = "blank/null hasło odrzucone")
	@NullAndEmptySource
	@DisplayName("puste lub null hasło generuje naruszenie")
	void blankPassword_isRejected(String password) {
		RegisterRequest req = validRequest();
		req.setPassword(password);
		Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
	}

	@ParameterizedTest(name = "imie '{0}' akceptowane")
	@ValueSource(strings = {
			"Jan",
			"Anna",
			"Krzysztof",
			"Łukasz",
			"Żaneta",
			"O'Brien",
			"Jean-Luc"
	})
	@DisplayName("poprawne imiona nie generują naruszeń")
	void validFirstNames_areAccepted(String firstName) {
		RegisterRequest req = validRequest();
		req.setFirstName(firstName);
		Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
		assertThat(violations).as("dla imienia %s", firstName)
				.noneMatch(v -> v.getPropertyPath().toString().equals("firstName"));
	}

	@ParameterizedTest(name = "blank/null imię odrzucone")
	@NullAndEmptySource
	@ValueSource(strings = {"   ", "\t"})
	@DisplayName("puste imiona generują naruszenia")
	void blankFirstName_isRejected(String firstName) {
		RegisterRequest req = validRequest();
		req.setFirstName(firstName);
		Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
	}

	@ParameterizedTest(name = "nazwisko '{0}' akceptowane")
	@ValueSource(strings = {
			"Kowalski",
			"Nowak",
			"Żuczek",
			"Mc'Donald",
			"de la Cruz",
			"Krzysztofik-Kowal"
	})
	@DisplayName("poprawne nazwiska nie generują naruszeń")
	void validLastNames_areAccepted(String lastName) {
		RegisterRequest req = validRequest();
		req.setLastName(lastName);
		Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
		assertThat(violations).as("dla nazwiska %s", lastName)
				.noneMatch(v -> v.getPropertyPath().toString().equals("lastName"));
	}

	@ParameterizedTest(name = "blank/null nazwisko odrzucone")
	@NullAndEmptySource
	@ValueSource(strings = {"   ", "\t"})
	@DisplayName("puste nazwiska generują naruszenia")
	void blankLastName_isRejected(String lastName) {
		RegisterRequest req = validRequest();
		req.setLastName(lastName);
		Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("lastName"));
	}

	@ParameterizedTest(name = "{0} -> min {1} naruszen ({2})")
	@CsvSource({
			"OK, 0, wszystkie pola poprawne",
			"BAD_EMAIL, 1, niepoprawny email",
			"SHORT_PASSWORD, 1, krótkie hasło",
			"BLANK_FIRSTNAME, 1, puste imię",
			"BLANK_LASTNAME, 1, puste nazwisko",
			"ALL_BLANK, 4, wszystkie pola puste"
	})
	@DisplayName("dla różnych zestawów danych liczba naruszeń jest zgodna z oczekiwaniami")
	void violationCount_matchesScenario(String scenario, int expectedMinCount, String description) {
		RegisterRequest req = switch (scenario) {
			case "OK" -> validRequest();
			case "BAD_EMAIL" -> {
				RegisterRequest r = validRequest();
				r.setEmail("nie-email");
				yield r;
			}
			case "SHORT_PASSWORD" -> {
				RegisterRequest r = validRequest();
				r.setPassword("krotkie");
				yield r;
			}
			case "BLANK_FIRSTNAME" -> {
				RegisterRequest r = validRequest();
				r.setFirstName("");
				yield r;
			}
			case "BLANK_LASTNAME" -> {
				RegisterRequest r = validRequest();
				r.setLastName("");
				yield r;
			}
			case "ALL_BLANK" -> {
				RegisterRequest r = new RegisterRequest();
				r.setEmail("");
				r.setPassword("");
				r.setFirstName("");
				r.setLastName("");
				yield r;
			}
			default -> throw new IllegalArgumentException(scenario);
		};

		Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
		assertThat(violations).as(description).hasSizeGreaterThanOrEqualTo(expectedMinCount);
	}

	@ParameterizedTest(name = "komunikat bledu zawiera '{1}'")
	@CsvSource({
			"BAD_EMAIL, Nieprawidłowy",
			"SHORT_PASSWORD, co najmniej 8",
			"BLANK_EMAIL, wymagany"
	})
	@DisplayName("komunikaty błędów są zgodne z polskimi treściami w DTO")
	void errorMessages_areInPolish(String scenario, String expectedFragment) {
		RegisterRequest req = switch (scenario) {
			case "BAD_EMAIL" -> {
				RegisterRequest r = validRequest();
				r.setEmail("nie-email");
				yield r;
			}
			case "SHORT_PASSWORD" -> {
				RegisterRequest r = validRequest();
				r.setPassword("krotkie");
				yield r;
			}
			case "BLANK_EMAIL" -> {
				RegisterRequest r = validRequest();
				r.setEmail("");
				yield r;
			}
			default -> throw new IllegalArgumentException(scenario);
		};

		Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
		assertThat(violations).anyMatch(v -> v.getMessage().contains(expectedFragment));
	}
}
