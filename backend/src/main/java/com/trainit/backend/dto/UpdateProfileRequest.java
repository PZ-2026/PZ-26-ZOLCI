package com.trainit.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {
	@NotBlank(message = "Imię jest wymagane")
	private String firstName;
	@NotBlank(message = "Nazwisko jest wymagane")
	private String lastName;
	@NotBlank(message = "Email jest wymagany")
	@Email(message = "Nieprawidłowy format adresu email")
	private String email;
	@Size(min = 8, message = "Nowe hasło musi mieć minimum 8 znaków")
	private String newPassword;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
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
