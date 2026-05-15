package com.trainit.backend.controller;

import com.trainit.backend.dto.ChangeRoleRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testy warstwy MVC kontrolera {@link AdminController}.
 */
@WebMvcTest(AdminController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AdminController adminController;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AdminService adminService;

	private static Authentication adminAuthentication() {
		return new UsernamePasswordAuthenticationToken(
				new JwtPrincipal(1, "admin@test.com", "ADMIN"),
				null,
				List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
		);
	}

	@Test
	@DisplayName("GET /api/admin/users → 200 + lista UserResponse")
	void should_return200AndList_when_getUsers() throws Exception {
		when(adminService.getAllUsers()).thenReturn(List.of(
				new UserResponse(1, "a@test.com", "Ala", "Nowak", "USER", true),
				new UserResponse(2, "b@test.com", "Bartek", "Kowalski", "TRAINER", false)
		));

		mockMvc.perform(get("/api/admin/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].role").value("USER"))
				.andExpect(jsonPath("$[1].role").value("TRAINER"));
	}

	@Test
	@DisplayName("PUT /api/admin/users/{id}/role z body {role:TRAINER} → 200 + UserResponse")
	void should_return200_when_changeRoleWithValidBody() {
		UserResponse updated = new UserResponse(10, "u@test.com", "Jan", "Kowalski", "TRAINER", true);
		when(adminService.changeUserRole(eq(1), eq(10), eq("TRAINER"))).thenReturn(updated);

		ChangeRoleRequest request = new ChangeRoleRequest();
		request.setRole("TRAINER");

		ResponseEntity<UserResponse> response = adminController.changeRole(10, request, adminAuthentication());

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().id()).isEqualTo(10);
		assertThat(response.getBody().role()).isEqualTo("TRAINER");
	}

	@Test
	@DisplayName("PUT /api/admin/users/{id}/role z pustym body → 400")
	void should_return400_when_changeRoleWithEmptyBody() throws Exception {
		mockMvc.perform(put("/api/admin/users/10/role")
						.contentType(MediaType.APPLICATION_JSON)
						.content(""))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /api/admin/users/{id}/block → 200 + UserResponse (isActive=false)")
	void should_return200_when_blockUser() {
		when(adminService.blockUser(eq(1), eq(5)))
				.thenReturn(new UserResponse(5, "x@test.com", "X", "Y", "USER", false));

		ResponseEntity<UserResponse> response = adminController.blockUser(5, adminAuthentication());

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().isActive()).isFalse();
	}

	@Test
	@DisplayName("PUT /api/admin/users/{id}/unblock → 200 + UserResponse (isActive=true)")
	void should_return200_when_unblockUser() {
		when(adminService.unblockUser(eq(1), eq(5)))
				.thenReturn(new UserResponse(5, "x@test.com", "X", "Y", "USER", true));

		ResponseEntity<UserResponse> response = adminController.unblockUser(5, adminAuthentication());

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().isActive()).isTrue();
	}

	@Test
	@DisplayName("DELETE /api/admin/users/{id} → 204")
	void should_return204_when_deleteUser() {
		doNothing().when(adminService).deleteUser(eq(1), eq(99));

		ResponseEntity<Void> response = adminController.deleteUser(99, adminAuthentication());

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	@DisplayName("PUT /api/admin/users/{id}/block gdy serwis rzuca IllegalArgumentException")
	void should_throwIllegalArgumentException_when_blockUserOnSelf() {
		when(adminService.blockUser(eq(1), eq(7)))
				.thenThrow(new IllegalArgumentException("Nie możesz wykonać tej operacji na własnym koncie"));

		org.junit.jupiter.api.Assertions.assertThrows(
				IllegalArgumentException.class,
				() -> adminController.blockUser(7, adminAuthentication())
		);
	}

	@Test
	@DisplayName("PUT /api/admin/users/{id}/block bez principal → 400")
	void should_return400_when_blockUserWithoutAuth() throws Exception {
		mockMvc.perform(put("/api/admin/users/7/block"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Brak poprawnego kontekstu uwierzytelnienia"));
	}
}
