package com.trainit.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * DTO żądania HTTP do utworzenia nowego planu treningowego.
 *
 * <p>Zawiera metadane planu (nazwa, opis, trudność, szacowany czas) oraz opcjonalną listę
 * pozycji ćwiczeń ({@link WorkoutExerciseRequest}). Walidacja wymaga niepustej nazwy planu.
 *
 * @see com.trainit.backend.controller.WorkoutController
 */
public class CreateWorkoutRequest {

	/** Nazwa planu treningowego; wymagana, niepusta po stronie walidacji. */
	@NotBlank(message = "Workout name cannot be blank")
	private String name;

	/** Opis planu treningowego; opcjonalny. */
	private String description;

	/** Poziom trudności planu (np. początkujący); opcjonalny. */
	private String difficultyLevel;

	/** Szacowany czas trwania planu w minutach; opcjonalny. */
	private Integer estimatedDuration;

	/** Lista ćwiczeń wchodzących w skład planu; opcjonalna. */
	private List<WorkoutExerciseRequest> exercises;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public CreateWorkoutRequest() {
	}

	/**
	 * Zwraca nazwę planu treningowego z żądania.
	 *
	 * @return nazwa planu
	 */
	public String getName() {
		return name;
	}

	/**
	 * Ustawia nazwę planu w DTO.
	 *
	 * @param name nazwa planu
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Zwraca opis planu z żądania.
	 *
	 * @return opis lub {@code null}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Ustawia opis planu w DTO.
	 *
	 * @param description treść opisu
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Zwraca poziom trudności planu z żądania.
	 *
	 * @return poziom trudności lub {@code null}
	 */
	public String getDifficultyLevel() {
		return difficultyLevel;
	}

	/**
	 * Ustawia poziom trudności planu w DTO.
	 *
	 * @param difficultyLevel poziom trudności
	 */
	public void setDifficultyLevel(String difficultyLevel) {
		this.difficultyLevel = difficultyLevel;
	}

	/**
	 * Zwraca szacowany czas trwania planu w minutach.
	 *
	 * @return czas w minutach lub {@code null}
	 */
	public Integer getEstimatedDuration() {
		return estimatedDuration;
	}

	/**
	 * Ustawia szacowany czas trwania planu.
	 *
	 * @param estimatedDuration czas w minutach
	 */
	public void setEstimatedDuration(Integer estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
	}

	/**
	 * Zwraca listę ćwiczeń planu z żądania.
	 *
	 * @return lista pozycji ćwiczeń lub {@code null}
	 */
	public List<WorkoutExerciseRequest> getExercises() {
		return exercises;
	}

	/**
	 * Ustawia listę ćwiczeń planu w DTO.
	 *
	 * @param exercises lista pozycji ćwiczeń
	 */
	public void setExercises(List<WorkoutExerciseRequest> exercises) {
		this.exercises = exercises;
	}
}
