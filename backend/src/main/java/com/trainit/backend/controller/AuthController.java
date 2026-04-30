package com.trainit.backend.controller;

import com.trainit.backend.dto.LoginRequest;
import com.trainit.backend.dto.LoginResponse;
import com.trainit.backend.dto.ForgotPasswordRequest;
import com.trainit.backend.dto.RegisterRequest;
import com.trainit.backend.dto.UpdateProfileRequest;
import com.trainit.backend.dto.UserResponse;
import com.trainit.backend.security.JwtPrincipal;
import com.trainit.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.forgotPassword(request);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/me")
	public ResponseEntity<UserResponse> me(Authentication authentication) {
		return ResponseEntity.ok(authService.getProfile(resolveUserId(authentication)));
	}

	@PutMapping("/me")
	public ResponseEntity<UserResponse> updateMe(
			@Valid @RequestBody UpdateProfileRequest request,
			Authentication authentication
	) {
		return ResponseEntity.ok(authService.updateProfile(resolveUserId(authentication), request));
	}

	private Integer resolveUserId(Authentication authentication) {
		Object principal = authentication == null ? null : authentication.getPrincipal();
		if (principal instanceof JwtPrincipal jwtPrincipal) {
			return jwtPrincipal.userId();
		}
		throw new IllegalArgumentException("Brak poprawnego kontekstu uwierzytelnienia");
	}
}
