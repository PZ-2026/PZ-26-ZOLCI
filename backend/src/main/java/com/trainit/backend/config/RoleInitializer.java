package com.trainit.backend.config;

import com.trainit.backend.entity.Role;
import com.trainit.backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Komponent startowy zapewniający istnienie podstawowej roli {@code USER} w bazie danych.
 *
 * <p>Implementuje {@link CommandLineRunner}, więc wykonuje się po podniesieniu kontekstu Springa.
 * Jeśli w tabeli {@code roles} nie ma rekordu o nazwie {@code USER}, tworzy go w jednej transakcji.
 * Dzięki temu {@link com.trainit.backend.service.AuthService} może bezpiecznie przypisać rolę
 * przy rejestracji bez ryzyka braku rekordu (o ile inicjalizacja zdążyła się wykonać).
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
	 * Uruchamia seed roli {@code USER}, gdy nie istnieje w bazie.
	 *
	 * <p>Wykonywane w transakcji, aby zapis był atomowy względem bazy.
	 *
	 * @param args argumenty linii poleceń aplikacji Spring Boot (nieużywane)
	 */
	@Override
	@Transactional
	public void run(String... args) {
		if (roleRepository.findByName("USER").isEmpty()) {
			Role role = new Role();
			role.setName("USER");
			roleRepository.save(role);
		}
	}
}
