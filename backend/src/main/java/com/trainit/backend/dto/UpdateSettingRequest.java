package com.trainit.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateSettingRequest {

	@NotBlank(message = "Setting value cannot be blank")
	private String value;

	public UpdateSettingRequest() {
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
