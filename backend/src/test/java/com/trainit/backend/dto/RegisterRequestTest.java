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
 * Testy walidacji oraz akcesorów DTO {@link RegisterRequest}.
 *
 * <p>Pokrywają wszystkie reguły walidacji Bean Validation: {@code @NotBlank} dla email/firstName/lastName,
 * {@code @Email} dla formatu, {@code @Size(min = 8)} dla hasła oraz akcesory wszystkich pól.
 *
 * @see RegisterRequest
 */
class RegisterRequestTest {

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
	 * @return wypełniony {@link RegisterRequest}
	 */
	private static RegisterRequest validRequest() {
		RegisterRequest req = new RegisterRequest();
		req.setEmail("jan@example.com");
		req.setPassword("Haslo123!");
		req.setFirstName("Jan");
		req.setLastName("Kowalski");
		return req;
	}

	@Test
	@DisplayName("poprawne żądanie nie generuje naruszeń walidacji")
	void valid_noViolations() {
		assertThat(validator.validate(validRequest())).isEmpty();
	}

	@Test
	@DisplayName("blank email generuje naruszenie")
	void blankEmail_violation() {
		RegisterRequest req = validRequest();
		req.setEmail("");
		assertThat(validator.validate(req))
				.anyMatch(v -> v.getPropertyPath().toString().equals("email"));
	}

	@Test
	@DisplayName("null email generuje naruszenie")
	void nullEmail_violation() {
		RegisterRequest req = validRequest();
		req.setEmail(null);
		assertThat(validator.validate(req))
				.anyMatch(v -> v.getPropertyPath().toString().equals("email"));
	}

	@Test
	@DisplayName("niepoprawny format emaila generuje naruszenie")
	void invalidEmailFormat_violation() {
		RegisterRequest req = validRequest();
		req.setEmail("nie-email");
		assertThat(validator.validate(req))
				.anyMatch(v -> v.getPropertyPath().toString().equals("email"));
	}

	@Test
	@DisplayName("blank hasło generuje naruszenie")
	void blankPassword_violation() {
		RegisterRequest req = validRequest();
		req.setPassword("");
		assertThat(validator.validate(req))
				.anyMatch(v -> v.getPropertyPath().toString().equals("password"));
	}

	@Test
	@DisplayName("hasło krótsze niż 8 znaków generuje naruszenie")
	void shortPassword_violation() {
		RegisterRequest req = validRequest();
		req.setPassword("krotkie");
		assertThat(validator.validate(req))
				.anyMatch(v -> v.getPropertyPath().toString().equals("password"));
	}

	@Test
	@DisplayName("hasło dokładnie 8 znaków jest akceptowane")
	void password8chars_isValid() {
		RegisterRequest req = validRequest();
		req.setPassword("12345678");
		assertThat(validator.validate(req)).isEmpty();
	}

	@Test
	@DisplayName("blank firstName generuje naruszenie")
	void blankFirstName_violation() {
		RegisterRequest req = validRequest();
		req.setFirstName("");
		assertThat(validator.validate(req))
				.anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
	}

	@Test
	@DisplayName("null firstName generuje naruszenie")
	void nullFirstName_violation() {
		RegisterRequest req = validRequest();
		req.setFirstName(null);
		assertThat(validator.validate(req))
				.anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
	}

	@Test
	@DisplayName("blank lastName generuje naruszenie")
	void blankLastName_violation() {
		RegisterRequest req = validRequest();
		req.setLastName("");
		assertThat(validator.validate(req))
				.anyMatch(v -> v.getPropertyPath().toString().equals("lastName"));
	}

	@Test
	@DisplayName("null lastName generuje naruszenie")
	void nullLastName_violation() {
		RegisterRequest req = validRequest();
		req.setLastName(null);
		assertThat(validator.validate(req))
				.anyMatch(v -> v.getPropertyPath().toString().equals("lastName"));
	}

	@Test
	@DisplayName("wszystkie pola puste — generują 4 naruszenia")
	void allBlank_generatesViolations() {
		RegisterRequest req = new RegisterRequest();
		req.setEmail("");
		req.setPassword("");
		req.setFirstName("");
		req.setLastName("");
		Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
		assertThat(violations).hasSizeGreaterThanOrEqualTo(4);
	}

	@Test
	@DisplayName("getter/setter email zachowuje wartość")
	void emailAccessor() {
		RegisterRequest req = new RegisterRequest();
		req.setEmail("a@b.com");
		assertThat(req.getEmail()).isEqualTo("a@b.com");
	}

	@Test
	@DisplayName("getter/setter password zachowuje wartość")
	void passwordAccessor() {
		RegisterRequest req = new RegisterRequest();
		req.setPassword("secret123");
		assertThat(req.getPassword()).isEqualTo("secret123");
	}

	@Test
	@DisplayName("getter/setter firstName zachowuje wartość")
	void firstNameAccessor() {
		RegisterRequest req = new RegisterRequest();
		req.setFirstName("Jan");
		assertThat(req.getFirstName()).isEqualTo("Jan");
	}

	@Test
	@DisplayName("getter/setter lastName zachowuje wartość")
	void lastNameAccessor() {
		RegisterRequest req = new RegisterRequest();
		req.setLastName("Kowalski");
		assertThat(req.getLastName()).isEqualTo("Kowalski");
	}

	@Test
	@DisplayName("nowy obiekt ma null we wszystkich polach")
	void newRequest_hasNullFields() {
		RegisterRequest req = new RegisterRequest();
		assertThat(req.getEmail()).isNull();
		assertThat(req.getPassword()).isNull();
		assertThat(req.getFirstName()).isNull();
		assertThat(req.getLastName()).isNull();
	}
}
