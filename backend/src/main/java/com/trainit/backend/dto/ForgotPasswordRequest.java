package com.trainit.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO żądania HTTP do zmiany hasła na podstawie adresu email.
 *
 * <p>Zawiera email konta oraz nowe hasło w postaci jawnej. Walidacja Bean Validation
 * sprawdza poprawność formatu email i minimalną długość hasła przed wywołaniem serwisu.
 *
 * @see com.trainit.backend.controller.AuthController
 */
public class ForgotPasswordRequest {

	/** Adres email konta, którego hasło ma zostać zmienione; wymagany, format zgodny z {@code @Email}. */
	@NotBlank(message = "Email jest wymagany")
	@Email(message = "Nieprawidłowy format adresu email")
	private String email;

	/** Nowe hasło w postaci jawnej; min. 8 znaków; przed zapisem hashowane BCrypt. */
	@NotBlank(message = "Nowe hasło jest wymagane")
	@Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków")
	private String newPassword;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public ForgotPasswordRequest() {
	}

	/**
	 * Zwraca email konta, dla którego resetowane jest hasło.
	 *
	 * @return adres email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Ustawia email w DTO.
	 *
	 * @param email adres email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Zwraca nowe hasło z żądania (jawne; nie zwracać w logach produkcyjnych).
	 *
	 * @return nowe hasło przed zahashowaniem
	 */
	public String getNewPassword() {
		return newPassword;
	}

	/**
	 * Ustawia nowe hasło w DTO.
	 *
	 * @param newPassword nowe hasło użytkownika
	 */
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}
