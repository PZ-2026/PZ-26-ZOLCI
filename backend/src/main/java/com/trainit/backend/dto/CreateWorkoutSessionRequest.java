package com.trainit.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO żądania HTTP do zaplanowania nowej sesji treningowej.
 *
 * <p>Zawiera identyfikator planu treningowego oraz opcjonalną datę planowaną rozpoczęcia.
 * Identyfikator planu jest wymagany przez walidację Bean Validation.
 *
 * @see com.trainit.backend.controller.WorkoutSessionController
 */
public class CreateWorkoutSessionRequest {

	/** Identyfikator planu treningowego; wymagany, nie może być {@code null}. */
	@NotNull(message = "Workout ID cannot be null")
	private Integer workoutId;

	/** Zaplanowana data i godzina rozpoczęcia sesji; opcjonalna. */
	private LocalDateTime plannedDate;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public CreateWorkoutSessionRequest() {
	}

	/**
	 * Zwraca identyfikator planu treningowego z żądania.
	 *
	 * @return identyfikator planu
	 */
	public Integer getWorkoutId() {
		return workoutId;
	}

	/**
	 * Ustawia identyfikator planu treningowego w DTO.
	 *
	 * @param workoutId identyfikator planu
	 */
	public void setWorkoutId(Integer workoutId) {
		this.workoutId = workoutId;
	}

	/**
	 * Zwraca zaplanowaną datę sesji z żądania.
	 *
	 * @return data planowana lub {@code null}
	 */
	public LocalDateTime getPlannedDate() {
		return plannedDate;
	}

	/**
	 * Ustawia zaplanowaną datę sesji w DTO.
	 *
	 * @param plannedDate data i godzina planowanego treningu
	 */
	public void setPlannedDate(LocalDateTime plannedDate) {
		this.plannedDate = plannedDate;
	}
}
