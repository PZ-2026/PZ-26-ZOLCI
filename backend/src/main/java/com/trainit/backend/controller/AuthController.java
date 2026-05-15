package com.trainit.backend.controller;

import com.trainit.backend.util.AppLog;

import com.trainit.backend.dto.LoginRequest;
import com.trainit.backend.dto.LoginResponse;
import com.trainit.backend.dto.ForgotPasswordRequest;
import com.trainit.backend.dto.RegisterRequest;
import com.trainit.backend.dto.UpdateProfileRequest;
import com.trainit.backend.dto.UserResponse;
import com.trainit.backend.security.JwtPrincipal;
import com.trainit.backend.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kontroler REST obsługujący rejestrację, logowanie i zarządzanie profilem użytkownika.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	private final AuthService authService;

	/**
	 * Tworzy kontroler z wymaganym serwisem uwierzytelniania.
	 *
	 * @param authService serwis warstwy biznesowej uwierzytelniania
	 */
	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	/**
	 * Rejestruje nowego użytkownika w systemie.
	 *
	 * @param request dane rejestracyjne (e-mail, hasło, imię, nazwisko)
	 * @return utworzony profil użytkownika
	 */
	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
		AppLog.success(log, "POST /api/auth/register, email={}", request.getEmail());
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
	}

	/**
	 * Loguje użytkownika i zwraca token JWT.
	 *
	 * @param request dane logowania (e-mail, hasło)
	 * @return odpowiedź z tokenem i danymi użytkownika
	 */
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		AppLog.success(log, "POST /api/auth/login, email={}", request.getEmail());
		return ResponseEntity.ok(authService.login(request));
	}

	/**
	 * Resetuje hasło użytkownika na podstawie adresu e-mail.
	 *
	 * @param request żądanie z adresem e-mail i nowym hasłem
	 * @return odpowiedź bez treści przy sukcesie
	 */
	@PostMapping("/forgot-password")
	public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		AppLog.success(log, "POST /api/auth/forgot-password, email={}", request.getEmail());
		authService.forgotPassword(request);
		return ResponseEntity.ok().build();
	}

	/**
	 * Zwraca profil aktualnie zalogowanego użytkownika.
	 *
	 * @param authentication kontekst uwierzytelnienia Spring Security
	 * @return dane profilu użytkownika
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@GetMapping("/me")
	public ResponseEntity<UserResponse> me(Authentication authentication) {
		Integer userId = resolveUserId(authentication);
		AppLog.success(log, "GET /api/auth/me, userId={}", userId);
		return ResponseEntity.ok(authService.getProfile(userId));
	}

	/**
	 * Aktualizuje profil aktualnie zalogowanego użytkownika.
	 *
	 * @param request nowe dane profilu
	 * @param authentication kontekst uwierzytelnienia Spring Security
	 * @return zaktualizowany profil użytkownika
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@PutMapping("/me")
	public ResponseEntity<UserResponse> updateMe(
			@Valid @RequestBody UpdateProfileRequest request,
			Authentication authentication
	) {
		Integer userId = resolveUserId(authentication);
		AppLog.success(log, "PUT /api/auth/me, userId={}", userId);
		return ResponseEntity.ok(authService.updateProfile(userId, request));
	}

	/**
	 * Wyznacza identyfikator użytkownika z kontekstu uwierzytelnienia.
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
		log.warn("Brak poprawnego kontekstu uwierzytelnienia w żądaniu auth");
		throw new IllegalArgumentException("Brak poprawnego kontekstu uwierzytelnienia");
	}
}
