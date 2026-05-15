package com.trainit.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO żądania HTTP do uruchomienia nowej sesji treningowej na podstawie planu.
 *
 * <p>Zawiera identyfikator użytkownika oraz identyfikator planu treningowego do wykonania.
 * Oba pola są wymagane przez walidację Bean Validation.
 *
 * @see com.trainit.backend.controller.WorkoutSessionController
 */
public class StartSessionRequest {

	/** Identyfikator użytkownika uruchamiającego sesję; wymagany. */
	@NotNull(message = "Id użytkownika jest wymagane")
	private Integer userId;

	/** Identyfikator planu treningowego do wykonania; wymagany. */
	@NotNull(message = "Id planu treningowego jest wymagane")
	private Integer workoutId;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public StartSessionRequest() {
	}

	/**
	 * Zwraca identyfikator użytkownika z żądania.
	 *
	 * @return identyfikator użytkownika
	 */
	public Integer getUserId() {
		return userId;
	}

	/**
	 * Ustawia identyfikator użytkownika w DTO.
	 *
	 * @param userId identyfikator użytkownika uruchamiającego sesję
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/**
	 * Zwraca identyfikator planu treningowego z żądania.
	 *
	 * @return identyfikator planu treningowego
	 */
	public Integer getWorkoutId() {
		return workoutId;
	}

	/**
	 * Ustawia identyfikator planu treningowego w DTO.
	 *
	 * @param workoutId identyfikator planu do wykonania
	 */
	public void setWorkoutId(Integer workoutId) {
		this.workoutId = workoutId;
	}
}
