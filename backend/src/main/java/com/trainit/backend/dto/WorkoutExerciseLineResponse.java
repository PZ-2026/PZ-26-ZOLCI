package com.trainit.backend.dto;

/**
 * Pozycja ćwiczenia w planie (tabela {@code workout_exercises}) dla aplikacji mobilnej.
 */
public record WorkoutExerciseLineResponse(
		Integer id,
		Integer exerciseId,
		String exerciseName,
		Integer sets,
		Integer reps,
		Double weight,
		Integer duration
) {
}
