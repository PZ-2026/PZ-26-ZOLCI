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

/**
 * Encja JPA reprezentująca wynik wykonania pojedynczego ćwiczenia w sesji treningowej.
 *
 * <p>Mapowana na tabelę {@code exercise_results}. Łączy sesję ({@link WorkoutSession})
 * z ćwiczeniem ({@link Exercise}) i przechowuje faktycznie wykonane serie, powtórzenia,
 * obciążenie, czas trwania oraz notatki użytkownika.
 *
 * @see WorkoutSession
 * @see Exercise
 */
@Entity
@Table(name = "exercise_results")
public class ExerciseResult {

	/**
	 * Konstruktor bezargumentowy wymagany przez specyfikację JPA.
	 */
	public ExerciseResult() {
	}

	/** Klucz główny; wartość generowana przez bazę (IDENTITY). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/** Sesja treningowa, do której należy wynik; relacja z {@link WorkoutSession}, klucz obcy {@code session_id}. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id", nullable = false)
	private WorkoutSession session;

	/** Ćwiczenie, którego dotyczy wynik; relacja z {@link Exercise}, klucz obcy {@code exercise_id}. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exercise_id", nullable = false)
	private Exercise exercise;

	/** Liczba wykonanych serii; kolumna {@code sets_done}. */
	@Column(name = "sets_done")
	private Integer setsDone;

	/** Liczba wykonanych powtórzeń w serii; kolumna {@code reps_done}. */
	@Column(name = "reps_done")
	private Integer repsDone;

	/** Użyte obciążenie (np. w kg); kolumna {@code weight_used}. */
	@Column(name = "weight_used")
	private java.math.BigDecimal weightUsed;

	/** Czas trwania ćwiczenia w sekundach; kolumna {@code duration}. */
	@Column
	private Integer duration;

	/** Notatki użytkownika po wykonaniu ćwiczenia; kolumna {@code notes}, typ TEXT. */
	@Column(columnDefinition = "TEXT")
	private String notes;

	/**
	 * Zwraca identyfikator wyniku ćwiczenia.
	 *
	 * @return klucz główny typu {@link Integer}
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Ustawia identyfikator wyniku.
	 *
	 * @param id wartość klucza głównego
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Zwraca sesję treningową powiązaną z tym wynikiem.
	 *
	 * @return encja {@link WorkoutSession}
	 */
	public WorkoutSession getSession() {
		return session;
	}

	/**
	 * Ustawia sesję treningową dla wyniku.
	 *
	 * @param session sesja, w której wykonano ćwiczenie
	 */
	public void setSession(WorkoutSession session) {
		this.session = session;
	}

	/**
	 * Zwraca ćwiczenie, którego dotyczy wynik.
	 *
	 * @return encja {@link Exercise}
	 */
	public Exercise getExercise() {
		return exercise;
	}

	/**
	 * Ustawia ćwiczenie powiązane z wynikiem.
	 *
	 * @param exercise wykonane ćwiczenie
	 */
	public void setExercise(Exercise exercise) {
		this.exercise = exercise;
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
	 * Zwraca użyte obciążenie podczas ćwiczenia.
	 *
	 * @return obciążenie lub {@code null}
	 */
	public java.math.BigDecimal getWeightUsed() {
		return weightUsed;
	}

	/**
	 * Ustawia użyte obciążenie.
	 *
	 * @param weightUsed wartość obciążenia (np. w kilogramach)
	 */
	public void setWeightUsed(java.math.BigDecimal weightUsed) {
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
	 * Zwraca notatki użytkownika do wyniku.
	 *
	 * @return notatki lub {@code null}
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * Ustawia notatki do wyniku ćwiczenia.
	 *
	 * @param notes treść notatek
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}
}
