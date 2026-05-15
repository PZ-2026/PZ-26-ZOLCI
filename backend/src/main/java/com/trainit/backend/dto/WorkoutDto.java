package com.trainit.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO odpowiedzi HTTP reprezentujące plan treningowy użytkownika.
 *
 * <p>Zawiera metadane planu (nazwa, opis, trudność, czas, data utworzenia) oraz listę
 * pozycji ćwiczeń ({@link WorkoutExerciseDto}). Mapowane z encji {@link com.trainit.backend.entity.Workout}.
 *
 * @see com.trainit.backend.controller.WorkoutController
 */
public class WorkoutDto {

	/** Identyfikator planu treningowego w bazie danych. */
	private Integer id;

	/** Nazwa planu treningowego. */
	private String name;

	/** Opis planu treningowego. */
	private String description;

	/** Poziom trudności planu (np. początkujący). */
	private String difficultyLevel;

	/** Szacowany czas trwania planu w minutach. */
	private Integer estimatedDuration;

	/** Data i godzina utworzenia planu. */
	private LocalDateTime createdAt;

	/** Lista ćwiczeń wchodzących w skład planu. */
	private List<WorkoutExerciseDto> exercises;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public WorkoutDto() {
	}

	/**
	 * Zwraca identyfikator planu treningowego.
	 *
	 * @return identyfikator planu
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Ustawia identyfikator planu.
	 *
	 * @param id identyfikator planu
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Zwraca nazwę planu treningowego.
	 *
	 * @return nazwa planu
	 */
	public String getName() {
		return name;
	}

	/**
	 * Ustawia nazwę planu.
	 *
	 * @param name nazwa planu
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Zwraca opis planu treningowego.
	 *
	 * @return opis lub {@code null}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Ustawia opis planu.
	 *
	 * @param description treść opisu
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Zwraca poziom trudności planu.
	 *
	 * @return poziom trudności lub {@code null}
	 */
	public String getDifficultyLevel() {
		return difficultyLevel;
	}

	/**
	 * Ustawia poziom trudności planu.
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
	 * Zwraca datę utworzenia planu.
	 *
	 * @return data i godzina utworzenia
	 */
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * Ustawia datę utworzenia planu.
	 *
	 * @param createdAt data i godzina utworzenia
	 */
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Zwraca listę ćwiczeń planu.
	 *
	 * @return lista pozycji ćwiczeń lub {@code null}
	 */
	public List<WorkoutExerciseDto> getExercises() {
		return exercises;
	}

	/**
	 * Ustawia listę ćwiczeń planu.
	 *
	 * @param exercises lista pozycji ćwiczeń
	 */
	public void setExercises(List<WorkoutExerciseDto> exercises) {
		this.exercises = exercises;
	}
}
