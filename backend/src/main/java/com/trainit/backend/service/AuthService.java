package com.trainit.backend.service;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;

	private final RoleRepository roleRepository;

	private final BCryptPasswordEncoder passwordEncoder;

	private final JwtService jwtService;

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

	@Transactional
	public UserResponse register(RegisterRequest request) {
		String email = normalizeEmail(request.getEmail());
		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyExistsException("Ten adres email jest już zajęty");
		}
		Role userRole = roleRepository.findByName("USER")
				.orElseThrow(() -> new IllegalStateException("Brak roli USER w bazie"));
		User user = new User();
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setFirstName(request.getFirstName().trim());
		user.setLastName(request.getLastName().trim());
		user.setRole(userRole);
		userRepository.save(user);
		return UserResponse.fromEntity(user);
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		String email = normalizeEmail(request.getEmail());
		User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}
		if (!Boolean.TRUE.equals(user.getIsActive())) {
			throw new InvalidCredentialsException();
		}
		String token = jwtService.generateToken(
				user.getId(),
				user.getEmail(),
				user.getRole() == null ? "USER" : user.getRole().getName()
		);
		return LoginResponse.fromEntity(user, token);
	}

	@Transactional
	public void forgotPassword(ForgotPasswordRequest request) {
		String email = normalizeEmail(request.getEmail());
		userRepository.findByEmail(email).ifPresent(user -> {
			user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
			userRepository.save(user);
		});
	}

	@Transactional(readOnly = true)
	public UserResponse getProfile(Integer userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika"));
		return UserResponse.fromEntity(user);
	}

	@Transactional
	public UserResponse updateProfile(Integer userId, UpdateProfileRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika"));
		String normalizedEmail = normalizeEmail(request.getEmail());
		if (userRepository.existsByEmailAndIdNot(normalizedEmail, userId)) {
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
		return UserResponse.fromEntity(user);
	}
}
