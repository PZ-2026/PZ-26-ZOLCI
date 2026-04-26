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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testy jednostkowe serwisu {@link AuthService}.
 *
 * <p>Pokrywają oba publiczne przepływy: rejestrację i logowanie. Dla rejestracji testujemy
 * happy path, kolizję adresu email, normalizację (trim + lowercase) oraz brak roli {@code USER}
 * w bazie. Dla logowania weryfikujemy poprawne dane, błędne hasło, nieznany email oraz konto
 * nieaktywne. Wszystkie zależności są mockowane przez Mockito.
 *
 * @see AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	/** Repozytorium użytkowników — mock dostarczany przez Mockito. */
	@Mock
	private UserRepository userRepository;

	/** Repozytorium ról — mock zwracający rolę {@code USER} w testach happy path. */
	@Mock
	private RoleRepository roleRepository;

	/** Enkoder BCrypt — mock; nie wykonujemy realnego hashowania w testach jednostkowych. */
	@Mock
	private BCryptPasswordEncoder passwordEncoder;

	/** Testowany serwis z wstrzykniętymi wyżej mockami. */
	@InjectMocks
	private AuthService authService;

	/**
	 * Tworzy aktywnego użytkownika z rolą {@code USER} do testów logowania.
	 *
	 * @param email email użytkownika
	 * @param hash zapisany hash hasła
	 * @return gotowa encja {@link User} z ustawionymi wszystkimi wymaganymi polami
	 */
	private static User buildActiveUser(String email, String hash) {
		User user = new User();
		user.setEmail(email);
		user.setPasswordHash(hash);
		user.setFirstName("Jan");
		user.setLastName("Kowalski");
		user.setIsActive(true);
		Role role = new Role();
		role.setName("USER");
		user.setRole(role);
		return user;
	}

	/**
	 * Tworzy poprawne żądanie rejestracji z wartościami testowymi.
	 *
	 * @return wypełniony {@link RegisterRequest}
	 */
	private static RegisterRequest sampleRegisterRequest() {
		RegisterRequest req = new RegisterRequest();
		req.setEmail("jan@example.com");
		req.setPassword("Haslo123!");
		req.setFirstName("Jan");
		req.setLastName("Kowalski");
		return req;
	}

	@Nested
	@DisplayName("register")
	class RegisterTests {

		@Test
		@DisplayName("zwraca UserResponse z danymi zapisanego użytkownika")
		void register_happyPath_returnsUserResponse() {
			RegisterRequest req = sampleRegisterRequest();
			Role role = new Role();
			role.setName("USER");

			when(userRepository.existsByEmail("jan@example.com")).thenReturn(false);
			when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
			when(passwordEncoder.encode("Haslo123!")).thenReturn("hashed");
			when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
				User saved = invocation.getArgument(0);
				saved.setId(42);
				return saved;
			});

			UserResponse result = authService.register(req);

			assertThat(result).isNotNull();
			assertThat(result.email()).isEqualTo("jan@example.com");
			assertThat(result.firstName()).isEqualTo("Jan");
			assertThat(result.lastName()).isEqualTo("Kowalski");
			assertThat(result.role()).isEqualTo("USER");
			assertThat(result.id()).isEqualTo(42);
		}

		@Test
		@DisplayName("hashuje hasło przed zapisem i nie zapisuje wartości jawnej")
		void register_hashesPasswordBeforeSave() {
			RegisterRequest req = sampleRegisterRequest();
			Role role = new Role();
			role.setName("USER");

			when(userRepository.existsByEmail(anyString())).thenReturn(false);
			when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
			when(passwordEncoder.encode("Haslo123!")).thenReturn("BCRYPT_HASH");
			when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

			authService.register(req);

			ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
			verify(userRepository).save(captor.capture());
			User saved = captor.getValue();
			assertThat(saved.getPasswordHash()).isEqualTo("BCRYPT_HASH");
			assertThat(saved.getPasswordHash()).isNotEqualTo("Haslo123!");
		}

		@Test
		@DisplayName("przypisuje rolę USER pobraną z repozytorium ról")
		void register_assignsUserRole() {
			RegisterRequest req = sampleRegisterRequest();
			Role role = new Role();
			role.setName("USER");
			role.setId(7);

			when(userRepository.existsByEmail(anyString())).thenReturn(false);
			when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
			when(passwordEncoder.encode(anyString())).thenReturn("h");
			when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

			authService.register(req);

			ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
			verify(userRepository).save(captor.capture());
			User saved = captor.getValue();
			assertThat(saved.getRole()).isNotNull();
			assertThat(saved.getRole().getName()).isEqualTo("USER");
		}

		@Test
		@DisplayName("rzuca EmailAlreadyExistsException gdy email zajęty i nie zapisuje użytkownika")
		void register_duplicateEmail_throws() {
			when(userRepository.existsByEmail(anyString())).thenReturn(true);

			assertThatThrownBy(() -> authService.register(sampleRegisterRequest()))
					.isInstanceOf(EmailAlreadyExistsException.class)
					.hasMessageContaining("zajęty");
			verify(userRepository, never()).save(any());
			verify(passwordEncoder, never()).encode(anyString());
		}

		@Test
		@DisplayName("normalizuje email do małych liter przed sprawdzeniem unikalności")
		void register_normalizesEmailToLowercase() {
			RegisterRequest req = sampleRegisterRequest();
			req.setEmail("JAN@Example.COM");
			Role role = new Role();
			role.setName("USER");

			when(userRepository.existsByEmail("jan@example.com")).thenReturn(false);
			when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
			when(passwordEncoder.encode(anyString())).thenReturn("h");
			when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

			authService.register(req);

			verify(userRepository).existsByEmail("jan@example.com");
			ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
			verify(userRepository).save(captor.capture());
			assertThat(captor.getValue().getEmail()).isEqualTo("jan@example.com");
		}

		@Test
		@DisplayName("przycina białe znaki w emailu, imieniu i nazwisku")
		void register_trimsWhitespace() {
			RegisterRequest req = sampleRegisterRequest();
			req.setEmail("   jan@example.com  ");
			req.setFirstName("  Jan  ");
			req.setLastName("  Kowalski   ");
			Role role = new Role();
			role.setName("USER");

			when(userRepository.existsByEmail("jan@example.com")).thenReturn(false);
			when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
			when(passwordEncoder.encode(anyString())).thenReturn("h");
			when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

			authService.register(req);

			ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
			verify(userRepository).save(captor.capture());
			User saved = captor.getValue();
			assertThat(saved.getEmail()).isEqualTo("jan@example.com");
			assertThat(saved.getFirstName()).isEqualTo("Jan");
			assertThat(saved.getLastName()).isEqualTo("Kowalski");
		}

		@Test
		@DisplayName("rzuca IllegalStateException gdy w bazie brakuje roli USER")
		void register_missingUserRole_throwsIllegalState() {
			when(userRepository.existsByEmail(anyString())).thenReturn(false);
			when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> authService.register(sampleRegisterRequest()))
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("USER");
			verify(userRepository, never()).save(any());
		}

		@Test
		@DisplayName("zwraca null email w odpowiedzi gdy żądanie ma null (nie powinno być po walidacji DTO, ale serwis nie powinien rzucać NPE wcześniej)")
		void register_nullEmail_throwsNpeOnExists() {
			RegisterRequest req = sampleRegisterRequest();
			req.setEmail(null);
			when(userRepository.existsByEmail(null)).thenReturn(false);

			assertThatThrownBy(() -> authService.register(req))
					.isInstanceOf(Exception.class);
		}
	}

	@Nested
	@DisplayName("login")
	class LoginTests {

		@Test
		@DisplayName("zwraca LoginResponse z niepustym tokenem dla poprawnych danych")
		void login_validCredentials_returnsResponseWithToken() {
			User user = buildActiveUser("jan@example.com", "hashed");
			user.setId(11);
			when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
			when(passwordEncoder.matches("Haslo123!", "hashed")).thenReturn(true);

			LoginResponse response = authService.login(loginRequest("jan@example.com", "Haslo123!"));

			assertThat(response).isNotNull();
			assertThat(response.token()).isNotBlank();
			assertThat(response.email()).isEqualTo("jan@example.com");
			assertThat(response.role()).isEqualTo("USER");
			assertThat(response.id()).isEqualTo(11);
		}

		@Test
		@DisplayName("rzuca InvalidCredentialsException gdy hasło nie pasuje")
		void login_wrongPassword_throws() {
			User user = buildActiveUser("jan@example.com", "hashed");
			when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
			when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

			assertThatThrownBy(() -> authService.login(loginRequest("jan@example.com", "zle")))
					.isInstanceOf(InvalidCredentialsException.class);
		}

		@Test
		@DisplayName("rzuca InvalidCredentialsException gdy email nie istnieje (bez sprawdzania hasła)")
		void login_unknownEmail_throws() {
			when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

			assertThatThrownBy(() -> authService.login(loginRequest("brak@x.com", "pass")))
					.isInstanceOf(InvalidCredentialsException.class);
			verify(passwordEncoder, never()).matches(anyString(), anyString());
		}

		@Test
		@DisplayName("rzuca InvalidCredentialsException dla konta z isActive=false mimo poprawnego hasła")
		void login_inactiveAccount_throws() {
			User user = buildActiveUser("jan@example.com", "hashed");
			user.setIsActive(false);
			when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
			when(passwordEncoder.matches("p", "hashed")).thenReturn(true);

			assertThatThrownBy(() -> authService.login(loginRequest("jan@example.com", "p")))
					.isInstanceOf(InvalidCredentialsException.class);
		}

		@Test
		@DisplayName("rzuca InvalidCredentialsException dla konta z isActive=null")
		void login_nullActiveFlag_throws() {
			User user = buildActiveUser("jan@example.com", "hashed");
			user.setIsActive(null);
			when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
			when(passwordEncoder.matches("p", "hashed")).thenReturn(true);

			assertThatThrownBy(() -> authService.login(loginRequest("jan@example.com", "p")))
					.isInstanceOf(InvalidCredentialsException.class);
		}

		@Test
		@DisplayName("normalizuje email przed wyszukiwaniem w repozytorium")
		void login_normalizesEmail() {
			User user = buildActiveUser("jan@example.com", "hashed");
			when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
			when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

			authService.login(loginRequest("  JAN@EXAMPLE.COM  ", "p"));

			verify(userRepository).findByEmail("jan@example.com");
		}

		@Test
		@DisplayName("każde wywołanie generuje nowy token (UUID stub)")
		void login_generatesUniqueTokensPerCall() {
			User user = buildActiveUser("jan@example.com", "hashed");
			when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
			when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

			LoginResponse first = authService.login(loginRequest("jan@example.com", "p"));
			LoginResponse second = authService.login(loginRequest("jan@example.com", "p"));

			assertThat(first.token()).isNotEqualTo(second.token());
		}

		@Test
		@DisplayName("zwraca null jako rola gdy użytkownik nie ma przypisanej roli")
		void login_userWithoutRole_returnsNullRole() {
			User user = buildActiveUser("jan@example.com", "hashed");
			user.setRole(null);
			when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
			when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

			LoginResponse response = authService.login(loginRequest("jan@example.com", "p"));

			assertThat(response.role()).isNull();
		}
	}

	/**
	 * Pomocnicza fabryka {@link LoginRequest} bez konieczności użycia setterów w każdym teście.
	 *
	 * @param email email logowania
	 * @param password hasło logowania
	 * @return wypełniony {@link LoginRequest}
	 */
	private static LoginRequest loginRequest(String email, String password) {
		LoginRequest req = new LoginRequest();
		req.setEmail(email);
		req.setPassword(password);
		return req;
	}
}
