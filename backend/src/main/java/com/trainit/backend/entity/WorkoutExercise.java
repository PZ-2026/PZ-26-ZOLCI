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
 * Encja JPA reprezentująca pojedynczą pozycję ćwiczenia w planie treningowym.
 *
 * <p>Mapowana na tabelę {@code workout_exercises}. Łączy plan ({@link Workout})
 * z ćwiczeniem ({@link Exercise}) i określa planowane serie, powtórzenia,
 * obciążenie oraz czas trwania dla tej pozycji.
 *
 * @see Workout
 * @see Exercise
 */
@Entity
@Table(name = "workout_exercises")
public class WorkoutExercise {

	/**
	 * Konstruktor bezargumentowy wymagany przez specyfikację JPA.
	 */
	public WorkoutExercise() {
	}

	/** Klucz główny; wartość generowana przez bazę (IDENTITY). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/** Plan treningowy zawierający tę pozycję; relacja z {@link Workout}, klucz obcy {@code workout_id}. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workout_id", nullable = false)
	private Workout workout;

	/** Ćwiczenie wchodzące w skład planu; relacja z {@link Exercise}, klucz obcy {@code exercise_id}. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exercise_id", nullable = false)
	private Exercise exercise;

	/** Planowana liczba serii; kolumna {@code sets}. */
	@Column
	private Integer sets;

	/** Planowana liczba powtórzeń w serii; kolumna {@code reps}. */
	@Column
	private Integer reps;

	/** Planowane obciążenie (np. w kg); kolumna {@code weight}. */
	@Column
	private java.math.BigDecimal weight;

	/** Planowany czas trwania ćwiczenia w sekundach; kolumna {@code duration}. */
	@Column
	private Integer duration;

	/**
	 * Zwraca identyfikator pozycji ćwiczenia w planie.
	 *
	 * @return klucz główny typu {@link Integer}
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Ustawia identyfikator pozycji.
	 *
	 * @param id wartość klucza głównego
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Zwraca plan treningowy, do którego należy ta pozycja.
	 *
	 * @return encja {@link Workout}
	 */
	public Workout getWorkout() {
		return workout;
	}

	/**
	 * Ustawia plan treningowy dla pozycji.
	 *
	 * @param workout plan zawierający ćwiczenie
	 */
	public void setWorkout(Workout workout) {
		this.workout = workout;
	}

	/**
	 * Zwraca ćwiczenie przypisane do pozycji w planie.
	 *
	 * @return encja {@link Exercise}
	 */
	public Exercise getExercise() {
		return exercise;
	}

	/**
	 * Ustawia ćwiczenie w pozycji planu.
	 *
	 * @param exercise ćwiczenie w planie
	 */
	public void setExercise(Exercise exercise) {
		this.exercise = exercise;
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
	public java.math.BigDecimal getWeight() {
		return weight;
	}

	/**
	 * Ustawia planowane obciążenie.
	 *
	 * @param weight wartość obciążenia (np. w kilogramach)
	 */
	public void setWeight(java.math.BigDecimal weight) {
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
