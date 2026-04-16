package com.trainit.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Encja JPA reprezentująca rolę użytkownika w systemie (np. {@code USER}).
 *
 * <p>Mapowana na tabelę {@code roles}. Zawiera identyfikator oraz unikalną w praktyce nazwę roli.
 * Wiele użytkowników może wskazywać tę samą rolę przez klucz obcy {@code role_id} w tabeli {@code users}.
 *
 * <p>Role mogą być inicjalizowane przy starcie aplikacji — patrz {@link com.trainit.backend.config.RoleInitializer}.
 *
 * @see com.trainit.backend.entity.User
 * @see com.trainit.backend.repository.RoleRepository
 */
@Entity
@Table(name = "roles")
public class Role {

	/**
	 * Konstruktor bezargumentowy wymagany przez specyfikację JPA.
	 */
	public Role() {
	}

	/** Klucz główny roli; generowany przez bazę (IDENTITY). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/** Nazwa roli (np. {@code USER}); kolumna {@code name}, wymagana, max. 50 znaków. */
	@Column(nullable = false, length = 50)
	private String name;

	/**
	 * Zwraca identyfikator roli.
	 *
	 * @return klucz główny typu {@link Integer}
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Ustawia identyfikator roli.
	 *
	 * @param id klucz główny
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Zwraca nazwę roli służącą do autoryzacji i odpowiedzi API.
	 *
	 * @return nazwa roli (np. {@code USER})
	 */
	public String getName() {
		return name;
	}

	/**
	 * Ustawia nazwę roli.
	 *
	 * @param name unikalna w kontekście biznesowym nazwa (np. {@code USER})
	 */
	public void setName(String name) {
		this.name = name;
	}
}
