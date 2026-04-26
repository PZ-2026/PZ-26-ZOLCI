package com.trainit.backend.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy jednostkowe encji {@link Role} weryfikujące akcesory.
 *
 * <p>Encja {@link Role} jest prostym POJO mapowanym na tabelę {@code roles} z generowanym
 * identyfikatorem oraz nazwą. Brak logiki — sprawdzamy tylko spójność getterów i setterów.
 */
class RoleTest {

	@Test
	@DisplayName("getter/setter id zachowuje wartość")
	void idAccessor() {
		Role role = new Role();
		role.setId(3);
		assertThat(role.getId()).isEqualTo(3);
	}

	@Test
	@DisplayName("getter/setter name zachowuje wartość")
	void nameAccessor() {
		Role role = new Role();
		role.setName("USER");
		assertThat(role.getName()).isEqualTo("USER");
	}

	@Test
	@DisplayName("nowy obiekt ma wartości null dla wszystkich pól")
	void newRole_hasNullFields() {
		Role role = new Role();
		assertThat(role.getId()).isNull();
		assertThat(role.getName()).isNull();
	}

	@Test
	@DisplayName("setter pozwala zmienić nazwę roli wielokrotnie")
	void nameAccessor_canBeChanged() {
		Role role = new Role();
		role.setName("USER");
		role.setName("ADMIN");
		assertThat(role.getName()).isEqualTo("ADMIN");
	}
}
