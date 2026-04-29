package com.trainit.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ExerciseResultRequest {

	@NotNull(message = "Exercise ID cannot be null")
	private Integer exerciseId;

	private Integer setsDone;

	private Integer repsDone;

	private BigDecimal weightUsed;

	private Integer duration;

	private String notes;

	public ExerciseResultRequest() {
	}

	public Integer getExerciseId() {
		return exerciseId;
	}

	public void setExerciseId(Integer exerciseId) {
		this.exerciseId = exerciseId;
	}

	public Integer getSetsDone() {
		return setsDone;
	}

	public void setSetsDone(Integer setsDone) {
		this.setsDone = setsDone;
	}

	public Integer getRepsDone() {
		return repsDone;
	}

	public void setRepsDone(Integer repsDone) {
		this.repsDone = repsDone;
	}

	public BigDecimal getWeightUsed() {
		return weightUsed;
	}

	public void setWeightUsed(BigDecimal weightUsed) {
		this.weightUsed = weightUsed;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
