package com.trainit.backend.repository;

import com.trainit.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

	/**
	 * Wykonuje zapytanie repozytorium: `existsByEmail`.
	 * @param email kryterium wyszukiwania
	 * @return wynik typu `boolean`
	 */
	boolean existsByEmail(String email);
	/**
	 * Wykonuje zapytanie repozytorium: `existsByEmailAndIdNot`.
	 * @param email kryterium wyszukiwania
	 * @param id kryterium wyszukiwania
	 * @return wynik typu `boolean`
	 */
	boolean existsByEmailAndIdNot(String email, Integer id);

	/**
	 * Wykonuje zapytanie repozytorium: `findByEmail`.
	 * @param email kryterium wyszukiwania
	 * @return wynik typu `Optional<User>`
	 */
	Optional<User> findByEmail(String email);
}
