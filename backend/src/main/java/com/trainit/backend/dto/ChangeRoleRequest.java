package com.trainit.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO żądania HTTP do zmiany roli użytkownika przez administratora.
 *
 * <p>Zawiera nazwę roli docelowej (np. USER, TRAINER, ADMIN). Walidacja wymaga
 * niepustej wartości przed przekazaniem do warstwy serwisu.
 *
 * @see com.trainit.backend.controller.AdminController
 */
public class ChangeRoleRequest {

	/** Nazwa roli docelowej przypisywanej użytkownikowi; wymagana, niepusta. */
	@NotBlank
	private String role;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public ChangeRoleRequest() {
	}

	/**
	 * Zwraca nazwę roli docelowej z żądania.
	 *
	 * @return nazwa roli (np. USER, ADMIN)
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Ustawia nazwę roli docelowej w DTO.
	 *
	 * @param role nazwa roli do przypisania
	 */
	public void setRole(String role) {
		this.role = role;
	}
}
