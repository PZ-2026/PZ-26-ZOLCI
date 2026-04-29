package com.trainit.backend.config;

import com.trainit.backend.entity.Role;
import com.trainit.backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Komponent startowy zapewniający istnienie podstawowych ról w bazie danych.
 *
 * <p>Implementuje {@link CommandLineRunner}, więc wykonuje się po podniesieniu kontekstu Springa.
 * Jeśli w tabeli {@code roles} nie ma rekordów o nazwach {@code USER}, {@code TRAINER} lub
 * {@code ADMIN}, tworzy je w jednej transakcji. Dzięki temu warstwa auth i logika ról działa
 * spójnie ze specyfikacją projektu.
 *
 * @see RoleRepository
 * @see com.trainit.backend.entity.Role
 * @see com.trainit.backend.service.AuthService#register(com.trainit.backend.dto.RegisterRequest)
 */
@Component
public class RoleInitializer implements CommandLineRunner {

	/** Repozytorium ról używane do sprawdzenia i zapisu roli startowej. */
	private final RoleRepository roleRepository;

	/**
	 * Tworzy komponent z repozytorium ról.
	 *
	 * @param roleRepository repozytorium {@link RoleRepository}
	 */
	public RoleInitializer(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	/**
	 * Uruchamia seed ról systemowych, gdy nie istnieją w bazie.
	 *
	 * <p>Wykonywane w transakcji, aby zapis był atomowy względem bazy.
	 *
	 * @param args argumenty linii poleceń aplikacji Spring Boot (nieużywane)
	 */
	@Override
	@Transactional
	public void run(String... args) {
		ensureRole("USER");
		ensureRole("TRAINER");
		ensureRole("ADMIN");
	}

	/**
	 * Tworzy wskazaną rolę, jeśli nie istnieje.
	 *
	 * @param roleName nazwa roli
	 */
	private void ensureRole(String roleName) {
		if (roleRepository.findByName(roleName).isEmpty()) {
			Role role = new Role();
			role.setName(roleName);
			roleRepository.save(role);
		}
	}
}
