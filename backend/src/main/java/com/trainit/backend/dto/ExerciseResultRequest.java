package com.trainit.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO żądania HTTP do zapisania wyniku pojedynczego ćwiczenia w sesji treningowej.
 *
 * <p>Zawiera identyfikator ćwiczenia oraz faktycznie wykonane serie, powtórzenia,
 * obciążenie, czas i notatki. Identyfikator ćwiczenia jest wymagany przez walidację.
 *
 * @see CompleteWorkoutSessionRequest
 */
public class ExerciseResultRequest {

	/** Identyfikator ćwiczenia z katalogu; wymagany, nie może być {@code null}. */
	@NotNull(message = "Exercise ID cannot be null")
	private Integer exerciseId;

	/** Liczba wykonanych serii; opcjonalna. */
	private Integer setsDone;

	/** Liczba wykonanych powtórzeń; opcjonalna. */
	private Integer repsDone;

	/** Użyte obciążenie (np. w kg); opcjonalne. */
	private BigDecimal weightUsed;

	/** Czas trwania ćwiczenia w sekundach; opcjonalny. */
	private Integer duration;

	/** Notatki użytkownika po wykonaniu ćwiczenia; opcjonalne. */
	private String notes;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public ExerciseResultRequest() {
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
	 * Zwraca liczbę wykonanych serii z żądania.
	 *
	 * @return liczba serii lub {@code null}
	 */
	public Integer getSetsDone() {
		return setsDone;
	}

	/**
	 * Ustawia liczbę wykonanych serii w DTO.
	 *
	 * @param setsDone liczba serii
	 */
	public void setSetsDone(Integer setsDone) {
		this.setsDone = setsDone;
	}

	/**
	 * Zwraca liczbę wykonanych powtórzeń z żądania.
	 *
	 * @return liczba powtórzeń lub {@code null}
	 */
	public Integer getRepsDone() {
		return repsDone;
	}

	/**
	 * Ustawia liczbę wykonanych powtórzeń w DTO.
	 *
	 * @param repsDone liczba powtórzeń
	 */
	public void setRepsDone(Integer repsDone) {
		this.repsDone = repsDone;
	}

	/**
	 * Zwraca użyte obciążenie z żądania.
	 *
	 * @return obciążenie lub {@code null}
	 */
	public BigDecimal getWeightUsed() {
		return weightUsed;
	}

	/**
	 * Ustawia użyte obciążenie w DTO.
	 *
	 * @param weightUsed wartość obciążenia (np. w kilogramach)
	 */
	public void setWeightUsed(BigDecimal weightUsed) {
		this.weightUsed = weightUsed;
	}

	/**
	 * Zwraca czas trwania ćwiczenia z żądania.
	 *
	 * @return czas w sekundach lub {@code null}
	 */
	public Integer getDuration() {
		return duration;
	}

	/**
	 * Ustawia czas trwania ćwiczenia w DTO.
	 *
	 * @param duration czas w sekundach
	 */
	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	/**
	 * Zwraca notatki z żądania.
	 *
	 * @return notatki lub {@code null}
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * Ustawia notatki w DTO.
	 *
	 * @param notes treść notatek
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}
}
