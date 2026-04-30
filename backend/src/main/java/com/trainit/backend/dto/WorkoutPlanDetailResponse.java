package com.trainit.backend.dto;

/**
 * Szczegóły planu treningowego dla formularza edycji w aplikacji mobilnej.
 */
public record WorkoutPlanDetailResponse(
		Integer id,
		String name,
		String description,
		String difficultyLevel,
		Integer estimatedDuration
) {
}
