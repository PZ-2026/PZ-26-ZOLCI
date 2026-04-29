package com.trainit.backend.service;

import com.trainit.backend.dto.LoginRequest;
import com.trainit.backend.dto.LoginResponse;
import com.trainit.backend.dto.RegisterRequest;
import com.trainit.backend.dto.UserResponse;
import com.trainit.backend.entity.Role;
import com.trainit.backend.entity.User;
import com.trainit.backend.exception.EmailAlreadyExistsException;
import com.trainit.backend.exception.InvalidCredentialsException;
import com.trainit.backend.repository.RoleRepository;
import com.trainit.backend.repository.UserRepository;
import com.trainit.backend.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Dodatkowe testy edge case'ów {@link AuthService}.
 *
 * <p>Klasa uzupełnia {@link AuthServiceTest} o testy parametryzowane oraz scenariusze
 * graniczne — takie jak różne kombinacje wielkości liter w emailu, różna długość imion
 * i nazwisk, oraz wielokrotne wywołania kolejno po sobie.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceEdgeCasesTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private BCryptPasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@InjectMocks
	private AuthService authService;

	/**
	 * Tworzy aktywnego użytkownika z rolą {@code USER}.
	 *
	 * @param email email
	 * @return encja użytkownika z aktywnym statusem
	 */
	private static User user(String email) {
		User u = new User();
		u.setEmail(email);
		u.setPasswordHash("hash");
		u.setIsActive(true);
		u.setFirstName("Jan");
		u.setLastName("Kowalski");
		Role role = new Role();
		role.setName("USER");
		u.setRole(role);
		return u;
	}

	/**
	 * Tworzy żądanie rejestracyjne.
	 *
	 * @param email email do rejestracji
	 * @return wypełniony {@link RegisterRequest}
	 */
	private static RegisterRequest regReq(String email) {
		RegisterRequest r = new RegisterRequest();
		r.setEmail(email);
		r.setPassword("Haslo123!");
		r.setFirstName("Jan");
		r.setLastName("Kowalski");
		return r;
	}

	/**
	 * Tworzy żądanie logowania.
	 *
	 * @param email email
	 * @param password hasło
	 * @return wypełniony {@link LoginRequest}
	 */
	private static LoginRequest logReq(String email, String password) {
		LoginRequest r = new LoginRequest();
		r.setEmail(email);
		r.setPassword(password);
		return r;
	}

	@ParameterizedTest(name = "email '{0}' normalizowany do '{1}'")
	@CsvSource({
			"jan@example.com,         jan@example.com",
			"JAN@EXAMPLE.COM,         jan@example.com",
			"Jan@Example.Com,         jan@example.com",
			"  jan@example.com,       jan@example.com",
			"jan@example.com  ,       jan@example.com",
			"  JAN@EXAMPLE.COM  ,     jan@example.com"
	})
	@DisplayName("rejestracja: email jest normalizowany (trim + lowercase) niezależnie od formatu")
	void register_normalizesEmailVariations(String inputEmail, String expectedEmail) {
		Role role = new Role();
		role.setName("USER");
		when(userRepository.existsByEmail(expectedEmail)).thenReturn(false);
		when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
		when(passwordEncoder.encode(anyString())).thenReturn("h");
		when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		authService.register(regReq(inputEmail));

		verify(userRepository).existsByEmail(expectedEmail);
		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		assertThat(captor.getValue().getEmail()).isEqualTo(expectedEmail);
	}

	@ParameterizedTest(name = "email '{0}' -> szukane '{1}'")
	@CsvSource({
			"jan@example.com,         jan@example.com",
			"JAN@EXAMPLE.COM,         jan@example.com",
			"  jan@example.com,       jan@example.com",
			"  JAN@EXAMPLE.COM  ,     jan@example.com"
	})
	@DisplayName("logowanie: email jest normalizowany przed wyszukiwaniem")
	void login_normalizesEmailVariations(String inputEmail, String expectedSearchEmail) {
		when(userRepository.findByEmail(expectedSearchEmail)).thenReturn(Optional.of(user("jan@example.com")));
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
		when(jwtService.generateToken(any(), anyString(), anyString())).thenReturn("token-1");

		authService.login(logReq(inputEmail, "p"));

		verify(userRepository).findByEmail(expectedSearchEmail);
	}

	@ParameterizedTest(name = "imie '{0}' przycinane do '{1}'")
	@CsvSource({
			"Jan,            Jan",
			"  Jan,          Jan",
			"Jan  ,          Jan",
			"  Jan  ,        Jan",
			"Krzysztof,      Krzysztof"
	})
	@DisplayName("rejestracja: imię i nazwisko są przycinane z białych znaków")
	void register_trimsFirstAndLastName(String input, String expected) {
		Role role = new Role();
		role.setName("USER");
		when(userRepository.existsByEmail(anyString())).thenReturn(false);
		when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
		when(passwordEncoder.encode(anyString())).thenReturn("h");
		when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		RegisterRequest req = regReq("jan@example.com");
		req.setFirstName(input);
		req.setLastName(input);

		authService.register(req);

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		assertThat(captor.getValue().getFirstName()).isEqualTo(expected);
		assertThat(captor.getValue().getLastName()).isEqualTo(expected);
	}

	@ParameterizedTest(name = "kolizja na emailu '{0}'")
	@ValueSource(strings = {
			"jan@example.com",
			"anna.nowak@example.org",
			"long.email.address@subdomain.example.com",
			"x@y.z"
	})
	@DisplayName("rejestracja: dla każdego zajętego emaila rzucamy EmailAlreadyExistsException")
	void register_duplicateForVariousEmails_throws(String email) {
		when(userRepository.existsByEmail(email)).thenReturn(true);
		assertThatThrownBy(() -> authService.register(regReq(email)))
				.isInstanceOf(EmailAlreadyExistsException.class);
		verify(userRepository, never()).save(any());
	}

	@ParameterizedTest(name = "logowanie: scenariusz {0}")
	@CsvSource({
			"VALID,                jan@example.com,  Haslo123!",
			"WRONG_PASSWORD,       jan@example.com,  ZleHaslo",
			"UNKNOWN_EMAIL,        nieznany@x.com,   Haslo123!",
			"INACTIVE,             jan@example.com,  Haslo123!"
	})
	@DisplayName("logowanie: każdy scenariusz daje odpowiednią reakcję serwisu")
	void login_variousScenarios(String scenario, String email, String password) {
		switch (scenario) {
			case "VALID" -> {
				when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user(email)));
				when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
				when(jwtService.generateToken(any(), anyString(), anyString())).thenReturn("token-1");
				LoginResponse r = authService.login(logReq(email, password));
				assertThat(r.token()).isNotBlank();
			}
			case "WRONG_PASSWORD" -> {
				when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user(email)));
				when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
				assertThatThrownBy(() -> authService.login(logReq(email, password)))
						.isInstanceOf(InvalidCredentialsException.class);
			}
			case "UNKNOWN_EMAIL" -> {
				when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
				assertThatThrownBy(() -> authService.login(logReq(email, password)))
						.isInstanceOf(InvalidCredentialsException.class);
			}
			case "INACTIVE" -> {
				User u = user(email);
				u.setIsActive(false);
				when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(u));
				when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
				assertThatThrownBy(() -> authService.login(logReq(email, password)))
						.isInstanceOf(InvalidCredentialsException.class);
			}
		}
	}

	@org.junit.jupiter.api.Test
	@DisplayName("rejestracja: hasło o długości 8 znaków jest hashowane bez modyfikacji długości")
	void register_minLengthPassword_isPassedToEncoder() {
		Role role = new Role();
		role.setName("USER");
		when(userRepository.existsByEmail(anyString())).thenReturn(false);
		when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
		when(passwordEncoder.encode("12345678")).thenReturn("h");
		when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		RegisterRequest req = regReq("a@b.com");
		req.setPassword("12345678");
		authService.register(req);

		verify(passwordEncoder).encode("12345678");
	}

	@org.junit.jupiter.api.Test
	@DisplayName("rejestracja: kolejne wywołania nie współdzielą stanu i każde idzie do save")
	void register_multipleCalls_independentState() {
		Role role = new Role();
		role.setName("USER");
		when(userRepository.existsByEmail(anyString())).thenReturn(false);
		when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
		when(passwordEncoder.encode(anyString())).thenReturn("h");
		when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		UserResponse r1 = authService.register(regReq("user1@example.com"));
		UserResponse r2 = authService.register(regReq("user2@example.com"));
		UserResponse r3 = authService.register(regReq("user3@example.com"));

		assertThat(r1.email()).isEqualTo("user1@example.com");
		assertThat(r2.email()).isEqualTo("user2@example.com");
		assertThat(r3.email()).isEqualTo("user3@example.com");
		verify(userRepository, times(3)).save(any());
	}

	@org.junit.jupiter.api.Test
	@DisplayName("logowanie: nieaktywne konto nie odsłania informacji o tym że istnieje (ten sam wyjątek co przy braku użytkownika)")
	void login_inactiveAndUnknown_throwSameException() {
		User inactive = user("active@x.com");
		inactive.setIsActive(false);
		when(userRepository.findByEmail("active@x.com")).thenReturn(Optional.of(inactive));
		when(userRepository.findByEmail("unknown@x.com")).thenReturn(Optional.empty());
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

		Throwable inactiveEx = org.junit.jupiter.api.Assertions.assertThrows(
				InvalidCredentialsException.class,
				() -> authService.login(logReq("active@x.com", "p")));
		Throwable unknownEx = org.junit.jupiter.api.Assertions.assertThrows(
				InvalidCredentialsException.class,
				() -> authService.login(logReq("unknown@x.com", "p")));

		assertThat(inactiveEx.getClass()).isEqualTo(unknownEx.getClass());
		assertThat(inactiveEx.getMessage()).isEqualTo(unknownEx.getMessage());
	}
}
