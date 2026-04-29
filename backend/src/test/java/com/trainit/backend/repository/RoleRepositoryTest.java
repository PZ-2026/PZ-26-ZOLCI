package com.trainit.backend.repository;

import com.trainit.backend.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy integracyjne repozytorium {@link RoleRepository} na bazie H2.
 *
 * <p>Sprawdzają podstawowe operacje CRUD oraz zapytanie pomocnicze {@link RoleRepository#findByName(String)}.
 *
 * @see RoleRepository
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryTest {

	@Autowired
	private RoleRepository roleRepository;

	@Test
	@DisplayName("save nadaje wygenerowane id roli")
	void save_assignsId() {
		Role role = new Role();
		role.setName("TEST_ROLE_" + UUID.randomUUID());
		Role saved = roleRepository.save(role);
		assertThat(saved.getId()).isNotNull();
	}

	@Test
	@DisplayName("findByName zwraca rolę dla istniejącej nazwy")
	void findByName_returnsRoleForExistingName() {
		String roleName = "TEST_ROLE_" + UUID.randomUUID();
		Role role = new Role();
		role.setName(roleName);
		roleRepository.save(role);

		Optional<Role> found = roleRepository.findByName(roleName);
		assertThat(found).isPresent();
		assertThat(found.get().getName()).isEqualTo(roleName);
	}

	@Test
	@DisplayName("findByName zwraca empty dla nieistniejącej nazwy")
	void findByName_returnsEmptyForUnknown() {
		assertThat(roleRepository.findByName("NOPE")).isEmpty();
	}

	@Test
	@DisplayName("findByName jest case-sensitive")
	void findByName_isCaseSensitive() {
		String roleName = "TEST_ROLE_" + UUID.randomUUID();
		Role role = new Role();
		role.setName(roleName);
		roleRepository.save(role);
		assertThat(roleRepository.findByName(roleName.toLowerCase())).isEmpty();
	}

	@Test
	@DisplayName("można zapisać role USER i ADMIN obok siebie")
	void save_multipleRoles() {
		long initialCount = roleRepository.count();
		String userRoleName = "TEST_USER_" + UUID.randomUUID();
		String adminRoleName = "TEST_ADMIN_" + UUID.randomUUID();
		Role userRole = new Role();
		userRole.setName(userRoleName);
		Role adminRole = new Role();
		adminRole.setName(adminRoleName);
		roleRepository.save(userRole);
		roleRepository.save(adminRole);

		assertThat(roleRepository.count()).isEqualTo(initialCount + 2);
		assertThat(roleRepository.findByName(userRoleName)).isPresent();
		assertThat(roleRepository.findByName(adminRoleName)).isPresent();
	}

	@Test
	@DisplayName("count zwraca poprawną liczbę ról")
	void count_reflectsRowsCount() {
		long initialCount = roleRepository.count();
		Role r = new Role();
		r.setName("TEST_ROLE_" + UUID.randomUUID());
		roleRepository.save(r);
		assertThat(roleRepository.count()).isEqualTo(initialCount + 1);
	}
}
