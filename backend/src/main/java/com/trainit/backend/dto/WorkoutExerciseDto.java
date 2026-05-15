package com.trainit.backend.dto;

import java.math.BigDecimal;

/**
 * DTO odpowiedzi HTTP reprezentujące pojedynczą pozycję ćwiczenia w planie treningowym.
 *
 * <p>Zawiera identyfikator pozycji, dane ćwiczenia z katalogu oraz planowane serie,
 * powtórzenia, obciążenie i czas. Mapowane z encji {@link com.trainit.backend.entity.WorkoutExercise}.
 *
 * @see WorkoutDto
 */
public class WorkoutExerciseDto {

	/** Identyfikator pozycji ćwiczenia w planie. */
	private Integer id;

	/** Identyfikator ćwiczenia z katalogu. */
	private Integer exerciseId;

	/** Nazwa ćwiczenia do wyświetlenia w interfejsie. */
	private String exerciseName;

	/** Grupa mięśniowa ćwiczenia. */
	private String muscleGroup;

	/** Planowana liczba serii. */
	private Integer sets;

	/** Planowana liczba powtórzeń w serii. */
	private Integer reps;

	/** Planowane obciążenie (np. w kg). */
	private BigDecimal weight;

	/** Planowany czas trwania ćwiczenia w sekundach. */
	private Integer duration;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public WorkoutExerciseDto() {
	}

	/**
	 * Zwraca identyfikator pozycji ćwiczenia w planie.
	 *
	 * @return identyfikator pozycji
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Ustawia identyfikator pozycji.
	 *
	 * @param id identyfikator pozycji
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Zwraca identyfikator ćwiczenia z katalogu.
	 *
	 * @return identyfikator ćwiczenia
	 */
	public Integer getExerciseId() {
		return exerciseId;
	}

	/**
	 * Ustawia identyfikator ćwiczenia.
	 *
	 * @param exerciseId identyfikator ćwiczenia
	 */
	public void setExerciseId(Integer exerciseId) {
		this.exerciseId = exerciseId;
	}

	/**
	 * Zwraca nazwę ćwiczenia.
	 *
	 * @return nazwa ćwiczenia
	 */
	public String getExerciseName() {
		return exerciseName;
	}

	/**
	 * Ustawia nazwę ćwiczenia.
	 *
	 * @param exerciseName nazwa ćwiczenia
	 */
	public void setExerciseName(String exerciseName) {
		this.exerciseName = exerciseName;
	}

	/**
	 * Zwraca grupę mięśniową ćwiczenia.
	 *
	 * @return grupa mięśniowa lub {@code null}
	 */
	public String getMuscleGroup() {
		return muscleGroup;
	}

	/**
	 * Ustawia grupę mięśniową.
	 *
	 * @param muscleGroup grupa mięśniowa
	 */
	public void setMuscleGroup(String muscleGroup) {
		this.muscleGroup = muscleGroup;
	}

	/**
	 * Zwraca planowaną liczbę serii.
	 *
	 * @return liczba serii lub {@code null}
	 */
	public Integer getSets() {
		return sets;
	}

	/**
	 * Ustawia planowaną liczbę serii.
	 *
	 * @param sets liczba serii
	 */
	public void setSets(Integer sets) {
		this.sets = sets;
	}

	/**
	 * Zwraca planowaną liczbę powtórzeń.
	 *
	 * @return liczba powtórzeń lub {@code null}
	 */
	public Integer getReps() {
		return reps;
	}

	/**
	 * Ustawia planowaną liczbę powtórzeń.
	 *
	 * @param reps liczba powtórzeń
	 */
	public void setReps(Integer reps) {
		this.reps = reps;
	}

	/**
	 * Zwraca planowane obciążenie.
	 *
	 * @return obciążenie lub {@code null}
	 */
	public BigDecimal getWeight() {
		return weight;
	}

	/**
	 * Ustawia planowane obciążenie.
	 *
	 * @param weight wartość obciążenia (np. w kilogramach)
	 */
	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}

	/**
	 * Zwraca planowany czas trwania ćwiczenia w sekundach.
	 *
	 * @return czas w sekundach lub {@code null}
	 */
	public Integer getDuration() {
		return duration;
	}

	/**
	 * Ustawia planowany czas trwania ćwiczenia.
	 *
	 * @param duration czas w sekundach
	 */
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
}
