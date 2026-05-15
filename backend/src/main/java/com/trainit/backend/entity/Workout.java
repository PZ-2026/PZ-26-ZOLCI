package com.trainit.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Encja JPA reprezentująca plan treningowy użytkownika.
 *
 * <p>Mapowana na tabelę {@code workouts}. Przechowuje nazwę, opis, poziom trudności,
 * szacowany czas trwania oraz powiązanie z właścicielem planu ({@link User}).
 *
 * <p>Przed pierwszym zapisem {@link #prePersist()} ustawia {@link #createdAt}
 * na bieżący czas lokalny, jeśli pole nie zostało wcześniej wypełnione.
 *
 * @see WorkoutExercise
 * @see com.trainit.backend.repository.WorkoutRepository
 */
@Entity
@Table(name = "workouts")
public class Workout {

	/**
	 * Konstruktor bezargumentowy wymagany przez specyfikację JPA.
	 */
	public Workout() {
	}

	/** Klucz główny; wartość generowana przez bazę (IDENTITY). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/** Właściciel planu treningowego; relacja z {@link User}, klucz obcy {@code user_id}. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	/** Nazwa planu treningowego; kolumna {@code name}, wymagana, max. 255 znaków. */
	@Column(nullable = false, length = 255)
	private String name;

	/** Opis planu; kolumna {@code description}, typ TEXT. */
	@Column(columnDefinition = "TEXT")
	private String description;

	/** Poziom trudności (np. początkujący, zaawansowany); kolumna {@code difficulty_level}, max. 50 znaków. */
	@Column(name = "difficulty_level", length = 50)
	private String difficultyLevel;

	/** Szacowany czas trwania planu w minutach; kolumna {@code estimated_duration}. */
	@Column(name = "estimated_duration")
	private Integer estimatedDuration;

	/** Czas utworzenia planu; kolumna {@code created_at}. */
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	/**
	 * Wywoływane przez JPA tuż przed trwałym zapisem encji.
	 *
	 * <p>Ustawia {@link #createdAt} na bieżący czas lokalny, jeśli pole ma wartość {@code null}.
	 */
	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	/**
	 * Zwraca identyfikator planu treningowego.
	 *
	 * @return klucz główny typu {@link Integer}
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Ustawia identyfikator planu.
	 *
	 * @param id wartość klucza głównego
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Zwraca użytkownika będącego właścicielem planu.
	 *
	 * @return encja {@link User}
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Ustawia właściciela planu treningowego.
	 *
	 * @param user użytkownik tworzący plan
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Zwraca nazwę planu treningowego.
	 *
	 * @return nazwa planu
	 */
	public String getName() {
		return name;
	}

	/**
	 * Ustawia nazwę planu treningowego.
	 *
	 * @param name nazwa planu
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Zwraca opis planu treningowego.
	 *
	 * @return opis lub {@code null}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Ustawia opis planu.
	 *
	 * @param description treść opisu
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Zwraca poziom trudności planu.
	 *
	 * @return poziom trudności lub {@code null}
	 */
	public String getDifficultyLevel() {
		return difficultyLevel;
	}

	/**
	 * Ustawia poziom trudności planu.
	 *
	 * @param difficultyLevel poziom trudności
	 */
	public void setDifficultyLevel(String difficultyLevel) {
		this.difficultyLevel = difficultyLevel;
	}

	/**
	 * Zwraca szacowany czas trwania planu w minutach.
	 *
	 * @return czas w minutach lub {@code null}
	 */
	public Integer getEstimatedDuration() {
		return estimatedDuration;
	}

	/**
	 * Ustawia szacowany czas trwania planu.
	 *
	 * @param estimatedDuration czas w minutach
	 */
	public void setEstimatedDuration(Integer estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
	}

	/**
	 * Zwraca znacznik czasu utworzenia planu.
	 *
	 * @return czas utworzenia lub {@code null} przed pierwszym zapisem
	 */
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * Ustawia znacznik czasu utworzenia planu.
	 *
	 * @param createdAt czas utworzenia rekordu
	 */
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
