package com.trainit.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class ChangeRoleRequest {

	@NotBlank
	private String role;

	public ChangeRoleRequest() {
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}

