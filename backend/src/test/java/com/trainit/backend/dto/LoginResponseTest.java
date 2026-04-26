package com.trainit.backend.dto;

import com.trainit.backend.entity.Role;
import com.trainit.backend.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy jednostkowe rekordu {@link LoginResponse} oraz fabryki {@link LoginResponse#fromEntity(User, String)}.
 *
 * <p>Weryfikują poprawne mapowanie pól z encji {@link User} wraz z dołączeniem tokena
 * przekazanego osobnym argumentem oraz zachowanie {@code null}-safe roli.
 */
class LoginResponseTest {

	@Test
	@DisplayName("fromEntity mapuje wszystkie pola użytkownika i token")
	void fromEntity_mapsAllFields() {
		User user = new User();
		user.setId(1);
		user.setEmail("jan@example.com");
		user.setFirstName("Jan");
		user.setLastName("Kowalski");
		Role role = new Role();
		role.setName("USER");
		user.setRole(role);

		LoginResponse response = LoginResponse.fromEntity(user, "TOKEN");

		assertThat(response.id()).isEqualTo(1);
		assertThat(response.email()).isEqualTo("jan@example.com");
		assertThat(response.firstName()).isEqualTo("Jan");
		assertThat(response.lastName()).isEqualTo("Kowalski");
		assertThat(response.role()).isEqualTo("USER");
		assertThat(response.token()).isEqualTo("TOKEN");
	}

	@Test
	@DisplayName("fromEntity zwraca null dla nazwy roli gdy encja nie ma roli")
	void fromEntity_nullRole_returnsNullRoleName() {
		User user = new User();
		user.setId(1);
		user.setEmail("a@b.com");
		user.setRole(null);

		LoginResponse response = LoginResponse.fromEntity(user, "T");

		assertThat(response.role()).isNull();
	}

	@Test
	@DisplayName("rekord przechowuje wartości przekazane do konstruktora")
	void recordHoldsValues() {
		LoginResponse response = new LoginResponse(2, "x@y.com", "X", "Y", "ADMIN", "TOK");
		assertThat(response.id()).isEqualTo(2);
		assertThat(response.token()).isEqualTo("TOK");
	}

	@Test
	@DisplayName("dwa rekordy z identycznymi wartościami są równe")
	void recordEquality() {
		LoginResponse a = new LoginResponse(1, "a@b.com", "A", "B", "USER", "T");
		LoginResponse b = new LoginResponse(1, "a@b.com", "A", "B", "USER", "T");
		assertThat(a).isEqualTo(b);
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
	}
}
