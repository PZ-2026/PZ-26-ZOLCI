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
 * Encja JPA reprezentująca użytkownika aplikacji TrainIT.
 *
 * <p>Mapowana na tabelę {@code users}. Przechowuje dane logowania (email, skrót hasła),
 * dane profilu (imię, nazwisko), powiązanie z {@link Role}, flagę aktywności oraz znacznik
 * czasu utworzenia rekordu. Klucz główny jest generowany automatycznie przez bazę
 * (kolumna {@code id}).
 *
 * <p>Przed pierwszym zapisem {@link #prePersist()} ustawia domyślnie {@code createdAt}
 * oraz {@code isActive}, jeśli nie zostały wcześniej wypełnione.
 *
 * @see Role
 * @see com.trainit.backend.repository.UserRepository
 */
@Entity
@Table(name = "users")
public class User {

	/**
	 * Konstruktor bezargumentowy wymagany przez specyfikację JPA.
	 */
	public User() {
	}

	/** Klucz główny; wartość generowana przez bazę (IDENTITY). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/** Unikalny adres email używany jako login; kolumna {@code email}, max. 255 znaków. */
	@Column(nullable = false, unique = true, length = 255)
	private String email;

	/** Skrót hasła (BCrypt); kolumna {@code password_hash}, typ TEXT w bazie. */
	@Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
	private String passwordHash;

	/** Imię użytkownika; kolumna {@code first_name}, max. 100 znaków. */
	@Column(name = "first_name", length = 100)
	private String firstName;

	/** Nazwisko użytkownika; kolumna {@code last_name}, max. 100 znaków. */
	@Column(name = "last_name", length = 100)
	private String lastName;

	/** Rola przypisana użytkownikowi; relacja wiele-do-jednego z tabelą {@code roles}, klucz obcy {@code role_id}. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id")
	private Role role;

	/** Flaga aktywności konta ({@code true} — logowanie dozwolone); kolumna {@code is_active}. */
	@Column(name = "is_active")
	private Boolean isActive;

	/** Czas utworzenia rekordu; kolumna {@code created_at}. */
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	/**
	 * Wywoływane przez JPA tuż przed trwałym zapisem encji.
	 *
	 * <p>Ustawia {@link #createdAt} na bieżący czas lokalny oraz {@link #isActive} na {@code true},
	 * jeśli pola mają wartość {@code null}.
	 */
	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		if (isActive == null) {
			isActive = true;
		}
	}

	/**
	 * Zwraca identyfikator użytkownika z bazy danych.
	 *
	 * <p>Przed zapisem encji może być {@code null}.
	 *
	 * @return klucz główny typu {@link Integer}
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Ustawia identyfikator użytkownika.
	 *
	 * <p>Zwykle ustawiany przez dostawcę trwałości; ręczne ustawianie jest rzadkie (np. testy).
	 *
	 * @param id wartość klucza głównego
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Zwraca zapisany adres email (login).
	 *
	 * @return email użytkownika
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Ustawia adres email użytkownika.
	 *
	 * @param email unikalny adres email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Zwraca skrót hasła (BCrypt) przechowywany w bazie.
	 *
	 * @return hash hasła; nigdy nie zwraca hasła jawnego
	 */
	public String getPasswordHash() {
		return passwordHash;
	}

	/**
	 * Ustawia skrót hasła po stronie trwałości.
	 *
	 * @param passwordHash wartość zakodowana przez {@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder}
	 */
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	/**
	 * Zwraca imię użytkownika.
	 *
	 * @return imię lub {@code null}
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Ustawia imię użytkownika.
	 *
	 * @param firstName imię (np. z formularza rejestracji)
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Zwraca nazwisko użytkownika.
	 *
	 * @return nazwisko lub {@code null}
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Ustawia nazwisko użytkownika.
	 *
	 * @param lastName nazwisko
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Zwraca encję roli powiązanej z użytkownikiem.
	 *
	 * @return obiekt {@link Role} lub {@code null}, jeśli nie przypisano
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * Ustawia rolę użytkownika.
	 *
	 * @param role encja roli (np. {@code USER})
	 */
	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * Zwraca informację, czy konto jest aktywne i może się logować.
	 *
	 * @return {@code true} gdy konto aktywne; {@code false} lub {@code null} w zależności od danych w bazie
	 */
	public Boolean getIsActive() {
		return isActive;
	}

	/**
	 * Ustawia flagę aktywności konta.
	 *
	 * @param isActive {@code true} aby umożliwić logowanie
	 */
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * Zwraca znacznik czasu utworzenia rekordu użytkownika.
	 *
	 * @return czas utworzenia lub {@code null} przed pierwszym zapisem
	 */
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * Ustawia znacznik czasu utworzenia (zwykle przez {@link #prePersist()} lub migrację).
	 *
	 * @param createdAt czas utworzenia konta
	 */
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
