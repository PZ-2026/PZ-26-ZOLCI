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

import java.time.LocalDateTime;

/**
 * Encja JPA reprezentująca sesję treningową użytkownika.
 *
 * <p>Mapowana na tabelę {@code workout_sessions}. Łączy użytkownika ({@link User})
 * z planem treningowym ({@link Workout}), przechowuje datę planowaną i zakończenia,
 * status sesji oraz faktyczny czas trwania.
 *
 * @see ExerciseResult
 * @see com.trainit.backend.repository.WorkoutSessionRepository
 */
@Entity
@Table(name = "workout_sessions")
public class WorkoutSession {

	/**
	 * Konstruktor bezargumentowy wymagany przez specyfikację JPA.
	 */
	public WorkoutSession() {
	}

	/** Klucz główny; wartość generowana przez bazę (IDENTITY). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/** Użytkownik wykonujący sesję; relacja z {@link User}, klucz obcy {@code user_id}. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	/** Plan treningowy realizowany w sesji; relacja z {@link Workout}, klucz obcy {@code workout_id}. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workout_id", nullable = false)
	private Workout workout;

	/** Zaplanowana data i godzina rozpoczęcia sesji; kolumna {@code planned_date}. */
	@Column(name = "planned_date")
	private LocalDateTime plannedDate;

	/** Data i godzina faktycznego zakończenia sesji; kolumna {@code completed_date}. */
	@Column(name = "completed_date")
	private LocalDateTime completedDate;

	/** Status sesji (np. zaplanowana, w trakcie, zakończona); kolumna {@code status}, max. 50 znaków. */
	@Column(length = 50)
	private String status;

	/** Faktyczny czas trwania sesji w minutach; kolumna {@code duration}. */
	@Column
	private Integer duration;

	/**
	 * Zwraca identyfikator sesji treningowej.
	 *
	 * @return klucz główny typu {@link Integer}
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Ustawia identyfikator sesji.
	 *
	 * @param id wartość klucza głównego
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Zwraca użytkownika wykonującego sesję.
	 *
	 * @return encja {@link User}
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Ustawia użytkownika sesji.
	 *
	 * @param user użytkownik realizujący trening
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Zwraca plan treningowy realizowany w sesji.
	 *
	 * @return encja {@link Workout}
	 */
	public Workout getWorkout() {
		return workout;
	}

	/**
	 * Ustawia plan treningowy sesji.
	 *
	 * @param workout plan wykonywany w sesji
	 */
	public void setWorkout(Workout workout) {
		this.workout = workout;
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
	 * @return status (np. PLANNED, IN_PROGRESS, COMPLETED) lub {@code null}
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
}
