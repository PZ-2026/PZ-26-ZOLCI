package com.trainit.backend.integration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.trainit.backend.dto.LoginRequest;
import com.trainit.backend.dto.RegisterRequest;
import com.trainit.backend.entity.Role;
import com.trainit.backend.entity.User;
import com.trainit.backend.repository.RoleRepository;
import com.trainit.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testy integracyjne pełnego stosu uwierzytelniania na bazie H2.
 *
 * <p>Wykorzystują pełen kontekst Spring Boot ({@code @SpringBootTest}), wbudowany {@link MockMvc}
 * oraz transakcyjne wycofanie po teście (@{@link Transactional}). Sprawdzają flow:
 * rejestracja → próba duplikatu → logowanie poprawne → logowanie błędne → konto nieaktywne.
 *
 * @see com.trainit.backend.controller.AuthController
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@BeforeEach
	void ensureUserRoleExists() {
		if (roleRepository.findByName("USER").isEmpty()) {
			Role role = new Role();
			role.setName("USER");
			roleRepository.save(role);
		}
	}

	/**
	 * Buduje DTO {@link RegisterRequest} z poprawnymi wartościami.
	 *
	 * @param email email do rejestracji
	 * @return wypełniony {@link RegisterRequest}
	 */
	private static RegisterRequest registerRequest(String email) {
		RegisterRequest req = new RegisterRequest();
		req.setEmail(email);
		req.setPassword("Haslo123!");
		req.setFirstName("Jan");
		req.setLastName("Kowalski");
		return req;
	}

	/**
	 * Buduje DTO {@link LoginRequest} z poprawnymi wartościami.
	 *
	 * @param email email do logowania
	 * @param password hasło do logowania
	 * @return wypełniony {@link LoginRequest}
	 */
	private static LoginRequest loginRequest(String email, String password) {
		LoginRequest req = new LoginRequest();
		req.setEmail(email);
		req.setPassword(password);
		return req;
	}

	@Test
	@DisplayName("rejestracja nowego użytkownika zwraca 201 i zapisuje rekord w bazie")
	void register_createsUserInDatabase() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerRequest("integ1@example.com"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("integ1@example.com"))
				.andExpect(jsonPath("$.role").value("USER"));

		assertThat(userRepository.findByEmail("integ1@example.com")).isPresent();
	}

	@Test
	@DisplayName("rejestracja zapisuje hash zamiast hasła jawnego")
	void register_storesBcryptHash() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerRequest("integ2@example.com"))))
				.andExpect(status().isCreated());

		User saved = userRepository.findByEmail("integ2@example.com").orElseThrow();
		assertThat(saved.getPasswordHash()).isNotEqualTo("Haslo123!");
		assertThat(saved.getPasswordHash()).startsWith("$2");
		assertThat(passwordEncoder.matches("Haslo123!", saved.getPasswordHash())).isTrue();
	}

	@Test
	@DisplayName("rejestracja na zajęty email zwraca 409")
	void register_duplicateEmail_returns409() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerRequest("dup@example.com"))))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerRequest("dup@example.com"))))
				.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("logowanie poprawnymi danymi po rejestracji zwraca 200 i niepusty token")
	void login_afterRegister_returnsToken() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerRequest("login1@example.com"))))
				.andExpect(status().isCreated());

		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest("login1@example.com", "Haslo123!"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists())
				.andExpect(jsonPath("$.email").value("login1@example.com"))
				.andReturn();

		JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
		assertThat(body.get("token").asText()).isNotBlank();
	}

	@Test
	@DisplayName("logowanie z błędnym hasłem zwraca 401")
	void login_wrongPassword_returns401() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerRequest("login2@example.com"))))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest("login2@example.com", "ZleHaslo!"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("logowanie nieznanego użytkownika zwraca 401")
	void login_unknownUser_returns401() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest("brak@example.com", "Haslo123!"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("logowanie z konta nieaktywnego zwraca 401")
	void login_inactiveAccount_returns401() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerRequest("blocked@example.com"))))
				.andExpect(status().isCreated());

		User user = userRepository.findByEmail("blocked@example.com").orElseThrow();
		user.setIsActive(false);
		userRepository.save(user);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest("blocked@example.com", "Haslo123!"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("rejestracja z emailem w wielkich literach zapisuje go w lowercase")
	void register_normalizesEmailToLowercase() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerRequest("CASE@Example.COM"))))
				.andExpect(status().isCreated());

		assertThat(userRepository.findByEmail("case@example.com")).isPresent();
		assertThat(userRepository.findByEmail("CASE@Example.COM")).isEmpty();
	}

	@Test
	@DisplayName("logowanie wielkimi literami w emailu też działa (normalizacja)")
	void login_caseInsensitiveEmail() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerRequest("normal@example.com"))))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest("NORMAL@example.com", "Haslo123!"))))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("walidacja blokuje rejestrację z pustym body — 400")
	void register_blankBody_returns400() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
	}
}
