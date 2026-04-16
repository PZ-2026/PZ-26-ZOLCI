package com.trainit.backend.repository;

import com.trainit.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozytorium Spring Data JPA dla encji {@link User}.
 *
 * <p>Dostarcza standardowe operacje CRUD ({@link JpaRepository}) oraz zapytania
 * pomocnicze po adresie email — używane przy rejestracji (sprawdzenie unikalności)
 * i logowaniu (wyszukanie konta).
 *
 * @see com.trainit.backend.service.AuthService
 * @see User
 */
public interface UserRepository extends JpaRepository<User, Integer> {

	/**
	 * Sprawdza, czy w bazie istnieje użytkownik o podanym adresie email.
	 *
	 * <p>Email powinien być znormalizowany po stronie serwisu (np. małe litery),
	 * aby wynik był zgodny z zapisem w tabeli {@code users}.
	 *
	 * @param email znormalizowany adres email ({@link String})
	 * @return {@code true}, jeśli rekord z takim emailem istnieje; w przeciwnym razie {@code false}
	 */
	boolean existsByEmail(String email);

	/**
	 * Wyszukuje użytkownika po adresie email.
	 *
	 * @param email znormalizowany adres email
	 * @return {@link Optional} zawierający {@link User}, lub puste, jeśli brak dopasowania
	 */
	Optional<User> findByEmail(String email);
}
