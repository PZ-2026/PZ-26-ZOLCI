package com.trainit.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Żądanie zakończenia wcześniej uruchomionej sesji treningowej.
 */
public class FinishSessionRequest {

	/** Czas trwania sesji w minutach. */
	@NotNull(message = "Czas trwania jest wymagany")
	@Min(value = 1, message = "Czas trwania musi być większy od 0")
	@Max(value = 600, message = "Czas trwania nie może przekraczać 600 minut")
	private Integer duration;

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}
}
