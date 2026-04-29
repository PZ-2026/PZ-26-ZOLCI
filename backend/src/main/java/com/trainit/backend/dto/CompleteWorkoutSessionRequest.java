package com.trainit.backend.dto;

import java.util.List;

public class CompleteWorkoutSessionRequest {

	private Integer duration;

	private List<ExerciseResultRequest> exerciseResults;

	public CompleteWorkoutSessionRequest() {
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public List<ExerciseResultRequest> getExerciseResults() {
		return exerciseResults;
	}

	public void setExerciseResults(List<ExerciseResultRequest> exerciseResults) {
		this.exerciseResults = exerciseResults;
	}
}
