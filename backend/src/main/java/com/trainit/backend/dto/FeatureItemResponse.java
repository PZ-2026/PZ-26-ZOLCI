package com.trainit.backend.dto;

/**
 * Prosty model odpowiedzi listowej używany przez modułowe ekrany aplikacji mobilnej.
 *
 * <p>Kontrakt odpowiada strukturze oczekiwanej przez klienta Android
 * ({@code FeatureItemDto}: pola {@code title} i {@code subtitle}).
 */
public class FeatureItemResponse {

	/** Id encji źródłowej (np. workout/session), gdy dotyczy. */
	private Integer id;

	/** Tytuł pozycji prezentowanej na liście modułu. */
	private String title;

	/** Krótki opis pomocniczy wyświetlany pod tytułem. */
	private String subtitle;

	/**
	 * Konstruktor bezargumentowy wymagany przez serializację JSON.
	 */
	public FeatureItemResponse() {
	}

	/**
	 * Tworzy odpowiedź listową z pełnym zestawem pól.
	 *
	 * @param id identyfikator encji źródłowej lub {@code null}
	 * @param title tytuł pozycji
	 * @param subtitle opis pozycji
	 */
	public FeatureItemResponse(Integer id, String title, String subtitle) {
		this.id = id;
		this.title = title;
		this.subtitle = subtitle;
	}

	/**
	 * Skrótowy konstruktor dla pozycji bez identyfikatora.
	 *
	 * @param title tytuł pozycji
	 * @param subtitle opis pozycji
	 */
	public FeatureItemResponse(String title, String subtitle) {
		this(null, title, subtitle);
	}

	/**
	 * Zwraca identyfikator encji źródłowej.
	 *
	 * @return identyfikator lub {@code null}
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Ustawia identyfikator encji źródłowej.
	 *
	 * @param id identyfikator
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Zwraca tytuł pozycji.
	 *
	 * @return tytuł
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Ustawia tytuł pozycji.
	 *
	 * @param title tytuł do ustawienia
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Zwraca opis pozycji.
	 *
	 * @return opis pomocniczy
	 */
	public String getSubtitle() {
		return subtitle;
	}

	/**
	 * Ustawia opis pozycji.
	 *
	 * @param subtitle opis do ustawienia
	 */
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
}
