package com.trainit.backend.repository;

import com.trainit.backend.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy integracyjne repozytorium {@link RoleRepository} na bazie H2.
 *
 * <p>Sprawdzają podstawowe operacje CRUD oraz zapytanie pomocnicze {@link RoleRepository#findByName(String)}.
 *
 * @see RoleRepository
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class RoleRepositoryTest {

	@Autowired
	private RoleRepository roleRepository;

	@Test
	@DisplayName("save nadaje wygenerowane id roli")
	void save_assignsId() {
		Role role = new Role();
		role.setName("USER");
		Role saved = roleRepository.save(role);
		assertThat(saved.getId()).isNotNull();
	}

	@Test
	@DisplayName("findByName zwraca rolę dla istniejącej nazwy")
	void findByName_returnsRoleForExistingName() {
		Role role = new Role();
		role.setName("USER");
		roleRepository.save(role);

		Optional<Role> found = roleRepository.findByName("USER");
		assertThat(found).isPresent();
		assertThat(found.get().getName()).isEqualTo("USER");
	}

	@Test
	@DisplayName("findByName zwraca empty dla nieistniejącej nazwy")
	void findByName_returnsEmptyForUnknown() {
		assertThat(roleRepository.findByName("NOPE")).isEmpty();
	}

	@Test
	@DisplayName("findByName jest case-sensitive")
	void findByName_isCaseSensitive() {
		Role role = new Role();
		role.setName("USER");
		roleRepository.save(role);
		assertThat(roleRepository.findByName("user")).isEmpty();
	}

	@Test
	@DisplayName("można zapisać role USER i ADMIN obok siebie")
	void save_multipleRoles() {
		Role userRole = new Role();
		userRole.setName("USER");
		Role adminRole = new Role();
		adminRole.setName("ADMIN");
		roleRepository.save(userRole);
		roleRepository.save(adminRole);

		assertThat(roleRepository.findAll()).hasSize(2);
		assertThat(roleRepository.findByName("USER")).isPresent();
		assertThat(roleRepository.findByName("ADMIN")).isPresent();
	}

	@Test
	@DisplayName("count zwraca poprawną liczbę ról")
	void count_reflectsRowsCount() {
		assertThat(roleRepository.count()).isZero();
		Role r = new Role();
		r.setName("USER");
		roleRepository.save(r);
		assertThat(roleRepository.count()).isEqualTo(1);
	}
}
