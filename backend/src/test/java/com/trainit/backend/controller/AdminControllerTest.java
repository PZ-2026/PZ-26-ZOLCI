package com.trainit.backend.controller;

import com.trainit.backend.dto.UserResponse;
import com.trainit.backend.exception.GlobalExceptionHandler;
import com.trainit.backend.security.JwtPrincipal;
import com.trainit.backend.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Testy warstwy MVC kontrolera {@link AdminController}.
 *
 * <p>Testy uruchamiają wyłącznie warstwę webową ({@code @WebMvcTest}) i mockują zależności serwisowe.
 * Obsługa wyjątków jest włączona przez import {@link GlobalExceptionHandler}.
 */
@WebMvcTest(AdminController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AdminService adminService;

	/**
	 * Tworzy kontekst uwierzytelnienia admina zgodny z JWT principal.
	 *
	 * @return post-processor ustawiający {@link org.springframework.security.core.Authentication}
	 */
	private static RequestPostProcessor adminAuth() {
		return authentication(
				new UsernamePasswordAuthenticationToken(
						new JwtPrincipal(1, "admin@test.com", "ADMIN"),
						null,
						List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
				)
		);
	}

	@Test
	@DisplayName("GET /api/admin/users → 200 + lista UserResponse")
	void getUsers_returns200AndList() throws Exception {
		when(adminService.getAllUsers()).thenReturn(List.of(
				new UserResponse(1, "a@test.com", "Ala", "Nowak", "USER", true),
				new UserResponse(2, "b@test.com", "Bartek", "Kowalski", "TRAINER", false)
		));

		mockMvc.perform(get("/api/admin/users").with(adminAuth()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].role").value("USER"))
				.andExpect(jsonPath("$[0].isActive").value(true))
				.andExpect(jsonPath("$[1].id").value(2))
				.andExpect(jsonPath("$[1].role").value("TRAINER"))
				.andExpect(jsonPath("$[1].isActive").value(false));
	}

	@Test
	@DisplayName("PUT /api/admin/users/{id}/role z body {role:TRAINER} → 200 + UserResponse")
	void changeRole_validBody_returns200() throws Exception {
		UserResponse updated = new UserResponse(10, "u@test.com", "Jan", "Kowalski", "TRAINER", true);
		when(adminService.changeUserRole(eq(1), eq(10), eq("TRAINER"))).thenReturn(updated);

		mockMvc.perform(put("/api/admin/users/10/role")
						.with(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new java.util.HashMap<>(java.util.Map.of("role", "TRAINER")))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.role").value("TRAINER"))
				.andExpect(jsonPath("$.isActive").value(true));
	}

	@Test
	@DisplayName("PUT /api/admin/users/{id}/role z pustym body → 400")
	void changeRole_emptyBody_returns400() throws Exception {
		mockMvc.perform(put("/api/admin/users/10/role")
						.with(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content(""))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /api/admin/users/{id}/block → 200 + UserResponse (isActive=false)")
	void blockUser_returns200AndInactive() throws Exception {
		when(adminService.blockUser(eq(1), eq(5))).thenReturn(new UserResponse(5, "x@test.com", "X", "Y", "USER", false));

		mockMvc.perform(put("/api/admin/users/5/block").with(adminAuth()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5))
				.andExpect(jsonPath("$.isActive").value(false));
	}

	@Test
	@DisplayName("PUT /api/admin/users/{id}/unblock → 200 + UserResponse (isActive=true)")
	void unblockUser_returns200AndActive() throws Exception {
		when(adminService.unblockUser(eq(1), eq(5))).thenReturn(new UserResponse(5, "x@test.com", "X", "Y", "USER", true));

		mockMvc.perform(put("/api/admin/users/5/unblock").with(adminAuth()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5))
				.andExpect(jsonPath("$.isActive").value(true));
	}

	@Test
	@DisplayName("DELETE /api/admin/users/{id} → 204")
	void deleteUser_returns204() throws Exception {
		doNothing().when(adminService).deleteUser(eq(1), eq(99));

		mockMvc.perform(delete("/api/admin/users/99").with(adminAuth()))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("PUT /api/admin/users/{id}/block gdy serwis rzuca IllegalArgumentException → 400")
	void blockUser_whenServiceThrowsIllegalArgumentException_returns400() throws Exception {
		when(adminService.blockUser(eq(1), eq(7)))
				.thenThrow(new IllegalArgumentException("Nie możesz wykonać tej operacji na własnym koncie"));

		mockMvc.perform(put("/api/admin/users/7/block").with(adminAuth()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Nie możesz wykonać tej operacji na własnym koncie"));
	}
}

