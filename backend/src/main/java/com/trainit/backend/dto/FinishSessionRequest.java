package com.trainit.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO żądania HTTP do zakończenia wcześniej uruchomionej sesji treningowej.
 *
 * <p>Zawiera faktyczny czas trwania sesji w minutach. Walidacja wymaga wartości
 * z zakresu od 1 do 600 minut włącznie.
 *
 * @see com.trainit.backend.controller.WorkoutSessionController
 */
public class FinishSessionRequest {

	/** Czas trwania sesji w minutach; wymagany, od 1 do 600. */
	@NotNull(message = "Czas trwania jest wymagany")
	@Min(value = 1, message = "Czas trwania musi być większy od 0")
	@Max(value = 600, message = "Czas trwania nie może przekraczać 600 minut")
	private Integer duration;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public FinishSessionRequest() {
	}

	/**
	 * Zwraca czas trwania sesji z żądania.
	 *
	 * @return czas trwania w minutach
	 */
	public Integer getDuration() {
		return duration;
	}

	/**
	 * Ustawia czas trwania sesji w DTO.
	 *
	 * @param duration czas trwania w minutach (1–600)
	 */
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
}
