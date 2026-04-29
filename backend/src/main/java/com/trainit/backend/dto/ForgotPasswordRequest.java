package com.trainit.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO żądania resetu hasła dla konta użytkownika.
 */
public class ForgotPasswordRequest {

	@NotBlank(message = "Email jest wymagany")
	@Email(message = "Nieprawidłowy format adresu email")
	private String email;

	@NotBlank(message = "Nowe hasło jest wymagane")
	@Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków")
	private String newPassword;

	public ForgotPasswordRequest() {
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}
