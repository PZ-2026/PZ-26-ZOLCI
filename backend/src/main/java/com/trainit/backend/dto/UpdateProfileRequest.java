package com.trainit.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO żądania HTTP do aktualizacji profilu zalogowanego użytkownika.
 *
 * <p>Zawiera imię, nazwisko, email oraz opcjonalne nowe hasło. Pola wymagane są walidowane
 * adnotacjami Bean Validation; hasło musi mieć co najmniej 8 znaków, gdy zostało podane.
 *
 * @see com.trainit.backend.controller.AuthController
 */
public class UpdateProfileRequest {

	/** Imię użytkownika; wymagane, niepuste po stronie walidacji. */
	@NotBlank(message = "Imię jest wymagane")
	private String firstName;

	/** Nazwisko użytkownika; wymagane, niepuste po stronie walidacji. */
	@NotBlank(message = "Nazwisko jest wymagane")
	private String lastName;

	/** Adres email konta; wymagany, format zgodny z adnotacją {@code @Email}. */
	@NotBlank(message = "Email jest wymagany")
	@Email(message = "Nieprawidłowy format adresu email")
	private String email;

	/** Nowe hasło w postaci jawnej; opcjonalne, min. 8 znaków gdy podane. */
	@Size(min = 8, message = "Nowe hasło musi mieć minimum 8 znaków")
	private String newPassword;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public UpdateProfileRequest() {
	}

	/**
	 * Zwraca imię z żądania aktualizacji profilu.
	 *
	 * @return imię użytkownika
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
	 * Zwraca nazwisko z żądania aktualizacji profilu.
	 *
	 * @return nazwisko użytkownika
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

	/**
	 * Zwraca email z żądania aktualizacji profilu.
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
	 * Zwraca nowe hasło z żądania (jawne; opcjonalne).
	 *
	 * @return nowe hasło lub {@code null}, gdy użytkownik nie zmienia hasła
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
