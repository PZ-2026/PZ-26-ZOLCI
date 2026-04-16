package com.trainit.backend.service;

import com.trainit.backend.dto.LoginRequest;
import com.trainit.backend.dto.LoginResponse;
import com.trainit.backend.dto.RegisterRequest;
import com.trainit.backend.dto.UserResponse;
import com.trainit.backend.entity.Role;
import com.trainit.backend.entity.User;
import com.trainit.backend.exception.EmailAlreadyExistsException;
import com.trainit.backend.exception.InvalidCredentialsException;
import com.trainit.backend.repository.RoleRepository;
import com.trainit.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serwis domenowy obsługujący rejestrację i logowanie użytkowników.
 *
 * <p>Operacje zapisu i odczytu danych użytkownika wykorzystują repozytoria JPA.
 * Hasła są hashowane algorytmem BCrypt przed zapisem. Metoda {@link #register(RegisterRequest)}
 * wykonuje się w transakcji zapisu; {@link #login(LoginRequest)} — w transakcji tylko do odczytu.
 *
 * <p>Token zwracany przy logowaniu jest obecnie losowym UUID (placeholder); docelowo może zostać
 * zastąpiony JWT — patrz komentarz TODO w kodzie metody {@link #login(LoginRequest)}.
 *
 * @see com.trainit.backend.controller.AuthController
 * @see UserRepository
 * @see RoleRepository
 */
@Service
public class AuthService {

	/** Repozytorium encji {@link User}; wyszukiwanie po emailu i zapis nowych kont. */
	private final UserRepository userRepository;

	/** Repozytorium ról; wymagane do przypisania roli {@code USER} przy rejestracji. */
	private final RoleRepository roleRepository;

	/** Koder haseł BCrypt; bean zdefiniowany w {@link com.trainit.backend.config.PasswordEncoderConfig}. */
	private final BCryptPasswordEncoder passwordEncoder;

	/**
	 * Tworzy serwis z zależnościami dostarczonymi przez kontener Springa.
	 *
	 * @param userRepository repozytorium użytkowników
	 * @param roleRepository repozytorium ról
	 * @param passwordEncoder enkoder BCrypt do hashowania haseł
	 */
	public AuthService(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Normalizuje adres email przed porównaniami i zapisem w bazie.
	 *
	 * <p>Usuwa białe znaki z początku i końca oraz zamienia litery na małe,
	 * aby uniknąć duplikatów różniących się wielkością liter.
	 *
	 * @param email surowy email z żądania lub {@code null}
	 * @return znormalizowany łańcuch lub {@code null}, jeśli wejście było {@code null}
	 */
	private static String normalizeEmail(String email) {
		if (email == null) {
			return null;
		}
		return email.trim().toLowerCase();
	}

	/**
	 * Rejestruje nowego użytkownika z domyślną rolą {@code USER}.
	 *
	 * <p>Email jest normalizowany; hasło zapisywane jest wyłącznie jako skrót BCrypt.
	 * Imię i nazwisko są przycinane z obu stron. Przed zapisem weryfikowana jest unikalność emaila.
	 *
	 * @param request dane wejściowe z warstwy REST ({@link RegisterRequest})
	 * @return DTO odpowiedzi z danymi utworzonego użytkownika (bez hasła)
	 * @throws EmailAlreadyExistsException gdy użytkownik o podanym emailu już istnieje
	 * @throws IllegalStateException gdy w bazie brakuje roli o nazwie {@code USER}
	 */
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

	/**
	 * Uwierzytelnia użytkownika po emailu i haśle oraz zwraca token sesji (obecnie losowy UUID).
	 *
	 * <p>Hasło z żądania jest porównywane ze skrótem z bazy metodą {@code matches}.
	 * Konto musi mieć flagę aktywności ustawioną na {@code true}.
	 *
	 * @param request dane logowania ({@link LoginRequest})
	 * @return odpowiedź z danymi użytkownika i tokenem
	 * @throws InvalidCredentialsException gdy użytkownik nie istnieje, hasło jest błędne lub konto jest nieaktywne
	 */
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
		String token = UUID.randomUUID().toString(); // TODO: JWT w kolejnej iteracji
		return LoginResponse.fromEntity(user, token);
	}
}
