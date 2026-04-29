package com.trainit.backend.dto;

import java.math.BigDecimal;

public class ExerciseResultDto {

	private Integer id;
	private Integer exerciseId;
	private String exerciseName;
	private Integer setsDone;
	private Integer repsDone;
	private BigDecimal weightUsed;
	private Integer duration;
	private String notes;

	public ExerciseResultDto() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getExerciseId() {
		return exerciseId;
	}

	public void setExerciseId(Integer exerciseId) {
		this.exerciseId = exerciseId;
	}

	public String getExerciseName() {
		return exerciseName;
	}

	public void setExerciseName(String exerciseName) {
		this.exerciseName = exerciseName;
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
