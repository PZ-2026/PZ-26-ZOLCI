package com.trainit.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO żądania HTTP dla endpointu rejestracji użytkownika.
 *
 * <p>Pola są walidowane adnotacjami Bean Validation przed wejściem do
 * {@link com.trainit.backend.service.AuthService#register(RegisterRequest)}.
 * Błędy walidacji są mapowane na odpowiedź {@code 400} przez
 * {@link com.trainit.backend.exception.GlobalExceptionHandler}.
 *
 * @see com.trainit.backend.controller.AuthController#register(RegisterRequest)
 */
public class RegisterRequest {

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public RegisterRequest() {
	}

	/** Adres email nowego konta; wymagany, poprawny format email. */
	@NotBlank(message = "Email jest wymagany")
	@Email(message = "Nieprawidłowy format adresu email")
	private String email;

	/** Hasło w postaci jawnej z żądania; min. 8 znaków; przed zapisem hashowane BCrypt. */
	@NotBlank(message = "Hasło jest wymagane")
	@Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków")
	private String password;

	/** Imię użytkownika; wymagane, niepuste po stronie walidacji. */
	@NotBlank(message = "Imię jest wymagane")
	private String firstName;

	/** Nazwisko użytkownika; wymagane, niepuste po stronie walidacji. */
	@NotBlank(message = "Nazwisko jest wymagane")
	private String lastName;

	/**
	 * Zwraca email przekazany w żądaniu rejestracji.
	 *
	 * @return adres email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Ustawia email w DTO (deserializacja JSON).
	 *
	 * @param email adres email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Zwraca hasło z żądania (jawne; nie zwracać w logach produkcyjnych).
	 *
	 * @return hasło przed zahashowaniem
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Ustawia hasło w DTO.
	 *
	 * @param password hasło użytkownika
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Zwraca imię z formularza rejestracji.
	 *
	 * @return imię
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Ustawia imię w DTO.
	 *
	 * @param firstName imię
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Zwraca nazwisko z formularza rejestracji.
	 *
	 * @return nazwisko
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Ustawia nazwisko w DTO.
	 *
	 * @param lastName nazwisko
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
