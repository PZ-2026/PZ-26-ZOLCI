package com.trainit.backend.controller;

import com.trainit.backend.util.AppLog;
import com.trainit.backend.dto.ChangeRoleRequest;
import com.trainit.backend.dto.UserResponse;
import com.trainit.backend.security.JwtPrincipal;
import com.trainit.backend.service.AdminService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Kontroler REST do zarządzania użytkownikami przez administratora.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private static final Logger log = LoggerFactory.getLogger(AdminController.class);

	/** Serwis operacji administracyjnych na użytkownikach. */
	private final AdminService adminService;

	/**
	 * Tworzy kontroler z wymaganym serwisem administracyjnym.
	 *
	 * @param adminService serwis warstwy biznesowej administracji
	 */
	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}

	/**
	 * Zwraca listę wszystkich użytkowników systemu.
	 *
	 * @return lista profili użytkowników
	 */
	@GetMapping("/users")
	public ResponseEntity<List<UserResponse>> getUsers() {
		AppLog.success(log, "GET /api/admin/users");
		return ResponseEntity.ok(adminService.getAllUsers());
	}

	/**
	 * Zmienia rolę wskazanego użytkownika.
	 *
	 * @param id identyfikator użytkownika docelowego
	 * @param request żądanie z nową rolą
	 * @param authentication kontekst uwierzytelnienia administratora
	 * @return zaktualizowany profil użytkownika
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@PutMapping("/users/{id}/role")
	public ResponseEntity<UserResponse> changeRole(
			@PathVariable Integer id,
			@Valid @RequestBody ChangeRoleRequest request,
			Authentication authentication
	) {
		Integer adminId = resolveUserId(authentication);
		AppLog.success(log, "PUT /api/admin/users/{}/role, adminId={}, targetUserId={}, role={}", id, adminId, id, request.getRole());
		return ResponseEntity.ok(adminService.changeUserRole(adminId, id, request.getRole()));
	}

	/**
	 * Blokuje konto wskazanego użytkownika.
	 *
	 * @param id identyfikator użytkownika docelowego
	 * @param authentication kontekst uwierzytelnienia administratora
	 * @return zaktualizowany profil użytkownika
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@PutMapping("/users/{id}/block")
	public ResponseEntity<UserResponse> blockUser(@PathVariable Integer id, Authentication authentication) {
		Integer adminId = resolveUserId(authentication);
		AppLog.success(log, "PUT /api/admin/users/{}/block, adminId={}, targetUserId={}", id, adminId, id);
		return ResponseEntity.ok(adminService.blockUser(adminId, id));
	}

	/**
	 * Odblokowuje konto wskazanego użytkownika.
	 *
	 * @param id identyfikator użytkownika docelowego
	 * @param authentication kontekst uwierzytelnienia administratora
	 * @return zaktualizowany profil użytkownika
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@PutMapping("/users/{id}/unblock")
	public ResponseEntity<UserResponse> unblockUser(@PathVariable Integer id, Authentication authentication) {
		Integer adminId = resolveUserId(authentication);
		AppLog.success(log, "PUT /api/admin/users/{}/unblock, adminId={}, targetUserId={}", id, adminId, id);
		return ResponseEntity.ok(adminService.unblockUser(adminId, id));
	}

	/**
	 * Trwale usuwa użytkownika wraz z powiązanymi danymi.
	 *
	 * @param id identyfikator użytkownika do usunięcia
	 * @param authentication kontekst uwierzytelnienia administratora
	 * @return odpowiedź bez treści przy sukcesie
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@DeleteMapping("/users/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Integer id, Authentication authentication) {
		Integer adminId = resolveUserId(authentication);
		AppLog.success(log, "DELETE /api/admin/users/{}, adminId={}, targetUserId={}", id, adminId, id);
		adminService.deleteUser(adminId, id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Wyznacza identyfikator administratora z kontekstu uwierzytelnienia.
	 *
	 * @param authentication kontekst uwierzytelnienia
	 * @return identyfikator użytkownika z tokena JWT
	 * @throws IllegalArgumentException gdy principal nie jest typu {@link JwtPrincipal}
	 */
	private Integer resolveUserId(Authentication authentication) {
		Object principal = authentication == null ? null : authentication.getPrincipal();
		if (principal instanceof JwtPrincipal jwtPrincipal) {
			return jwtPrincipal.userId();
		}
		log.warn("Brak poprawnego kontekstu uwierzytelnienia w żądaniu admin");
		throw new IllegalArgumentException("Brak poprawnego kontekstu uwierzytelnienia");
	}
}
