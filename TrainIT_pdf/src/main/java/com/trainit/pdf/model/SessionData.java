package com.trainit.pdf.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model danych pojedynczej sesji treningowej w raporcie PDF.
 *
 * <p>Przechowuje nazwę planu, datę zakończenia, czas trwania oraz listę wyników
 * poszczególnych ćwiczeń ({@link ExerciseResultData}). Używany przez {@link ReportData}.
 *
 * @see ReportData
 * @see ExerciseResultData
 */
public class SessionData {

	/** Nazwa planu treningowego wykonanego w sesji. */
	private String workoutName;

	/** Data zakończenia sesji w formacie tekstowym do wyświetlenia w PDF. */
	private String completedDate;

	/** Czas trwania sesji w minutach. */
	private Integer durationMinutes;

	/** Lista wyników ćwiczeń wykonanych podczas sesji. */
	private List<ExerciseResultData> results = new ArrayList<>();

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do mapowania danych z JSON lub ręcznego budowania obiektu.
	 */
	public SessionData() {
	}

	/**
	 * Tworzy dane sesji z podanymi wartościami pól.
	 *
	 * @param workoutName nazwa planu treningowego
	 * @param completedDate data zakończenia w formacie tekstowym
	 * @param durationMinutes czas trwania w minutach
	 * @param results lista wyników ćwiczeń; {@code null} zostanie zastąpiona pustą listą
	 */
	public SessionData(
			String workoutName,
			String completedDate,
			Integer durationMinutes,
			List<ExerciseResultData> results) {
		this.workoutName = workoutName;
		this.completedDate = completedDate;
		this.durationMinutes = durationMinutes;
		if (results != null) {
			this.results = results;
		}
	}

	/**
	 * Zwraca nazwę planu treningowego sesji.
	 *
	 * @return nazwa planu
	 */
	public String getWorkoutName() {
		return workoutName;
	}

	/**
	 * Ustawia nazwę planu treningowego.
	 *
	 * @param workoutName nazwa planu
	 */
	public void setWorkoutName(String workoutName) {
		this.workoutName = workoutName;
	}

	/**
	 * Zwraca datę zakończenia sesji w formacie tekstowym.
	 *
	 * @return data zakończenia
	 */
	public String getCompletedDate() {
		return completedDate;
	}

	/**
	 * Ustawia datę zakończenia sesji.
	 *
	 * @param completedDate data zakończenia do wyświetlenia w raporcie
	 */
	public void setCompletedDate(String completedDate) {
		this.completedDate = completedDate;
	}

	/**
	 * Zwraca czas trwania sesji w minutach.
	 *
	 * @return czas w minutach lub {@code null}
	 */
	public Integer getDurationMinutes() {
		return durationMinutes;
	}

	/**
	 * Ustawia czas trwania sesji.
	 *
	 * @param durationMinutes czas w minutach
	 */
	public void setDurationMinutes(Integer durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	/**
	 * Zwraca listę wyników ćwiczeń w sesji.
	 *
	 * @return lista wyników (nigdy {@code null} po ustawieniu przez {@link #setResults})
	 */
	public List<ExerciseResultData> getResults() {
		return results;
	}

	/**
	 * Ustawia listę wyników ćwiczeń; {@code null} zostanie zastąpiona pustą listą.
	 *
	 * @param results lista wyników ćwiczeń
	 */
	public void setResults(List<ExerciseResultData> results) {
		this.results = results != null ? results : new ArrayList<>();
	}
}
