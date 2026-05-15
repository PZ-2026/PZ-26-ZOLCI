package com.trainit.backend.dto;

import java.util.List;

/**
 * DTO żądania HTTP do zakończenia sesji treningowej wraz z wynikami ćwiczeń.
 *
 * <p>Zawiera faktyczny czas trwania sesji oraz listę wyników poszczególnych ćwiczeń
 * ({@link ExerciseResultRequest}). Używane przy jednoczesnym zapisie wyników i zamknięciu sesji.
 *
 * @see com.trainit.backend.controller.WorkoutSessionController
 */
public class CompleteWorkoutSessionRequest {

	/** Faktyczny czas trwania sesji w minutach; opcjonalny. */
	private Integer duration;

	/** Lista wyników ćwiczeń wykonanych podczas sesji; opcjonalna. */
	private List<ExerciseResultRequest> exerciseResults;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public CompleteWorkoutSessionRequest() {
	}

	/**
	 * Zwraca czas trwania sesji z żądania.
	 *
	 * @return czas w minutach lub {@code null}
	 */
	public Integer getDuration() {
		return duration;
	}

	/**
	 * Ustawia czas trwania sesji w DTO.
	 *
	 * @param duration czas trwania w minutach
	 */
	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	/**
	 * Zwraca listę wyników ćwiczeń z żądania.
	 *
	 * @return lista wyników lub {@code null}
	 */
	public List<ExerciseResultRequest> getExerciseResults() {
		return exerciseResults;
	}

	/**
	 * Ustawia listę wyników ćwiczeń w DTO.
	 *
	 * @param exerciseResults lista wyników ćwiczeń
	 */
	public void setExerciseResults(List<ExerciseResultRequest> exerciseResults) {
		this.exerciseResults = exerciseResults;
	}
}
