package com.trainit.backend.dto;

import java.math.BigDecimal;

/**
 * DTO odpowiedzi HTTP reprezentujące wynik wykonania ćwiczenia w sesji treningowej.
 *
 * <p>Zawiera identyfikator wyniku, dane ćwiczenia oraz faktycznie wykonane serie,
 * powtórzenia, obciążenie, czas i notatki. Mapowane z encji {@link com.trainit.backend.entity.ExerciseResult}.
 *
 * @see WorkoutSessionDto
 */
public class ExerciseResultDto {

	/** Identyfikator wyniku ćwiczenia w bazie danych. */
	private Integer id;

	/** Identyfikator ćwiczenia z katalogu. */
	private Integer exerciseId;

	/** Nazwa ćwiczenia do wyświetlenia w interfejsie. */
	private String exerciseName;

	/** Liczba wykonanych serii. */
	private Integer setsDone;

	/** Liczba wykonanych powtórzeń. */
	private Integer repsDone;

	/** Użyte obciążenie (np. w kg). */
	private BigDecimal weightUsed;

	/** Czas trwania ćwiczenia w sekundach. */
	private Integer duration;

	/** Notatki użytkownika po wykonaniu ćwiczenia. */
	private String notes;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public ExerciseResultDto() {
	}

	/**
	 * Zwraca identyfikator wyniku ćwiczenia.
	 *
	 * @return identyfikator wyniku
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Ustawia identyfikator wyniku.
	 *
	 * @param id identyfikator wyniku
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Zwraca identyfikator ćwiczenia.
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
	 * Zwraca liczbę wykonanych serii.
	 *
	 * @return liczba serii lub {@code null}
	 */
	public Integer getSetsDone() {
		return setsDone;
	}

	/**
	 * Ustawia liczbę wykonanych serii.
	 *
	 * @param setsDone liczba serii
	 */
	public void setSetsDone(Integer setsDone) {
		this.setsDone = setsDone;
	}

	/**
	 * Zwraca liczbę wykonanych powtórzeń.
	 *
	 * @return liczba powtórzeń lub {@code null}
	 */
	public Integer getRepsDone() {
		return repsDone;
	}

	/**
	 * Ustawia liczbę wykonanych powtórzeń.
	 *
	 * @param repsDone liczba powtórzeń
	 */
	public void setRepsDone(Integer repsDone) {
		this.repsDone = repsDone;
	}

	/**
	 * Zwraca użyte obciążenie.
	 *
	 * @return obciążenie lub {@code null}
	 */
	public BigDecimal getWeightUsed() {
		return weightUsed;
	}

	/**
	 * Ustawia użyte obciążenie.
	 *
	 * @param weightUsed wartość obciążenia (np. w kilogramach)
	 */
	public void setWeightUsed(BigDecimal weightUsed) {
		this.weightUsed = weightUsed;
	}

	/**
	 * Zwraca czas trwania ćwiczenia w sekundach.
	 *
	 * @return czas w sekundach lub {@code null}
	 */
	public Integer getDuration() {
		return duration;
	}

	/**
	 * Ustawia czas trwania ćwiczenia.
	 *
	 * @param duration czas w sekundach
	 */
	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	/**
	 * Zwraca notatki do wyniku ćwiczenia.
	 *
	 * @return notatki lub {@code null}
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * Ustawia notatki do wyniku.
	 *
	 * @param notes treść notatek
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}
}
