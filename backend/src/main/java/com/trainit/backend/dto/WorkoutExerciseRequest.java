package com.trainit.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class WorkoutExerciseRequest {

	@NotNull(message = "Exercise ID cannot be null")
	private Integer exerciseId;

	private Integer sets;

	private Integer reps;

	private BigDecimal weight;

	private Integer duration;

	public WorkoutExerciseRequest() {
	}

	public Integer getExerciseId() {
		return exerciseId;
	}

	public void setExerciseId(Integer exerciseId) {
		this.exerciseId = exerciseId;
	}

	public Integer getSets() {
		return sets;
	}

	public void setSets(Integer sets) {
		this.sets = sets;
	}

	public Integer getReps() {
		return reps;
	}

	public void setReps(Integer reps) {
		this.reps = reps;
	}

	public BigDecimal getWeight() {
		return weight;
	}

	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}
}
