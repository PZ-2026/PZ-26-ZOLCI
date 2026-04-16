package com.trainit.backend.controller;

import com.trainit.backend.dto.LoginRequest;
import com.trainit.backend.dto.LoginResponse;
import com.trainit.backend.dto.RegisterRequest;
import com.trainit.backend.dto.UserResponse;
import com.trainit.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Warstwa REST odpowiedzialna za rejestrację i logowanie użytkowników.
 *
 * <p>Wystawia prefiks {@code /api/auth}. Walidacja treści żądań odbywa się na podstawie
 * adnotacji Bean Validation ({@code @Valid}) na DTO; błędy walidacji mapuje
 * {@link com.trainit.backend.exception.GlobalExceptionHandler}.
 *
 * <p>Endpointy:
 * <ul>
 *     <li>{@code POST /api/auth/register} — rejestracja nowego konta, odpowiedź {@code 201 Created}</li>
 *     <li>{@code POST /api/auth/login} — logowanie, odpowiedź {@code 200 OK} z tokenem (obecnie stub UUID)</li>
 * </ul>
 *
 * @see AuthService
 * @see com.trainit.backend.dto.RegisterRequest
 * @see com.trainit.backend.dto.LoginRequest
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	/** Serwis z logiką rejestracji i logowania. */
	private final AuthService authService;

	/**
	 * Tworzy kontroler z wstrzykniętym serwisem uwierzytelniania.
	 *
	 * @param authService serwis {@link AuthService}; nie może być {@code null}
	 */
	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	/**
	 * Rejestruje nowego użytkownika w systemie.
	 *
	 * <p>Po pomyślnej rejestracji zwraca dane profilu bez hasła. W przypadku kolizji emaila
	 * serwis rzuca wyjątek mapowany na {@code 409 Conflict}.
	 *
	 * @param request treść JSON z polami rejestracji ({@link RegisterRequest}); musi przejść walidację
	 * @return odpowiedź {@link ResponseEntity} ze statusem {@link HttpStatus#CREATED} i ciałem {@link UserResponse}
	 */
	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
	}

	/**
	 * Loguje użytkownika na podstawie emaila i hasła w postaci jawnej z żądania.
	 *
	 * <p>Przy nieprawidłowych danych lub koncie nieaktywnym zwracany jest błąd mapowany
	 * na {@code 401 Unauthorized} przez {@link com.trainit.backend.exception.GlobalExceptionHandler}.
	 *
	 * @param request treść JSON z email i hasłem ({@link LoginRequest}); musi przejść walidację
	 * @return odpowiedź {@link ResponseEntity} ze statusem {@code 200} i ciałem {@link LoginResponse}
	 */
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}
}
