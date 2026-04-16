package com.trainit.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO żądania HTTP dla endpointu logowania.
 *
 * <p>Zawiera poświadczenia użytkownika przesłane w treści JSON. Walidacja odbywa się
 * przed wywołaniem {@link com.trainit.backend.service.AuthService#login(LoginRequest)}.
 *
 * @see com.trainit.backend.controller.AuthController#login(LoginRequest)
 */
public class LoginRequest {

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public LoginRequest() {
	}

	/** Adres email konta; wymagany, format zgodny z adnotacją {@code @Email}. */
	@NotBlank(message = "Email jest wymagany")
	@Email(message = "Nieprawidłowy format adresu email")
	private String email;

	/** Hasło w postaci jawnej; porównywane ze skrótem BCrypt po stronie serwisu. */
	@NotBlank(message = "Hasło jest wymagane")
	private String password;

	/**
	 * Zwraca email z żądania logowania.
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
	 * Zwraca hasło z żądania (jawne).
	 *
	 * @return hasło użytkownika
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Ustawia hasło w DTO.
	 *
	 * @param password hasło
	 */
	public void setPassword(String password) {
		this.password = password;
	}
}
