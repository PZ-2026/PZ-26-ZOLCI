package com.trainit.backend.controller;

import com.trainit.backend.dto.ChangeRoleRequest;
import com.trainit.backend.dto.UserResponse;
import com.trainit.backend.security.JwtPrincipal;
import com.trainit.backend.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final AdminService adminService;

	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}

	@GetMapping("/users")
	public ResponseEntity<List<UserResponse>> getUsers() {
		return ResponseEntity.ok(adminService.getAllUsers());
	}

	@PutMapping("/users/{id}/role")
	public ResponseEntity<UserResponse> changeRole(
			@PathVariable Integer id,
			@Valid @RequestBody ChangeRoleRequest request,
			Authentication authentication
	) {
		Integer adminId = resolveUserId(authentication);
		return ResponseEntity.ok(adminService.changeUserRole(adminId, id, request.getRole()));
	}

	@PutMapping("/users/{id}/block")
	public ResponseEntity<UserResponse> blockUser(@PathVariable Integer id, Authentication authentication) {
		Integer adminId = resolveUserId(authentication);
		return ResponseEntity.ok(adminService.blockUser(adminId, id));
	}

	@PutMapping("/users/{id}/unblock")
	public ResponseEntity<UserResponse> unblockUser(@PathVariable Integer id, Authentication authentication) {
		Integer adminId = resolveUserId(authentication);
		return ResponseEntity.ok(adminService.unblockUser(adminId, id));
	}

	@DeleteMapping("/users/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Integer id, Authentication authentication) {
		Integer adminId = resolveUserId(authentication);
		adminService.deleteUser(adminId, id);
		return ResponseEntity.noContent().build();
	}

	private Integer resolveUserId(Authentication authentication) {
		Object principal = authentication == null ? null : authentication.getPrincipal();
		if (principal instanceof JwtPrincipal jwtPrincipal) {
			return jwtPrincipal.userId();
		}
		throw new IllegalArgumentException("Brak poprawnego kontekstu uwierzytelnienia");
	}
}

