package com.trainit.backend.dto;

import com.trainit.backend.entity.User;

/**
 * Odpowiedź REST po pomyślnej rejestracji lub w kontekście danych profilu bez hasła.
 *
 * <p>Rekord niemodyfikowalny zawiera identyfikator użytkownika, email, imię, nazwisko oraz
 * nazwę roli jako tekst (np. {@code USER}). Nie zawiera skrótu hasła ani danych wrażliwych.
 *
 * @param id identyfikator użytkownika z bazy ({@link User#getId()})
 * @param email znormalizowany email konta
 * @param firstName imię
 * @param lastName nazwisko
 * @param role nazwa roli z encji {@link com.trainit.backend.entity.Role}; może być {@code null}, jeśli brak powiązania
 * @param isActive flaga aktywności konta; {@code true} gdy logowanie dozwolone
 * @see User
 * @see com.trainit.backend.service.AuthService#register(RegisterRequest)
 */
public record UserResponse(Integer id, String email, String firstName, String lastName, String role, Boolean isActive) {

	public UserResponse(Integer id, String email, String firstName, String lastName, String role) {
		this(id, email, firstName, lastName, role, true);
	}

	/**
	 * Buduje odpowiedź na podstawie zapisanej encji użytkownika.
	 *
	 * <p>Nazwa roli jest odczytywana z leniwego lub eager załadowanego powiązania {@link User#getRole()}.
	 *
	 * @param user encja {@link User} po zapisie lub odczycie z bazy
	 * @return rekord {@link UserResponse} gotowy do serializacji JSON
	 */
	public static UserResponse fromEntity(User user) {
		String roleName = user.getRole() != null ? user.getRole().getName() : null;
		return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), roleName, user.getIsActive());
	}
}
