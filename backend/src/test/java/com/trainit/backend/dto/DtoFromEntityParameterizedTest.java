package com.trainit.backend.dto;

import com.trainit.backend.entity.Role;
import com.trainit.backend.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Parametryzowane testy mapowania encji {@link User} na DTO {@link UserResponse} i {@link LoginResponse}.
 *
 * <p>Sprawdzają, że dla różnych zestawów wartości encji mapowanie zachowuje wszystkie pola
 * oraz że brak roli nie powoduje wyjątku.
 */
class DtoFromEntityParameterizedTest {

	/**
	 * Buduje encję użytkownika z podanymi wartościami i opcjonalną rolą.
	 *
	 * @param id identyfikator
	 * @param email email
	 * @param firstName imię
	 * @param lastName nazwisko
	 * @param roleName nazwa roli lub {@code null} jeśli brak roli
	 * @return wypełniona encja {@link User}
	 */
	private static User user(Integer id, String email, String firstName, String lastName, String roleName) {
		User user = new User();
		user.setId(id);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		if (roleName != null) {
			Role role = new Role();
			role.setName(roleName);
			user.setRole(role);
		}
		return user;
	}

	@ParameterizedTest(name = "UserResponse.fromEntity({0}, {1}, {2}, {3}, {4})")
	@CsvSource({
			"1, jan@example.com, Jan, Kowalski, USER",
			"2, anna@example.com, Anna, Nowak, USER",
			"3, admin@example.com, Admin, Adminsk, ADMIN",
			"4, x@y.z, X, Y, USER",
			"5, very.long.email.address@subdomain.example.com, BardzoDlugieImie, BardzoDlugieNazwisko, USER"
	})
	@DisplayName("UserResponse.fromEntity mapuje wszystkie pola dla różnych zestawów wartości")
	void userResponse_mapsAllFields(int id, String email, String firstName, String lastName, String roleName) {
		User user = user(id, email, firstName, lastName, roleName);
		UserResponse response = UserResponse.fromEntity(user);
		assertThat(response.id()).isEqualTo(id);
		assertThat(response.email()).isEqualTo(email);
		assertThat(response.firstName()).isEqualTo(firstName);
		assertThat(response.lastName()).isEqualTo(lastName);
		assertThat(response.role()).isEqualTo(roleName);
	}

	@ParameterizedTest(name = "LoginResponse.fromEntity z tokenem '{5}'")
	@CsvSource({
			"1, jan@example.com, Jan, Kowalski, USER, TOKEN-1",
			"2, anna@example.com, Anna, Nowak, USER, JWT.eyJzdWIiOiIyIn0.x",
			"3, admin@example.com, Admin, Admin, ADMIN, ABC123",
			"4, x@y.z, X, Y, USER, ''",
			"5, brak.token@x.com, A, B, USER, very-long-token-with-many-characters-and-dots.and.more"
	})
	@DisplayName("LoginResponse.fromEntity mapuje pola encji i token niezależnie")
	void loginResponse_mapsAllFields(int id, String email, String firstName, String lastName, String roleName, String token) {
		User user = user(id, email, firstName, lastName, roleName);
		LoginResponse response = LoginResponse.fromEntity(user, token);
		assertThat(response.id()).isEqualTo(id);
		assertThat(response.email()).isEqualTo(email);
		assertThat(response.firstName()).isEqualTo(firstName);
		assertThat(response.lastName()).isEqualTo(lastName);
		assertThat(response.role()).isEqualTo(roleName);
		assertThat(response.token()).isEqualTo(token);
	}

	@ParameterizedTest(name = "fromEntity bez roli przekazuje null jako role")
	@ValueSource(strings = {"a@b.com", "anna@example.com", "x@y.z"})
	@DisplayName("UserResponse.fromEntity zwraca null role gdy User.role==null")
	void userResponse_nullRole(String email) {
		User user = user(1, email, "X", "Y", null);
		UserResponse response = UserResponse.fromEntity(user);
		assertThat(response.role()).isNull();
		assertThat(response.email()).isEqualTo(email);
	}

	@ParameterizedTest(name = "LoginResponse z null role i tokenem '{0}'")
	@ValueSource(strings = {"TOKEN", "X", "long.token.value"})
	@DisplayName("LoginResponse.fromEntity z null role zachowuje token")
	void loginResponse_nullRole_keepsToken(String token) {
		User user = user(1, "a@b.com", "X", "Y", null);
		LoginResponse response = LoginResponse.fromEntity(user, token);
		assertThat(response.role()).isNull();
		assertThat(response.token()).isEqualTo(token);
	}

	@ParameterizedTest(name = "LoginResponse z null tokenem dla emaila '{0}'")
	@NullSource
	@DisplayName("LoginResponse.fromEntity akceptuje null jako token")
	void loginResponse_nullToken_isAccepted(String token) {
		User user = user(1, "a@b.com", "X", "Y", "USER");
		LoginResponse response = LoginResponse.fromEntity(user, token);
		assertThat(response.token()).isNull();
		assertThat(response.email()).isEqualTo("a@b.com");
	}

	@ParameterizedTest(name = "rekord równa się sam sobie i swojej kopii")
	@CsvSource({
			"1, a@b.com, A, B, USER",
			"2, c@d.com, C, D, ADMIN",
			"3, x@y.z,   X, Y, USER"
	})
	@DisplayName("UserResponse zachowuje semantykę record (equals/hashCode)")
	void userResponse_recordSemantics(int id, String email, String firstName, String lastName, String roleName) {
		UserResponse a = new UserResponse(id, email, firstName, lastName, roleName);
		UserResponse b = new UserResponse(id, email, firstName, lastName, roleName);
		assertThat(a).isEqualTo(b);
		assertThat(a).isEqualTo(a);
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
		assertThat(a.toString()).isEqualTo(b.toString());
	}

	@ParameterizedTest(name = "LoginResponse equals/hashCode dla {0}")
	@CsvSource({
			"1, a@b.com, A, B, USER, T",
			"2, c@d.com, C, D, ADMIN, X",
			"3, x@y.z,   X, Y, USER, ABC"
	})
	@DisplayName("LoginResponse zachowuje semantykę record (equals/hashCode)")
	void loginResponse_recordSemantics(int id, String email, String firstName, String lastName, String roleName, String token) {
		LoginResponse a = new LoginResponse(id, email, firstName, lastName, roleName, token);
		LoginResponse b = new LoginResponse(id, email, firstName, lastName, roleName, token);
		assertThat(a).isEqualTo(b);
		assertThat(a).isEqualTo(a);
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
	}
}
