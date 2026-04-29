package com.trainit.backend.dto;

import com.trainit.backend.entity.User;

/**
 * Odpowiedź REST po pomyślnym zalogowaniu.
 *
 * <p>Zawiera dane profilu użytkownika (bez hasła) oraz token JWT generowany w
 * {@link com.trainit.backend.service.AuthService#login(LoginRequest)}.
 *
 * @param id identyfikator użytkownika
 * @param email email konta
 * @param firstName imię
 * @param lastName nazwisko
 * @param role nazwa roli; może być {@code null}, jeśli brak powiązania w encji
 * @param token token sesji JWT przekazywany do klienta
 * @see User
 * @see com.trainit.backend.service.AuthService#login(LoginRequest)
 */
public record LoginResponse(Integer id, String email, String firstName, String lastName, String role, String token) {

	/**
	 * Buduje odpowiedź logowania z encji użytkownika i wygenerowanego tokena.
	 *
	 * @param user encja {@link User} po pomyślnym uwierzytelnieniu
	 * @param token wartość podpisanego tokena JWT
	 * @return rekord {@link LoginResponse} do zwrócenia z kontrolera
	 */
	public static LoginResponse fromEntity(User user, String token) {
		String roleName = user.getRole() != null ? user.getRole().getName() : null;
		return new LoginResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), roleName, token);
	}
}
