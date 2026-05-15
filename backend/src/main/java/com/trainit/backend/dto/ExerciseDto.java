package com.trainit.backend.dto;

/**
 * DTO odpowiedzi HTTP reprezentujące ćwiczenie z katalogu aplikacji.
 *
 * <p>Zawiera identyfikator, nazwę, grupę mięśniową, opis, flagę ćwiczenia niestandardowego
 * oraz opcjonalne dane autora (identyfikator i email). Mapowane z encji {@link com.trainit.backend.entity.Exercise}.
 *
 * @see com.trainit.backend.controller.ExerciseController
 */
public class ExerciseDto {

	/** Identyfikator ćwiczenia w bazie danych. */
	private Integer id;

	/** Nazwa ćwiczenia. */
	private String name;

	/** Grupa mięśniowa (np. klatka, plecy). */
	private String muscleGroup;

	/** Opis techniki lub wskazówek. */
	private String description;

	/** Flaga ćwiczenia niestandardowego dodanego przez użytkownika. */
	private Boolean isCustom;

	/** Identyfikator użytkownika, który utworzył ćwiczenie niestandardowe. */
	private Integer createdById;

	/** Email użytkownika, który utworzył ćwiczenie niestandardowe. */
	private String createdByEmail;

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do deserializacji JSON (framework MVC).
	 */
	public ExerciseDto() {
	}

	/**
	 * Tworzy DTO ćwiczenia z podstawowymi polami (bez danych autora).
	 *
	 * @param id identyfikator ćwiczenia
	 * @param name nazwa ćwiczenia
	 * @param muscleGroup grupa mięśniowa
	 * @param description opis ćwiczenia
	 * @param isCustom flaga ćwiczenia niestandardowego
	 */
	public ExerciseDto(Integer id, String name, String muscleGroup, String description, Boolean isCustom) {
		this.id = id;
		this.name = name;
		this.muscleGroup = muscleGroup;
		this.description = description;
		this.isCustom = isCustom;
	}

	/**
	 * Zwraca identyfikator ćwiczenia.
	 *
	 * @return identyfikator ćwiczenia
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Ustawia identyfikator ćwiczenia.
	 *
	 * @param id identyfikator ćwiczenia
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
	 * Zwraca grupę mięśniową ćwiczenia.
	 *
	 * @return grupa mięśniowa lub {@code null}
	 */
	public String getMuscleGroup() {
		return muscleGroup;
	}

	/**
	 * Ustawia grupę mięśniową.
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
	 * Zwraca informację, czy ćwiczenie jest niestandardowe.
	 *
	 * @return {@code true} dla ćwiczenia użytkownika; {@code null} gdy nie ustawiono
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
	 * Zwraca identyfikator autora ćwiczenia niestandardowego.
	 *
	 * @return identyfikator użytkownika lub {@code null}
	 */
	public Integer getCreatedById() {
		return createdById;
	}

	/**
	 * Ustawia identyfikator autora ćwiczenia.
	 *
	 * @param createdById identyfikator użytkownika
	 */
	public void setCreatedById(Integer createdById) {
		this.createdById = createdById;
	}

	/**
	 * Zwraca email autora ćwiczenia niestandardowego.
	 *
	 * @return email autora lub {@code null}
	 */
	public String getCreatedByEmail() {
		return createdByEmail;
	}

	/**
	 * Ustawia email autora ćwiczenia.
	 *
	 * @param createdByEmail adres email autora
	 */
	public void setCreatedByEmail(String createdByEmail) {
		this.createdByEmail = createdByEmail;
	}
}
