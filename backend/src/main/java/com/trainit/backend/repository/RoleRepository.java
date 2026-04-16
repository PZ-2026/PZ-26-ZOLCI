package com.trainit.backend.repository;

import com.trainit.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozytorium Spring Data JPA dla encji {@link Role}.
 *
 * <p>Umożliwia wyszukiwanie roli po nazwie — wykorzystywane przy rejestracji użytkownika
 * (przypisanie roli {@code USER}) oraz przy inicjalizacji danych startowych
 * ({@link com.trainit.backend.config.RoleInitializer}).
 *
 * @see Role
 * @see com.trainit.backend.service.AuthService
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {

	/**
	 * Wyszukuje rolę po dokładnej nazwie (np. {@code "USER"}).
	 *
	 * @param name nazwa roli w kolumnie {@code name}
	 * @return {@link Optional} z {@link Role} lub puste, jeśli nie znaleziono
	 */
	Optional<Role> findByName(String name);
}
