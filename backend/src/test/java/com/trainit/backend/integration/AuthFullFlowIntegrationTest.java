package com.trainit.backend.integration;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pełnoflowy test integracyjny scenariusza użytkownika.
 *
 * <p>Każdy test przechodzi przez wiele kroków API i sprawdza zachowanie systemu jako całości:
 * od pustej bazy, przez rejestrację wielu użytkowników, ich logowanie i interakcje z błędami,
 * aż po efekty modyfikacji statusu konta. Klasa uzupełnia {@link AuthIntegrationTest} o testy
 * scenariuszowe (a nie pojedyncze przypadki).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthFullFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@BeforeEach
	void seedRole() {
		if (roleRepository.findByName("USER").isEmpty()) {
			Role role = new Role();
			role.setName("USER");
			roleRepository.save(role);
		}
	}

	/**
	 * Wykonuje rejestrację i zwraca id utworzonego użytkownika.
	 *
	 * @param email email do rejestracji
	 * @return id utworzonego użytkownika
	 * @throws Exception gdy żądanie HTTP zawiedzie
	 */
	private int registerAndGetId(String email) throws Exception {
		RegisterRequest req = new RegisterRequest();
		req.setEmail(email);
		req.setPassword("Haslo123!");
		req.setFirstName("Jan");
		req.setLastName("Kowalski");

		String response = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return objectMapper.readTree(response).get("id").asInt();
	}

	/**
	 * Wykonuje logowanie i zwraca otrzymany token.
	 *
	 * @param email email do logowania
	 * @param password hasło do logowania
	 * @return token zwrócony przez serwis
	 * @throws Exception gdy żądanie HTTP zawiedzie
	 */
	private String loginAndGetToken(String email, String password) throws Exception {
		LoginRequest req = new LoginRequest();
		req.setEmail(email);
		req.setPassword(password);

		String response = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return objectMapper.readTree(response).get("token").asString();
	}

	@Test
	@DisplayName("scenariusz: rejestracja → logowanie → drugie logowanie z innym tokenem")
	void scenario_registerAndLoginTwice_yieldsDifferentTokens() throws Exception {
		registerAndGetId("scenario1@example.com");

		String token1 = loginAndGetToken("scenario1@example.com", "Haslo123!");
		String token2 = loginAndGetToken("scenario1@example.com", "Haslo123!");

		assertThat(token1).isNotBlank();
		assertThat(token2).isNotBlank();
		assertThat(token1).isNotEqualTo(token2);
	}

	@Test
	@DisplayName("scenariusz: rejestracja 3 użytkowników → wszyscy obecni w bazie z różnymi id")
	void scenario_threeRegistrations_haveUniqueIds() throws Exception {
		int id1 = registerAndGetId("u1@example.com");
		int id2 = registerAndGetId("u2@example.com");
		int id3 = registerAndGetId("u3@example.com");

		assertThat(id1).isNotEqualTo(id2);
		assertThat(id2).isNotEqualTo(id3);
		assertThat(id1).isNotEqualTo(id3);

		assertThat(userRepository.findByEmail("u1@example.com")).isPresent();
		assertThat(userRepository.findByEmail("u2@example.com")).isPresent();
		assertThat(userRepository.findByEmail("u3@example.com")).isPresent();
	}

	@Test
	@DisplayName("scenariusz: rejestracja → dezaktywacja → próba logowania kończy się 401")
	void scenario_deactivationBlocksLogin() throws Exception {
		registerAndGetId("deactivate@example.com");

		User user = userRepository.findByEmail("deactivate@example.com").orElseThrow();
		user.setIsActive(false);
		userRepository.save(user);

		LoginRequest loginReq = new LoginRequest();
		loginReq.setEmail("deactivate@example.com");
		loginReq.setPassword("Haslo123!");

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginReq)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Nieprawidłowy email lub hasło"));
	}

	@Test
	@DisplayName("scenariusz: po reaktywacji konta logowanie znowu działa")
	void scenario_reactivation_restoresLogin() throws Exception {
		registerAndGetId("reactivate@example.com");

		User user = userRepository.findByEmail("reactivate@example.com").orElseThrow();
		user.setIsActive(false);
		userRepository.save(user);

		LoginRequest loginReq = new LoginRequest();
		loginReq.setEmail("reactivate@example.com");
		loginReq.setPassword("Haslo123!");

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginReq)))
				.andExpect(status().isUnauthorized());

		user.setIsActive(true);
		userRepository.save(user);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginReq)))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("scenariusz: rejestracja zwraca strukturę JSON zgodną z UserResponse")
	void scenario_registerResponseStructure() throws Exception {
		RegisterRequest req = new RegisterRequest();
		req.setEmail("structure@example.com");
		req.setPassword("Haslo123!");
		req.setFirstName("Jan");
		req.setLastName("Kowalski");

		String response = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		JsonNode body = objectMapper.readTree(response);
		assertThat(body.has("id")).isTrue();
		assertThat(body.has("email")).isTrue();
		assertThat(body.has("firstName")).isTrue();
		assertThat(body.has("lastName")).isTrue();
		assertThat(body.has("role")).isTrue();
		assertThat(body.has("password")).as("brak hasła w odpowiedzi (bezpieczeństwo)").isFalse();
		assertThat(body.has("passwordHash")).as("brak hashe'u hasła w odpowiedzi (bezpieczeństwo)").isFalse();
	}

	@Test
	@DisplayName("scenariusz: logowanie zwraca strukturę JSON zgodną z LoginResponse")
	void scenario_loginResponseStructure() throws Exception {
		registerAndGetId("login-struct@example.com");

		LoginRequest loginReq = new LoginRequest();
		loginReq.setEmail("login-struct@example.com");
		loginReq.setPassword("Haslo123!");

		String response = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginReq)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		JsonNode body = objectMapper.readTree(response);
		assertThat(body.has("id")).isTrue();
		assertThat(body.has("email")).isTrue();
		assertThat(body.has("firstName")).isTrue();
		assertThat(body.has("lastName")).isTrue();
		assertThat(body.has("role")).isTrue();
		assertThat(body.has("token")).isTrue();
		assertThat(body.has("password")).as("brak hasła w odpowiedzi (bezpieczeństwo)").isFalse();
		assertThat(body.has("passwordHash")).as("brak hashe'u hasła w odpowiedzi (bezpieczeństwo)").isFalse();
	}

	@Test
	@DisplayName("scenariusz: błąd walidacji na rejestracji zwraca 400 ze strukturą {message, errors[]}")
	void scenario_validationErrorStructure() throws Exception {
		String response = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

		JsonNode body = objectMapper.readTree(response);
		assertThat(body.has("message")).isTrue();
		assertThat(body.get("message").asString()).isEqualTo("Błąd walidacji");
		assertThat(body.has("errors")).isTrue();
		assertThat(body.get("errors").isArray()).isTrue();
		assertThat(body.get("errors").size()).isGreaterThanOrEqualTo(4);
	}

	@Test
	@DisplayName("scenariusz: confict 409 dla duplikatu emaila ma czysty komunikat bez stack trace")
	void scenario_conflictHasCleanMessage() throws Exception {
		registerAndGetId("conflict@example.com");

		RegisterRequest req = new RegisterRequest();
		req.setEmail("conflict@example.com");
		req.setPassword("Haslo123!");
		req.setFirstName("Jan");
		req.setLastName("Kowalski");

		String response = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isConflict())
				.andReturn().getResponse().getContentAsString();

		JsonNode body = objectMapper.readTree(response);
		assertThat(body.has("message")).isTrue();
		assertThat(body.get("message").asString()).doesNotContain("Exception");
		assertThat(body.get("message").asString()).doesNotContain("at com.");
	}

	@Test
	@DisplayName("scenariusz: rejestracja → modyfikacja imienia w bazie → logowanie zwraca nowe imię")
	void scenario_userDataChangeReflectedInLogin() throws Exception {
		registerAndGetId("update-flow@example.com");

		User user = userRepository.findByEmail("update-flow@example.com").orElseThrow();
		user.setFirstName("ZmienioneImie");
		userRepository.save(user);

		LoginRequest loginReq = new LoginRequest();
		loginReq.setEmail("update-flow@example.com");
		loginReq.setPassword("Haslo123!");

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginReq)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.firstName").value("ZmienioneImie"));
	}
}
