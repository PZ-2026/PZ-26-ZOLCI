package com.trainit.backend.controller;

import com.trainit.backend.dto.LoginRequest;
import com.trainit.backend.dto.LoginResponse;
import com.trainit.backend.dto.ForgotPasswordRequest;
import com.trainit.backend.dto.RegisterRequest;
import com.trainit.backend.dto.UserResponse;
import com.trainit.backend.exception.EmailAlreadyExistsException;
import com.trainit.backend.exception.GlobalExceptionHandler;
import com.trainit.backend.exception.InvalidCredentialsException;
import com.trainit.backend.service.AuthService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testy warstwy MVC kontrolera {@link AuthController}.
 *
 * <p>Wykorzystują {@code @WebMvcTest} ograniczając kontekst do warstwy webowej oraz
 * importują {@link GlobalExceptionHandler}, aby sprawdzić zachowanie błędów. Serwis aplikacyjny
 * jest mockowany przez {@code @MockitoBean}.
 *
 * @see AuthController
 */
@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AuthService authService;

	/**
	 * Buduje poprawne żądanie rejestracji do serializacji JSON.
	 *
	 * @return przygotowane DTO {@link RegisterRequest}
	 */
	private static RegisterRequest registerRequest() {
		RegisterRequest req = new RegisterRequest();
		req.setEmail("jan@example.com");
		req.setPassword("Haslo123!");
		req.setFirstName("Jan");
		req.setLastName("Kowalski");
		return req;
	}

	/**
	 * Buduje poprawne żądanie logowania do serializacji JSON.
	 *
	 * @return przygotowane DTO {@link LoginRequest}
	 */
	private static LoginRequest loginRequest() {
		LoginRequest req = new LoginRequest();
		req.setEmail("jan@example.com");
		req.setPassword("Haslo123!");
		return req;
	}

	private static ForgotPasswordRequest forgotPasswordRequest() {
		ForgotPasswordRequest req = new ForgotPasswordRequest();
		req.setEmail("jan@example.com");
		req.setNewPassword("NoweHaslo123!");
		return req;
	}

	@Test
	@DisplayName("POST /api/auth/register z poprawnym body → 201 + UserResponse")
	void register_validBody_returns201() throws Exception {
		UserResponse response = new UserResponse(1, "jan@example.com", "Jan", "Kowalski", "USER");
		when(authService.register(any())).thenReturn(response);

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerRequest())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.email").value("jan@example.com"))
				.andExpect(jsonPath("$.firstName").value("Jan"))
				.andExpect(jsonPath("$.lastName").value("Kowalski"))
				.andExpect(jsonPath("$.role").value("USER"));
	}

	@Test
	@DisplayName("POST /api/auth/register z pustym emailem → 400 z błędami pól")
	void register_blankEmail_returns400() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"\",\"password\":\"Haslo123!\",\"firstName\":\"Jan\",\"lastName\":\"K\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Błąd walidacji"))
				.andExpect(jsonPath("$.errors").isArray());
	}

	@Test
	@DisplayName("POST /api/auth/register z niepoprawnym formatem emaila → 400")
	void register_invalidEmailFormat_returns400() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"nie-email\",\"password\":\"Haslo123!\",\"firstName\":\"Jan\",\"lastName\":\"K\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/auth/register z hasłem krótszym niż 8 znaków → 400")
	void register_shortPassword_returns400() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"a@b.com\",\"password\":\"krotk\",\"firstName\":\"Jan\",\"lastName\":\"K\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/auth/register z duplikatem emaila → 409")
	void register_duplicateEmail_returns409() throws Exception {
		when(authService.register(any())).thenThrow(new EmailAlreadyExistsException("Ten adres email jest już zajęty"));

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerRequest())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Ten adres email jest już zajęty"));
	}

	@Test
	@DisplayName("POST /api/auth/register z niepoprawnym JSON → 400")
	void register_malformedJson_returns400() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ to nie jest JSON"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Nieprawidłowy format JSON"));
	}

	@Test
	@DisplayName("POST /api/auth/login z poprawnym body → 200 + token")
	void login_validBody_returns200WithToken() throws Exception {
		LoginResponse response = new LoginResponse(1, "jan@example.com", "Jan", "Kowalski", "USER", "JWT-TOKEN");
		when(authService.login(any())).thenReturn(response);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("JWT-TOKEN"))
				.andExpect(jsonPath("$.email").value("jan@example.com"))
				.andExpect(jsonPath("$.role").value("USER"));
	}

	@Test
	@DisplayName("POST /api/auth/login z błędnymi danymi → 401")
	void login_wrongCredentials_returns401() throws Exception {
		when(authService.login(any())).thenThrow(new InvalidCredentialsException());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest())))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Nieprawidłowy email lub hasło"));
	}

	@Test
	@DisplayName("POST /api/auth/login z brakującymi polami → 400")
	void login_missingFields_returns400() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/auth/login z pustym hasłem → 400")
	void login_blankPassword_returns400() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"a@b.com\",\"password\":\"\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/auth/login z niepoprawnym formatem emaila → 400")
	void login_invalidEmail_returns400() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"nie-email\",\"password\":\"Haslo123!\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/auth/login bez body → 400")
	void login_emptyBody_returns400() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("nieoczekiwany wyjątek serwisu → 500 z ogólnym komunikatem")
	void unexpectedError_returns500() throws Exception {
		when(authService.register(any())).thenThrow(new RuntimeException("internal"));

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerRequest())))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.message").value("Wystąpił nieoczekiwany błąd"));
	}

	@Test
	@DisplayName("POST /api/auth/forgot-password z poprawnym body → 200")
	void forgotPassword_validBody_returns200() throws Exception {
		doNothing().when(authService).forgotPassword(any());

		mockMvc.perform(post("/api/auth/forgot-password")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(forgotPasswordRequest())))
				.andExpect(status().isOk());
	}
}
