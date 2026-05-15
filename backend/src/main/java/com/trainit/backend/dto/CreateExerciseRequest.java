package com.trainit.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO żądania HTTP do utworzenia nowego ćwiczenia w katalogu.
 *
 * <p>Zawiera nazwę, grupę mięśniową, opis oraz flagę ćwiczenia niestandardowego.
 * Pola są walidowane przed wywołaniem {@link com.trainit.backend.service.ExerciseService}.
 *
 * @see com.trainit.backend.controller.ExerciseController
 */
public class CreateExerciseRequest {

	/** Nazwa ćwiczenia; wymagana, niepusta po stronie walidacji. */
	@NotBlank(message = "Exercise name cannot be blank")
	private String name;

	/** Grupa mięśniowa (np. klatka, plecy); opcjonalna. */
	private String muscleGroup;

	/** Opis techniki lub wskazówek; opcjonalny. */
	private String description;

	/** Flaga ćwiczenia niestandardowego dodanego przez użytkownika; opcjonalna. */
	private Boolean isCustom;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public CreateExerciseRequest() {
	}

	/**
	 * Zwraca nazwę ćwiczenia z żądania.
	 *
	 * @return nazwa ćwiczenia
	 */
	public String getName() {
		return name;
	}

	/**
	 * Ustawia nazwę ćwiczenia w DTO.
	 *
	 * @param name nazwa ćwiczenia
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Zwraca grupę mięśniową z żądania.
	 *
	 * @return grupa mięśniowa lub {@code null}
	 */
	public String getMuscleGroup() {
		return muscleGroup;
	}

	/**
	 * Ustawia grupę mięśniową w DTO.
	 *
	 * @param muscleGroup grupa mięśniowa
	 */
	public void setMuscleGroup(String muscleGroup) {
		this.muscleGroup = muscleGroup;
	}

	/**
	 * Zwraca opis ćwiczenia z żądania.
	 *
	 * @return opis lub {@code null}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Ustawia opis ćwiczenia w DTO.
	 *
	 * @param description treść opisu
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Zwraca informację, czy ćwiczenie ma być oznaczone jako niestandardowe.
	 *
	 * @return {@code true} dla ćwiczenia użytkownika; {@code null} gdy nie podano
	 */
	public Boolean getIsCustom() {
		return isCustom;
	}

	/**
	 * Ustawia flagę ćwiczenia niestandardowego w DTO.
	 *
	 * @param isCustom {@code true} gdy ćwiczenie tworzone przez użytkownika
	 */
	public void setIsCustom(Boolean isCustom) {
		this.isCustom = isCustom;
	}
}
