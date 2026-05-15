package com.trainit.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO żądania HTTP do aktualizacji pojedynczego ustawienia użytkownika.
 *
 * <p>Zawiera nową wartość ustawienia identyfikowanego w ścieżce URL endpointu.
 * Walidacja wymaga niepustej wartości przed zapisem.
 *
 * @see com.trainit.backend.controller.FeatureDataController
 */
public class UpdateSettingRequest {

	/** Nowa wartość ustawienia; wymagana, niepusta po stronie walidacji. */
	@NotBlank(message = "Setting value cannot be blank")
	private String value;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public UpdateSettingRequest() {
	}

	/**
	 * Zwraca nową wartość ustawienia z żądania.
	 *
	 * @return wartość ustawienia
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Ustawia wartość ustawienia w DTO.
	 *
	 * @param value nowa wartość ustawienia
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
