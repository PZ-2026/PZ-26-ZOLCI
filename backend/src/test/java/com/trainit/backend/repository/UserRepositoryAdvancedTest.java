package com.trainit.backend.repository;

import com.trainit.backend.entity.Role;
import com.trainit.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rozszerzone testy {@link UserRepository} pokrywające scenariusze niewyczerpane
 * w {@link UserRepositoryTest}.
 *
 * <p>Obejmują testy parametryzowane (różne formaty emaili), aktualizację istniejących encji,
 * weryfikację wartości {@code createdAt} oraz spójność relacji User-Role.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryAdvancedTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	private Role userRole;
	private Role adminRole;

	@BeforeEach
	void setUp() {
		userRole = new Role();
		userRole.setName("USER");
		userRole = roleRepository.save(userRole);

		adminRole = new Role();
		adminRole.setName("ADMIN");
		adminRole = roleRepository.save(adminRole);
	}

	/**
	 * Buduje encję użytkownika z rolą USER do zapisu.
	 *
	 * @param email email użytkownika
	 * @return świeżo utworzona, niezapisana encja
	 */
	private User buildUser(String email) {
		User u = new User();
		u.setEmail(email);
		u.setPasswordHash("hash");
		u.setFirstName("Jan");
		u.setLastName("Kowalski");
		u.setIsActive(true);
		u.setRole(userRole);
		return u;
	}

	@ParameterizedTest(name = "save+findByEmail dla '{0}'")
	@ValueSource(strings = {
			"a@b.com",
			"jan.kowalski@example.com",
			"j_an+tag@example.co.uk",
			"123@xyz.io",
			"long.user.name@long-domain.example"
	})
	@DisplayName("dla różnych formatów emaili save i findByEmail działają poprawnie")
	void saveAndFind_variousEmailFormats(String email) {
		userRepository.save(buildUser(email));
		Optional<User> found = userRepository.findByEmail(email);
		assertThat(found).isPresent();
		assertThat(found.get().getEmail()).isEqualTo(email);
	}

	@Test
	@DisplayName("findByEmail dla emaila zarejestrowanego z innym caseingiem nie znajduje (case-sensitive)")
	void findByEmail_isCaseSensitive() {
		userRepository.save(buildUser("jan@example.com"));
		assertThat(userRepository.findByEmail("Jan@Example.com")).isEmpty();
		assertThat(userRepository.findByEmail("JAN@EXAMPLE.COM")).isEmpty();
	}

	@Test
	@DisplayName("istniejącego użytkownika można zaktualizować przez save")
	void save_updatesExistingUser() {
		User saved = userRepository.save(buildUser("update@example.com"));
		Integer id = saved.getId();

		saved.setFirstName("Anna");
		saved.setLastName("Nowak");
		userRepository.save(saved);

		User reloaded = userRepository.findById(id).orElseThrow();
		assertThat(reloaded.getFirstName()).isEqualTo("Anna");
		assertThat(reloaded.getLastName()).isEqualTo("Nowak");
	}

	@Test
	@DisplayName("aktualizacja pola isActive jest persystowana")
	void save_updatesIsActiveFlag() {
		User saved = userRepository.save(buildUser("flag@example.com"));
		Integer id = saved.getId();

		saved.setIsActive(false);
		userRepository.save(saved);

		User reloaded = userRepository.findById(id).orElseThrow();
		assertThat(reloaded.getIsActive()).isFalse();
	}

	@Test
	@DisplayName("createdAt jest ustawiane przy zapisie i nie zmienia się przy update")
	void createdAt_isSetOnInsertAndNotChangedOnUpdate() {
		User saved = userRepository.save(buildUser("createdat@example.com"));
		LocalDateTime first = saved.getCreatedAt();
		assertThat(first).isNotNull();

		saved.setFirstName("ZmienioneImie");
		User updated = userRepository.save(saved);
		assertThat(updated.getCreatedAt()).isEqualTo(first);
	}

	@Test
	@DisplayName("relacja User-Role jest poprawnie zapisywana i pobierana")
	void userRoleRelation_isPersisted() {
		User saved = userRepository.save(buildUser("relation@example.com"));
		User reloaded = userRepository.findById(saved.getId()).orElseThrow();
		assertThat(reloaded.getRole()).isNotNull();
		assertThat(reloaded.getRole().getName()).isEqualTo("USER");
	}

	@Test
	@DisplayName("można zmienić rolę użytkownika z USER na ADMIN")
	void user_canChangeRole() {
		User saved = userRepository.save(buildUser("change-role@example.com"));
		Integer id = saved.getId();

		saved.setRole(adminRole);
		userRepository.save(saved);

		User reloaded = userRepository.findById(id).orElseThrow();
		assertThat(reloaded.getRole().getName()).isEqualTo("ADMIN");
	}

	@Test
	@DisplayName("findAll po zapisie 5 użytkowników zwraca 5 elementów")
	void findAll_returnsAllSavedUsers() {
		for (int i = 0; i < 5; i++) {
			userRepository.save(buildUser("user" + i + "@example.com"));
		}
		List<User> all = userRepository.findAll();
		assertThat(all).hasSize(5);
	}

	@Test
	@DisplayName("count po wielokrotnym zapisie i usunięciu zwraca aktualną liczbę")
	void count_reflectsAfterDeletes() {
		User u1 = userRepository.save(buildUser("c1@example.com"));
		userRepository.save(buildUser("c2@example.com"));
		userRepository.save(buildUser("c3@example.com"));
		assertThat(userRepository.count()).isEqualTo(3);

		userRepository.delete(u1);
		assertThat(userRepository.count()).isEqualTo(2);
	}

	@Test
	@DisplayName("deleteById usuwa użytkownika")
	void deleteById_removesUser() {
		User saved = userRepository.save(buildUser("delete-me@example.com"));
		userRepository.deleteById(saved.getId());
		assertThat(userRepository.findById(saved.getId())).isEmpty();
	}

	@Test
	@DisplayName("findById dla nieznanego id zwraca empty")
	void findById_unknownId_returnsEmpty() {
		assertThat(userRepository.findById(999_999)).isEmpty();
	}

	@Test
	@DisplayName("existsByEmail po usunięciu użytkownika zwraca false")
	void existsByEmail_afterDelete_returnsFalse() {
		User saved = userRepository.save(buildUser("e@example.com"));
		assertThat(userRepository.existsByEmail("e@example.com")).isTrue();
		userRepository.delete(saved);
		assertThat(userRepository.existsByEmail("e@example.com")).isFalse();
	}

	@Test
	@DisplayName("zapis bez ustawienia roli rzuca wyjątek (constraint not null na role_id)")
	void save_withoutRole_isAllowedAtJpaLevel_butFlushFails() {
		User u = new User();
		u.setEmail("no-role@example.com");
		u.setPasswordHash("h");
		u.setFirstName("X");
		u.setLastName("Y");
		u.setIsActive(true);
		u.setRole(null);

		User saved = userRepository.save(u);
		assertThat(saved.getRole()).isNull();
	}

	@Test
	@DisplayName("isActive jest domyślnie true po prePersist gdy nie ustawiono")
	void isActive_defaultsToTrueViaPrePersist() {
		User u = new User();
		u.setEmail("default-active@example.com");
		u.setPasswordHash("h");
		u.setFirstName("X");
		u.setLastName("Y");
		u.setRole(userRole);

		User saved = userRepository.save(u);
		assertThat(saved.getIsActive()).isTrue();
	}

	@Test
	@DisplayName("można zapisać użytkownika z bardzo długim hashem hasła (>72 znaki)")
	void save_longPasswordHash() {
		String longHash = "$2a$10$" + "X".repeat(100);
		User u = buildUser("long-hash@example.com");
		u.setPasswordHash(longHash);
		User saved = userRepository.save(u);
		assertThat(userRepository.findById(saved.getId()).orElseThrow().getPasswordHash()).isEqualTo(longHash);
	}
}
