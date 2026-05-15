package com.trainit.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO odpowiedzi HTTP reprezentujące sesję treningową użytkownika.
 *
 * <p>Zawiera identyfikator sesji, dane planu, daty planowaną i zakończenia, status,
 * czas trwania oraz listę wyników ćwiczeń ({@link ExerciseResultDto}).
 * Mapowane z encji {@link com.trainit.backend.entity.WorkoutSession}.
 *
 * @see com.trainit.backend.controller.WorkoutSessionController
 */
public class WorkoutSessionDto {

	/** Identyfikator sesji treningowej w bazie danych. */
	private Integer id;

	/** Identyfikator planu treningowego realizowanego w sesji. */
	private Integer workoutId;

	/** Nazwa planu treningowego do wyświetlenia w interfejsie. */
	private String workoutName;

	/** Zaplanowana data i godzina rozpoczęcia sesji. */
	private LocalDateTime plannedDate;

	/** Data i godzina faktycznego zakończenia sesji. */
	private LocalDateTime completedDate;

	/** Status sesji (np. PLANNED, IN_PROGRESS, COMPLETED). */
	private String status;

	/** Faktyczny czas trwania sesji w minutach. */
	private Integer duration;

	/** Lista wyników ćwiczeń wykonanych podczas sesji. */
	private List<ExerciseResultDto> exerciseResults;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public WorkoutSessionDto() {
	}

	/**
	 * Zwraca identyfikator sesji treningowej.
	 *
	 * @return identyfikator sesji
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Ustawia identyfikator sesji.
	 *
	 * @param id identyfikator sesji
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Zwraca identyfikator planu treningowego sesji.
	 *
	 * @return identyfikator planu
	 */
	public Integer getWorkoutId() {
		return workoutId;
	}

	/**
	 * Ustawia identyfikator planu treningowego.
	 *
	 * @param workoutId identyfikator planu
	 */
	public void setWorkoutId(Integer workoutId) {
		this.workoutId = workoutId;
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
	 * Zwraca zaplanowaną datę i godzinę sesji.
	 *
	 * @return data planowana lub {@code null}
	 */
	public LocalDateTime getPlannedDate() {
		return plannedDate;
	}

	/**
	 * Ustawia zaplanowaną datę sesji.
	 *
	 * @param plannedDate data i godzina planowanego treningu
	 */
	public void setPlannedDate(LocalDateTime plannedDate) {
		this.plannedDate = plannedDate;
	}

	/**
	 * Zwraca datę i godzinę zakończenia sesji.
	 *
	 * @return data zakończenia lub {@code null} dla sesji niezakończonych
	 */
	public LocalDateTime getCompletedDate() {
		return completedDate;
	}

	/**
	 * Ustawia datę zakończenia sesji.
	 *
	 * @param completedDate data i godzina zakończenia treningu
	 */
	public void setCompletedDate(LocalDateTime completedDate) {
		this.completedDate = completedDate;
	}

	/**
	 * Zwraca status sesji treningowej.
	 *
	 * @return status sesji
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Ustawia status sesji.
	 *
	 * @param status nowy status sesji
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Zwraca faktyczny czas trwania sesji w minutach.
	 *
	 * @return czas w minutach lub {@code null}
	 */
	public Integer getDuration() {
		return duration;
	}

	/**
	 * Ustawia czas trwania sesji.
	 *
	 * @param duration czas w minutach
	 */
	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	/**
	 * Zwraca listę wyników ćwiczeń w sesji.
	 *
	 * @return lista wyników lub {@code null}
	 */
	public List<ExerciseResultDto> getExerciseResults() {
		return exerciseResults;
	}

	/**
	 * Ustawia listę wyników ćwiczeń sesji.
	 *
	 * @param exerciseResults lista wyników ćwiczeń
	 */
	public void setExerciseResults(List<ExerciseResultDto> exerciseResults) {
		this.exerciseResults = exerciseResults;
	}
}
