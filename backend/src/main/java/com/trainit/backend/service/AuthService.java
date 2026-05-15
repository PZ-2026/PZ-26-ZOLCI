package com.trainit.backend.service;

import com.trainit.backend.util.AppLog;

import com.trainit.backend.dto.LoginRequest;
import com.trainit.backend.dto.LoginResponse;
import com.trainit.backend.dto.ForgotPasswordRequest;
import com.trainit.backend.dto.RegisterRequest;
import com.trainit.backend.dto.UpdateProfileRequest;
import com.trainit.backend.dto.UserResponse;
import com.trainit.backend.entity.Role;
import com.trainit.backend.entity.User;
import com.trainit.backend.exception.EmailAlreadyExistsException;
import com.trainit.backend.exception.InvalidCredentialsException;
import com.trainit.backend.repository.RoleRepository;
import com.trainit.backend.repository.UserRepository;
import com.trainit.backend.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serwis obsługujący rejestrację, logowanie i zarządzanie profilem użytkownika.
 */
@Service
public class AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthService.class);

	private final UserRepository userRepository;

	private final RoleRepository roleRepository;

	private final BCryptPasswordEncoder passwordEncoder;

	private final JwtService jwtService;

	/**
	 * Tworzy serwis z wymaganymi zależnościami.
	 *
	 * @param userRepository repozytorium użytkowników
	 * @param roleRepository repozytorium ról
	 * @param passwordEncoder enkoder haseł BCrypt
	 * @param jwtService serwis generowania tokenów JWT
	 */
	public AuthService(
			UserRepository userRepository,
			RoleRepository roleRepository,
			BCryptPasswordEncoder passwordEncoder,
			JwtService jwtService
	) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	private static String normalizeEmail(String email) {
		if (email == null) {
			return null;
		}
		return email.trim().toLowerCase();
	}

	/**
	 * Rejestruje nowego użytkownika w systemie.
	 *
	 * @param request dane rejestracyjne
	 * @return profil utworzonego użytkownika
	 * @throws EmailAlreadyExistsException gdy adres e-mail jest już zajęty
	 * @throws IllegalStateException gdy brak roli USER w bazie
	 */
	@Transactional
	public UserResponse register(RegisterRequest request) {
		String email = normalizeEmail(request.getEmail());
		if (userRepository.existsByEmail(email)) {
			log.warn("Próba rejestracji na zajęty adres e-mail: {}", email);
			throw new EmailAlreadyExistsException("Ten adres email jest już zajęty");
		}
		Role userRole = roleRepository.findByName("USER")
				.orElseThrow(() -> {
					log.error("Brak roli USER w bazie danych");
					return new IllegalStateException("Brak roli USER w bazie");
				});
		User user = new User();
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setFirstName(request.getFirstName().trim());
		user.setLastName(request.getLastName().trim());
		user.setRole(userRole);
		userRepository.save(user);
		AppLog.success(log, "Zarejestrowano użytkownika, userId={}, email={}", user.getId(), email);
		return UserResponse.fromEntity(user);
	}

	/**
	 * Loguje użytkownika i generuje token JWT.
	 *
	 * @param request dane logowania
	 * @return odpowiedź z tokenem i danymi użytkownika
	 * @throws InvalidCredentialsException gdy dane logowania są nieprawidłowe lub konto nieaktywne
	 */
	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		String email = normalizeEmail(request.getEmail());
		User user = userRepository.findByEmail(email).orElseThrow(() -> {
			log.warn("Nieudane logowanie – nieznany e-mail: {}", email);
			return new InvalidCredentialsException();
		});
		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			log.warn("Nieudane logowanie – błędne hasło, userId={}", user.getId());
			throw new InvalidCredentialsException();
		}
		if (!Boolean.TRUE.equals(user.getIsActive())) {
			log.warn("Nieudane logowanie – konto nieaktywne, userId={}", user.getId());
			throw new InvalidCredentialsException();
		}
		String token = jwtService.generateToken(
				user.getId(),
				user.getEmail(),
				user.getRole() == null ? "USER" : user.getRole().getName()
		);
		AppLog.success(log, "Zalogowano użytkownika, userId={}, email={}", user.getId(), email);
		return LoginResponse.fromEntity(user, token);
	}

	/**
	 * Resetuje hasło użytkownika na podstawie adresu e-mail (jeśli konto istnieje).
	 *
	 * @param request żądanie z adresem e-mail i nowym hasłem
	 */
	@Transactional
	public void forgotPassword(ForgotPasswordRequest request) {
		String email = normalizeEmail(request.getEmail());
		userRepository.findByEmail(email).ifPresentOrElse(user -> {
			user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
			userRepository.save(user);
			AppLog.success(log, "Zresetowano hasło, userId={}, email={}", user.getId(), email);
		}, () -> AppLog.success(log, "Żądanie resetu hasła dla nieistniejącego e-maila (bez ujawniania): {}", email));
	}

	/**
	 * Zwraca profil użytkownika po identyfikatorze.
	 *
	 * @param userId identyfikator użytkownika
	 * @return profil użytkownika
	 * @throws IllegalArgumentException gdy użytkownik nie istnieje
	 */
	@Transactional(readOnly = true)
	public UserResponse getProfile(Integer userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> {
					log.warn("Nie znaleziono użytkownika, userId={}", userId);
					return new IllegalArgumentException("Nie znaleziono użytkownika");
				});
		AppLog.success(log, "Pobrano profil użytkownika, userId={}", userId);
		return UserResponse.fromEntity(user);
	}

	/**
	 * Aktualizuje profil użytkownika.
	 *
	 * @param userId identyfikator użytkownika
	 * @param request nowe dane profilu
	 * @return zaktualizowany profil
	 * @throws IllegalArgumentException gdy użytkownik nie istnieje
	 * @throws EmailAlreadyExistsException gdy nowy e-mail jest już zajęty
	 */
	@Transactional
	public UserResponse updateProfile(Integer userId, UpdateProfileRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> {
					log.warn("Nie znaleziono użytkownika do aktualizacji, userId={}", userId);
					return new IllegalArgumentException("Nie znaleziono użytkownika");
				});
		String normalizedEmail = normalizeEmail(request.getEmail());
		if (userRepository.existsByEmailAndIdNot(normalizedEmail, userId)) {
			log.warn("Próba aktualizacji profilu na zajęty e-mail, userId={}, email={}", userId, normalizedEmail);
			throw new EmailAlreadyExistsException("Ten adres email jest już zajęty");
		}
		user.setFirstName(request.getFirstName().trim());
		user.setLastName(request.getLastName().trim());
		user.setEmail(normalizedEmail);
		String newPassword = request.getNewPassword();
		if (newPassword != null && !newPassword.isBlank()) {
			user.setPasswordHash(passwordEncoder.encode(newPassword));
		}
		userRepository.save(user);
		AppLog.success(log, "Zaktualizowano profil użytkownika, userId={}", userId);
		return UserResponse.fromEntity(user);
	}
}
