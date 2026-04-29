package com.trainit.backend.dto;

public class ExerciseDto {

	private Integer id;
	private String name;
	private String muscleGroup;
	private String description;
	private Boolean isCustom;
	private Integer createdById;
	private String createdByEmail;

	public ExerciseDto() {
	}

	public ExerciseDto(Integer id, String name, String muscleGroup, String description, Boolean isCustom) {
		this.id = id;
		this.name = name;
		this.muscleGroup = muscleGroup;
		this.description = description;
		this.isCustom = isCustom;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public Integer getCreatedById() {
		return createdById;
	}

	public void setCreatedById(Integer createdById) {
		this.createdById = createdById;
	}

	public String getCreatedByEmail() {
		return createdByEmail;
	}

	public void setCreatedByEmail(String createdByEmail) {
		this.createdByEmail = createdByEmail;
	}
}
