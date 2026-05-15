package com.trainit.backend.pdf.model;

/**
 * Model danych wyniku pojedynczego ćwiczenia w sesji treningowej do generowania raportu PDF.
 *
 * <p>Przechowuje nazwę ćwiczenia oraz faktycznie wykonane serie, powtórzenia, obciążenie
 * i notatki użytkownika. Używany przez {@link SessionData} jako element listy wyników.
 *
 * @see SessionData
 * @see com.trainit.backend.pdf.service.PdfReportService
 */
public class ExerciseResultData {

	/** Nazwa ćwiczenia wyświetlana w raporcie. */
	private String exerciseName;

	/** Liczba wykonanych serii. */
	private Integer setsDone;

	/** Liczba wykonanych powtórzeń. */
	private Integer repsDone;

	/** Użyte obciążenie (np. w kg). */
	private Double weightUsed;

	/** Notatki użytkownika po wykonaniu ćwiczenia. */
	private String notes;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do mapowania danych z JSON lub ręcznego budowania obiektu.
	 */
	public ExerciseResultData() {
	}

	/**
	 * Tworzy wynik ćwiczenia z podanymi wartościami pól.
	 *
	 * @param exerciseName nazwa ćwiczenia
	 * @param setsDone liczba wykonanych serii
	 * @param repsDone liczba wykonanych powtórzeń
	 * @param weightUsed użyte obciążenie
	 * @param notes notatki użytkownika
	 */
	public ExerciseResultData(
			String exerciseName,
			Integer setsDone,
			Integer repsDone,
			Double weightUsed,
			String notes) {
		this.exerciseName = exerciseName;
		this.setsDone = setsDone;
		this.repsDone = repsDone;
		this.weightUsed = weightUsed;
		this.notes = notes;
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
	public Double getWeightUsed() {
		return weightUsed;
	}

	/**
	 * Ustawia użyte obciążenie.
	 *
	 * @param weightUsed wartość obciążenia (np. w kilogramach)
	 */
	public void setWeightUsed(Double weightUsed) {
		this.weightUsed = weightUsed;
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
