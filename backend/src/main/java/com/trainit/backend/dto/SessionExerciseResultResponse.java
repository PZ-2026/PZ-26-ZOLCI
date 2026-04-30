package com.trainit.backend.dto;

/**
 * Wynik ćwiczenia zapisany w ramach sesji treningowej (WF-10).
 */
public record SessionExerciseResultResponse(
		Integer id,
		Integer exerciseId,
		String exerciseName,
		Integer setsDone,
		Integer repsDone,
		Double weightUsed,
		Integer duration,
		String notes
) {
}
