package com.trainit.backend.security;

import java.security.Principal;

/**
 * Reprezentuje uwierzytelnionego użytkownika odczytanego z tokena JWT.
 *
 * <p>Obiekt jest ustawiany jako {@link Principal} w kontekście Spring Security
 * i używany przez kontrolery do egzekwowania dostępu do własnych danych.
 *
 * @param userId identyfikator użytkownika z bazy
 * @param email email użytkownika
 * @param role rola aplikacyjna (np. USER, TRAINER, ADMIN)
 */
public record JwtPrincipal(Integer userId, String email, String role) implements Principal {

	/**
	 * Zwraca nazwę principal wykorzystywaną przez interfejs {@link Principal}.
	 *
	 * @return email użytkownika
	 */
	@Override
	public String getName() {
		return email;
	}
}
