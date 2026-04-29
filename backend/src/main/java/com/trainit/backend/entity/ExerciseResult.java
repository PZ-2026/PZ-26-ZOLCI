package com.trainit.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "exercise_results")
public class ExerciseResult {

	public ExerciseResult() {
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id", nullable = false)
	private WorkoutSession session;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exercise_id", nullable = false)
	private Exercise exercise;

	@Column(name = "sets_done")
	private Integer setsDone;

	@Column(name = "reps_done")
	private Integer repsDone;

	@Column(name = "weight_used")
	private java.math.BigDecimal weightUsed;

	@Column
	private Integer duration;

	@Column(columnDefinition = "TEXT")
	private String notes;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public WorkoutSession getSession() {
		return session;
	}

	public void setSession(WorkoutSession session) {
		this.session = session;
	}

	public Exercise getExercise() {
		return exercise;
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;
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

	public java.math.BigDecimal getWeightUsed() {
		return weightUsed;
	}

	public void setWeightUsed(java.math.BigDecimal weightUsed) {
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
