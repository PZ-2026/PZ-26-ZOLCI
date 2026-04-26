package com.trainit.backend.repository;

import com.trainit.backend.entity.Role;
import com.trainit.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testy integracyjne repozytorium {@link UserRepository} na bazie H2.
 *
 * <p>Adnotacja {@link DataJpaTest} podnosi minimalny kontekst JPA z transakcją wycofywaną po teście.
 * Każdy test najpierw zapisuje rolę {@code USER}, ponieważ użytkownik wskazuje na nią kluczem obcym.
 *
 * @see UserRepository
 * @see User
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	private Role userRole;

	@BeforeEach
	void setUp() {
		userRole = new Role();
		userRole.setName("USER");
		userRole = roleRepository.save(userRole);
	}

	/**
	 * Tworzy poprawną encję użytkownika do zapisu w bazie testowej.
	 *
	 * @param email unikalny adres email
	 * @return nowa, niezapisana encja {@link User}
	 */
	private User buildUser(String email) {
		User user = new User();
		user.setEmail(email);
		user.setPasswordHash("hash");
		user.setFirstName("Jan");
		user.setLastName("Kowalski");
		user.setIsActive(true);
		user.setRole(userRole);
		return user;
	}

	@Test
	@DisplayName("save nadaje wygenerowane id encji")
	void save_assignsId() {
		User saved = userRepository.save(buildUser("a@b.com"));
		assertThat(saved.getId()).isNotNull();
	}

	@Test
	@DisplayName("save zachowuje wszystkie pola w bazie")
	void save_persistsAllFields() {
		User saved = userRepository.save(buildUser("test@test.com"));
		User reloaded = userRepository.findById(saved.getId()).orElseThrow();
		assertThat(reloaded.getEmail()).isEqualTo("test@test.com");
		assertThat(reloaded.getPasswordHash()).isEqualTo("hash");
		assertThat(reloaded.getFirstName()).isEqualTo("Jan");
		assertThat(reloaded.getLastName()).isEqualTo("Kowalski");
		assertThat(reloaded.getIsActive()).isTrue();
		assertThat(reloaded.getRole().getName()).isEqualTo("USER");
	}

	@Test
	@DisplayName("save uruchamia hook prePersist który ustawia createdAt i isActive")
	void save_prePersistFillsDefaults() {
		User user = new User();
		user.setEmail("default@example.com");
		user.setPasswordHash("h");
		user.setFirstName("Anna");
		user.setLastName("Nowak");
		user.setRole(userRole);

		User saved = userRepository.save(user);

		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getIsActive()).isTrue();
	}

	@Test
	@DisplayName("existsByEmail zwraca true dla istniejącego emaila")
	void existsByEmail_returnsTrueForExisting() {
		userRepository.save(buildUser("jan@example.com"));
		assertThat(userRepository.existsByEmail("jan@example.com")).isTrue();
	}

	@Test
	@DisplayName("existsByEmail zwraca false dla nieznanego emaila")
	void existsByEmail_returnsFalseForUnknown() {
		assertThat(userRepository.existsByEmail("brak@brak.com")).isFalse();
	}

	@Test
	@DisplayName("existsByEmail jest case-sensitive (logika normalizacji jest w serwisie)")
	void existsByEmail_isCaseSensitive() {
		userRepository.save(buildUser("jan@example.com"));
		assertThat(userRepository.existsByEmail("JAN@example.com")).isFalse();
	}

	@Test
	@DisplayName("findByEmail zwraca użytkownika dla istniejącego emaila")
	void findByEmail_returnsUserForExisting() {
		userRepository.save(buildUser("anna@example.com"));
		Optional<User> found = userRepository.findByEmail("anna@example.com");
		assertThat(found).isPresent();
		assertThat(found.get().getEmail()).isEqualTo("anna@example.com");
		assertThat(found.get().getRole().getName()).isEqualTo("USER");
	}

	@Test
	@DisplayName("findByEmail zwraca empty dla nieznanego emaila")
	void findByEmail_returnsEmptyForUnknown() {
		assertThat(userRepository.findByEmail("brak@brak.com")).isEmpty();
	}

	@Test
	@DisplayName("zapis duplikatu emaila narusza unikalność i rzuca wyjątek")
	void save_duplicateEmail_throwsDataIntegrityViolation() {
		userRepository.save(buildUser("dup@example.com"));
		assertThatThrownBy(() -> {
			userRepository.save(buildUser("dup@example.com"));
			userRepository.flush();
		}).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("count zwraca 0 dla pustej tabeli i 2 po dodaniu dwóch użytkowników")
	void count_reflectsRowsCount() {
		assertThat(userRepository.count()).isZero();
		userRepository.save(buildUser("a@a.com"));
		userRepository.save(buildUser("b@b.com"));
		assertThat(userRepository.count()).isEqualTo(2);
	}

	@Test
	@DisplayName("delete usuwa użytkownika z bazy")
	void delete_removesUser() {
		User saved = userRepository.save(buildUser("del@example.com"));
		userRepository.delete(saved);
		assertThat(userRepository.findById(saved.getId())).isEmpty();
	}

	@Test
	@DisplayName("findAll zwraca wszystkich zapisanych użytkowników")
	void findAll_returnsAllUsers() {
		userRepository.save(buildUser("u1@example.com"));
		userRepository.save(buildUser("u2@example.com"));
		assertThat(userRepository.findAll()).hasSize(2);
	}
}
