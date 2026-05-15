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

/**
 * Encja JPA reprezentująca ćwiczenie w katalogu aplikacji TrainIT.
 *
 * <p>Mapowana na tabelę {@code exercises}. Przechowuje nazwę, grupę mięśniową, opis,
 * informację czy ćwiczenie jest niestandardowe ({@link #isCustom}) oraz opcjonalne
 * powiązanie z użytkownikiem, który je utworzył ({@link #createdBy}).
 *
 * <p>Przed pierwszym zapisem {@link #prePersist()} ustawia domyślnie {@code isCustom}
 * na {@code false}, jeśli pole nie zostało wcześniej wypełnione.
 *
 * @see com.trainit.backend.repository.ExerciseRepository
 */
@Entity
@Table(name = "exercises")
public class Exercise {

	/**
	 * Konstruktor bezargumentowy wymagany przez specyfikację JPA.
	 */
	public Exercise() {
	}

	/** Klucz główny; wartość generowana przez bazę (IDENTITY). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/** Nazwa ćwiczenia; kolumna {@code name}, wymagana, max. 255 znaków. */
	@Column(nullable = false, length = 255)
	private String name;

	/** Grupa mięśniowa (np. klatka, plecy); kolumna {@code muscle_group}, max. 100 znaków. */
	@Column(length = 100)
	private String muscleGroup;

	/** Opis techniki lub wskazówek; kolumna {@code description}, typ TEXT. */
	@Column(columnDefinition = "TEXT")
	private String description;

	/** Flaga ćwiczenia niestandardowego dodanego przez użytkownika; kolumna {@code is_custom}. */
	@Column(name = "is_custom")
	private Boolean isCustom;

	/** Użytkownik, który utworzył ćwiczenie niestandardowe; relacja z {@link User}, klucz obcy {@code created_by}. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;

	/**
	 * Wywoływane przez JPA tuż przed trwałym zapisem encji.
	 *
	 * <p>Ustawia {@link #isCustom} na {@code false}, jeśli pole ma wartość {@code null}.
	 */
	@PrePersist
	void prePersist() {
		if (isCustom == null) {
			isCustom = false;
		}
	}

	/**
	 * Zwraca identyfikator ćwiczenia z bazy danych.
	 *
	 * <p>Przed zapisem encji może być {@code null}.
	 *
	 * @return klucz główny typu {@link Integer}
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Ustawia identyfikator ćwiczenia.
	 *
	 * @param id wartość klucza głównego
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Zwraca nazwę ćwiczenia.
	 *
	 * @return nazwa ćwiczenia
	 */
	public String getName() {
		return name;
	}

	/**
	 * Ustawia nazwę ćwiczenia.
	 *
	 * @param name nazwa ćwiczenia
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Zwraca grupę mięśniową przypisaną do ćwiczenia.
	 *
	 * @return grupa mięśniowa lub {@code null}
	 */
	public String getMuscleGroup() {
		return muscleGroup;
	}

	/**
	 * Ustawia grupę mięśniową ćwiczenia.
	 *
	 * @param muscleGroup grupa mięśniowa
	 */
	public void setMuscleGroup(String muscleGroup) {
		this.muscleGroup = muscleGroup;
	}

	/**
	 * Zwraca opis ćwiczenia.
	 *
	 * @return opis lub {@code null}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Ustawia opis ćwiczenia.
	 *
	 * @param description treść opisu
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Zwraca informację, czy ćwiczenie jest niestandardowe (dodane przez użytkownika).
	 *
	 * @return {@code true} dla ćwiczenia niestandardowego; {@code false} lub {@code null} w zależności od danych
	 */
	public Boolean getIsCustom() {
		return isCustom;
	}

	/**
	 * Ustawia flagę ćwiczenia niestandardowego.
	 *
	 * @param isCustom {@code true} gdy ćwiczenie utworzone przez użytkownika
	 */
	public void setIsCustom(Boolean isCustom) {
		this.isCustom = isCustom;
	}

	/**
	 * Zwraca użytkownika, który utworzył to ćwiczenie.
	 *
	 * @return encja {@link User} lub {@code null} dla ćwiczeń systemowych
	 */
	public User getCreatedBy() {
		return createdBy;
	}

	/**
	 * Ustawia autora ćwiczenia niestandardowego.
	 *
	 * @param createdBy użytkownik tworzący ćwiczenie
	 */
	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}
}
