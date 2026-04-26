package com.trainit.backend.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy jednostkowe encji {@link User} skupiające się na akcesorach oraz hooku {@link User#prePersist()}.
 *
 * <p>Encja jest prostym POJO mapowanym na tabelę {@code users}. Sprawdzamy poprawność setterów
 * i getterów oraz wartości domyślne ustawiane przed pierwszym zapisem.
 *
 * @see User
 */
class UserTest {

	@Test
	@DisplayName("getter/setter id zachowuje wartość")
	void idAccessor() {
		User user = new User();
		user.setId(7);
		assertThat(user.getId()).isEqualTo(7);
	}

	@Test
	@DisplayName("getter/setter email zachowuje wartość")
	void emailAccessor() {
		User user = new User();
		user.setEmail("a@b.com");
		assertThat(user.getEmail()).isEqualTo("a@b.com");
	}

	@Test
	@DisplayName("getter/setter passwordHash zachowuje wartość")
	void passwordHashAccessor() {
		User user = new User();
		user.setPasswordHash("hash");
		assertThat(user.getPasswordHash()).isEqualTo("hash");
	}

	@Test
	@DisplayName("getter/setter firstName zachowuje wartość")
	void firstNameAccessor() {
		User user = new User();
		user.setFirstName("Jan");
		assertThat(user.getFirstName()).isEqualTo("Jan");
	}

	@Test
	@DisplayName("getter/setter lastName zachowuje wartość")
	void lastNameAccessor() {
		User user = new User();
		user.setLastName("Kowalski");
		assertThat(user.getLastName()).isEqualTo("Kowalski");
	}

	@Test
	@DisplayName("getter/setter role zachowuje wartość")
	void roleAccessor() {
		User user = new User();
		Role role = new Role();
		role.setName("USER");
		user.setRole(role);
		assertThat(user.getRole()).isSameAs(role);
		assertThat(user.getRole().getName()).isEqualTo("USER");
	}

	@Test
	@DisplayName("getter/setter isActive zachowuje wartość")
	void isActiveAccessor() {
		User user = new User();
		user.setIsActive(true);
		assertThat(user.getIsActive()).isTrue();
		user.setIsActive(false);
		assertThat(user.getIsActive()).isFalse();
		user.setIsActive(null);
		assertThat(user.getIsActive()).isNull();
	}

	@Test
	@DisplayName("getter/setter createdAt zachowuje wartość")
	void createdAtAccessor() {
		User user = new User();
		LocalDateTime now = LocalDateTime.of(2026, 1, 15, 12, 0);
		user.setCreatedAt(now);
		assertThat(user.getCreatedAt()).isEqualTo(now);
	}

	@Test
	@DisplayName("prePersist ustawia createdAt na bieżący czas gdy null")
	void prePersist_setsCreatedAtWhenNull() throws Exception {
		User user = new User();
		assertThat(user.getCreatedAt()).isNull();

		LocalDateTime before = LocalDateTime.now();
		invokePrePersist(user);
		LocalDateTime after = LocalDateTime.now();

		assertThat(user.getCreatedAt()).isNotNull();
		assertThat(user.getCreatedAt()).isBetween(before.minusSeconds(1), after.plusSeconds(1));
	}

	@Test
	@DisplayName("prePersist ustawia isActive na true gdy null")
	void prePersist_setsIsActiveWhenNull() throws Exception {
		User user = new User();
		invokePrePersist(user);
		assertThat(user.getIsActive()).isTrue();
	}

	@Test
	@DisplayName("prePersist nie nadpisuje istniejącego createdAt")
	void prePersist_doesNotOverwriteExistingCreatedAt() throws Exception {
		User user = new User();
		LocalDateTime fixed = LocalDateTime.of(2020, 1, 1, 0, 0);
		user.setCreatedAt(fixed);

		invokePrePersist(user);

		assertThat(user.getCreatedAt()).isEqualTo(fixed);
	}

	@Test
	@DisplayName("prePersist nie nadpisuje isActive=false ustawionego ręcznie")
	void prePersist_doesNotOverwriteExistingIsActive() throws Exception {
		User user = new User();
		user.setIsActive(false);

		invokePrePersist(user);

		assertThat(user.getIsActive()).isFalse();
	}

	/**
	 * Wywołuje pakietowo widoczną metodę {@code prePersist()} przez refleksję.
	 *
	 * @param user encja na której wywołujemy hook
	 * @throws Exception gdy refleksja zawiedzie
	 */
	private static void invokePrePersist(User user) throws Exception {
		Method method = User.class.getDeclaredMethod("prePersist");
		method.setAccessible(true);
		method.invoke(user);
	}
}
