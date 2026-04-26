package com.trainit.backend.dto;

import com.trainit.backend.entity.Role;
import com.trainit.backend.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy jednostkowe rekordu {@link UserResponse} oraz statycznej fabryki {@link UserResponse#fromEntity(User)}.
 *
 * <p>Sprawdzają poprawne mapowanie pól z encji oraz zachowanie {@code null}-safe gdy użytkownik
 * nie ma przypisanej roli.
 */
class UserResponseTest {

	@Test
	@DisplayName("fromEntity mapuje wszystkie pola użytkownika z rolą USER")
	void fromEntity_mapsAllFields() {
		User user = new User();
		user.setId(1);
		user.setEmail("jan@example.com");
		user.setFirstName("Jan");
		user.setLastName("Kowalski");
		Role role = new Role();
		role.setName("USER");
		user.setRole(role);

		UserResponse response = UserResponse.fromEntity(user);

		assertThat(response.id()).isEqualTo(1);
		assertThat(response.email()).isEqualTo("jan@example.com");
		assertThat(response.firstName()).isEqualTo("Jan");
		assertThat(response.lastName()).isEqualTo("Kowalski");
		assertThat(response.role()).isEqualTo("USER");
	}

	@Test
	@DisplayName("fromEntity zwraca null dla roli gdy encja nie ma przypisanej roli")
	void fromEntity_nullRole_returnsNullRoleName() {
		User user = new User();
		user.setId(1);
		user.setEmail("a@b.com");
		user.setFirstName("A");
		user.setLastName("B");
		user.setRole(null);

		UserResponse response = UserResponse.fromEntity(user);

		assertThat(response.role()).isNull();
	}

	@Test
	@DisplayName("rekord przechowuje wartości przekazane do konstruktora")
	void recordHoldsValues() {
		UserResponse response = new UserResponse(5, "x@y.com", "X", "Y", "ADMIN");
		assertThat(response.id()).isEqualTo(5);
		assertThat(response.email()).isEqualTo("x@y.com");
		assertThat(response.firstName()).isEqualTo("X");
		assertThat(response.lastName()).isEqualTo("Y");
		assertThat(response.role()).isEqualTo("ADMIN");
	}

	@Test
	@DisplayName("dwa rekordy z identycznymi wartościami są równe")
	void recordEquality() {
		UserResponse a = new UserResponse(1, "a@b.com", "A", "B", "USER");
		UserResponse b = new UserResponse(1, "a@b.com", "A", "B", "USER");
		assertThat(a).isEqualTo(b);
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
	}

	@Test
	@DisplayName("toString rekordu zawiera wszystkie pola")
	void recordToString() {
		UserResponse response = new UserResponse(1, "a@b.com", "A", "B", "USER");
		String str = response.toString();
		assertThat(str).contains("a@b.com").contains("USER").contains("A").contains("B");
	}
}
