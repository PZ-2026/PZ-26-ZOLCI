package com.trainit.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO żądania HTTP reprezentujące pojedynczą pozycję ćwiczenia w planie treningowym.
 *
 * <p>Zawiera identyfikator ćwiczenia z katalogu oraz planowane serie, powtórzenia,
 * obciążenie i czas. Używane w {@link CreateWorkoutRequest} jako element listy ćwiczeń.
 *
 * @see CreateWorkoutRequest
 */
public class WorkoutExerciseRequest {

	/** Identyfikator ćwiczenia z katalogu; wymagany, nie może być {@code null}. */
	@NotNull(message = "Exercise ID cannot be null")
	private Integer exerciseId;

	/** Planowana liczba serii; opcjonalna. */
	private Integer sets;

	/** Planowana liczba powtórzeń w serii; opcjonalna. */
	private Integer reps;

	/** Planowane obciążenie (np. w kg); opcjonalne. */
	private BigDecimal weight;

	/** Planowany czas trwania ćwiczenia w sekundach; opcjonalny. */
	private Integer duration;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public WorkoutExerciseRequest() {
	}

	/**
	 * Zwraca identyfikator ćwiczenia z żądania.
	 *
	 * @return identyfikator ćwiczenia
	 */
	public Integer getExerciseId() {
		return exerciseId;
	}

	/**
	 * Ustawia identyfikator ćwiczenia w DTO.
	 *
	 * @param exerciseId identyfikator ćwiczenia
	 */
	public void setExerciseId(Integer exerciseId) {
		this.exerciseId = exerciseId;
	}

	/**
	 * Zwraca planowaną liczbę serii z żądania.
	 *
	 * @return liczba serii lub {@code null}
	 */
	public Integer getSets() {
		return sets;
	}

	/**
	 * Ustawia planowaną liczbę serii w DTO.
	 *
	 * @param sets liczba serii
	 */
	public void setSets(Integer sets) {
		this.sets = sets;
	}

	/**
	 * Zwraca planowaną liczbę powtórzeń z żądania.
	 *
	 * @return liczba powtórzeń lub {@code null}
	 */
	public Integer getReps() {
		return reps;
	}

	/**
	 * Ustawia planowaną liczbę powtórzeń w DTO.
	 *
	 * @param reps liczba powtórzeń
	 */
	public void setReps(Integer reps) {
		this.reps = reps;
	}

	/**
	 * Zwraca planowane obciążenie z żądania.
	 *
	 * @return obciążenie lub {@code null}
	 */
	public BigDecimal getWeight() {
		return weight;
	}

	/**
	 * Ustawia planowane obciążenie w DTO.
	 *
	 * @param weight wartość obciążenia (np. w kilogramach)
	 */
	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}

	/**
	 * Zwraca planowany czas trwania ćwiczenia z żądania.
	 *
	 * @return czas w sekundach lub {@code null}
	 */
	public Integer getDuration() {
		return duration;
	}

	/**
	 * Ustawia planowany czas trwania ćwiczenia w DTO.
	 *
	 * @param duration czas w sekundach
	 */
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
}
