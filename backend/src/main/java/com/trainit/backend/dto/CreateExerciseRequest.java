package com.trainit.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateExerciseRequest {

	@NotBlank(message = "Exercise name cannot be blank")
	private String name;

	private String muscleGroup;

	private String description;

	private Boolean isCustom;

	public CreateExerciseRequest() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMuscleGroup() {
		return muscleGroup;
	}

	public void setMuscleGroup(String muscleGroup) {
		this.muscleGroup = muscleGroup;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getIsCustom() {
		return isCustom;
	}

	public void setIsCustom(Boolean isCustom) {
		this.isCustom = isCustom;
	}
}
