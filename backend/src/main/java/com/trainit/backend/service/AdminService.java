package com.trainit.backend.service;

import com.trainit.backend.util.AppLog;

import com.trainit.backend.dto.UserResponse;
import com.trainit.backend.entity.Role;
import com.trainit.backend.entity.User;
import com.trainit.backend.entity.Workout;
import com.trainit.backend.entity.WorkoutSession;
import com.trainit.backend.repository.ExerciseRepository;
import com.trainit.backend.repository.ExerciseResultRepository;
import com.trainit.backend.repository.RoleRepository;
import com.trainit.backend.repository.UserRepository;
import com.trainit.backend.repository.WorkoutExerciseRepository;
import com.trainit.backend.repository.WorkoutRepository;
import com.trainit.backend.repository.WorkoutSessionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serwis administracyjny do zarządzania użytkownikami systemu.
 */
@Service
public class AdminService {

	private static final Logger log = LoggerFactory.getLogger(AdminService.class);

	private static final String SELF_OPERATION_ERROR = "Nie możesz wykonać tej operacji na własnym koncie";

	/** Repozytorium kont użytkowników. */
	private final UserRepository userRepository;

	/** Repozytorium ról systemowych. */
	private final RoleRepository roleRepository;

	/** Repozytorium planów treningowych. */
	private final WorkoutRepository workoutRepository;

	/** Repozytorium powiązań ćwiczeń z planami. */
	private final WorkoutExerciseRepository workoutExerciseRepository;

	/** Repozytorium sesji treningowych. */
	private final WorkoutSessionRepository workoutSessionRepository;

	/** Repozytorium wyników ćwiczeń w sesjach. */
	private final ExerciseResultRepository exerciseResultRepository;

	/** Repozytorium ćwiczeń. */
	private final ExerciseRepository exerciseRepository;

	/** Kontekst JPA do operacji kaskadowego usuwania danych użytkownika. */
	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Tworzy serwis z wymaganymi repozytoriami.
	 *
	 * @param userRepository repozytorium użytkowników
	 * @param roleRepository repozytorium ról
	 * @param workoutRepository repozytorium planów treningowych
	 * @param workoutExerciseRepository repozytorium ćwiczeń w planach
	 * @param workoutSessionRepository repozytorium sesji treningowych
	 * @param exerciseResultRepository repozytorium wyników ćwiczeń
	 * @param exerciseRepository repozytorium ćwiczeń
	 */
	public AdminService(
			UserRepository userRepository,
			RoleRepository roleRepository,
			WorkoutRepository workoutRepository,
			WorkoutExerciseRepository workoutExerciseRepository,
			WorkoutSessionRepository workoutSessionRepository,
			ExerciseResultRepository exerciseResultRepository,
			ExerciseRepository exerciseRepository
	) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.workoutRepository = workoutRepository;
		this.workoutExerciseRepository = workoutExerciseRepository;
		this.workoutSessionRepository = workoutSessionRepository;
		this.exerciseResultRepository = exerciseResultRepository;
		this.exerciseRepository = exerciseRepository;
	}

	/**
	 * Zwraca listę wszystkich użytkowników.
	 *
	 * @return lista profili użytkowników
	 */
	@Transactional(readOnly = true)
	public List<UserResponse> getAllUsers() {
		List<UserResponse> users = userRepository.findAll().stream().map(UserResponse::fromEntity).toList();
		AppLog.success(log, "Pobrano listę użytkowników, liczba={}", users.size());
		return users;
	}

	/**
	 * Zmienia rolę wskazanego użytkownika.
	 *
	 * @param adminId identyfikator administratora wykonującego operację
	 * @param userId identyfikator użytkownika docelowego
	 * @param roleName nazwa nowej roli
	 * @return zaktualizowany profil użytkownika
	 * @throws IllegalArgumentException gdy operacja na własnym koncie, nieprawidłowa rola lub brak użytkownika
	 * @throws IllegalStateException gdy brak roli w bazie
	 */
	@Transactional
	public UserResponse changeUserRole(Integer adminId, Integer userId, String roleName) {
		ensureNotSelf(adminId, userId);

		String normalized = normalizeRole(roleName);
		validateRole(normalized);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> {
					log.warn("Zmiana roli – nie znaleziono użytkownika, userId={}", userId);
					return new IllegalArgumentException("Nie znaleziono użytkownika");
				});

		Role role = roleRepository.findByName(normalized)
				.orElseThrow(() -> {
					log.error("Brak roli {} w bazie danych", normalized);
					return new IllegalStateException("Brak roli " + normalized + " w bazie");
				});

		user.setRole(role);
		userRepository.save(user);
		AppLog.success(log, "Zmieniono rolę użytkownika, adminId={}, userId={}, rola={}", adminId, userId, normalized);
		return UserResponse.fromEntity(user);
	}

	/**
	 * Blokuje konto wskazanego użytkownika.
	 *
	 * @param adminId identyfikator administratora
	 * @param userId identyfikator użytkownika docelowego
	 * @return zaktualizowany profil użytkownika
	 * @throws IllegalArgumentException gdy operacja na własnym koncie lub brak użytkownika
	 */
	@Transactional
	public UserResponse blockUser(Integer adminId, Integer userId) {
		ensureNotSelf(adminId, userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> {
					log.warn("Blokada – nie znaleziono użytkownika, userId={}", userId);
					return new IllegalArgumentException("Nie znaleziono użytkownika");
				});
		user.setIsActive(false);
		userRepository.save(user);
		AppLog.success(log, "Zablokowano użytkownika, adminId={}, userId={}", adminId, userId);
		return UserResponse.fromEntity(user);
	}

	/**
	 * Odblokowuje konto wskazanego użytkownika.
	 *
	 * @param adminId identyfikator administratora
	 * @param userId identyfikator użytkownika docelowego
	 * @return zaktualizowany profil użytkownika
	 * @throws IllegalArgumentException gdy operacja na własnym koncie lub brak użytkownika
	 */
	@Transactional
	public UserResponse unblockUser(Integer adminId, Integer userId) {
		ensureNotSelf(adminId, userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> {
					log.warn("Odblokowanie – nie znaleziono użytkownika, userId={}", userId);
					return new IllegalArgumentException("Nie znaleziono użytkownika");
				});
		user.setIsActive(true);
		userRepository.save(user);
		AppLog.success(log, "Odblokowano użytkownika, adminId={}, userId={}", adminId, userId);
		return UserResponse.fromEntity(user);
	}

	/**
	 * Trwale usuwa użytkownika wraz z powiązanymi danymi.
	 *
	 * @param adminId identyfikator administratora
	 * @param userId identyfikator użytkownika do usunięcia
	 * @throws IllegalArgumentException gdy operacja na własnym koncie lub brak użytkownika
	 */
	@Transactional
	public void deleteUser(Integer adminId, Integer userId) {
		ensureNotSelf(adminId, userId);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> {
					log.warn("Usuwanie – nie znaleziono użytkownika, userId={}", userId);
					return new IllegalArgumentException("Nie znaleziono użytkownika");
				});

		List<WorkoutSession> sessions = workoutSessionRepository.findByUserId(userId);
		for (WorkoutSession session : sessions) {
			exerciseResultRepository.deleteBySessionId(session.getId());
		}

		workoutSessionRepository.deleteAll(sessions);

		List<Workout> workouts = workoutRepository.findByUserId(userId);
		for (Workout workout : workouts) {
			workoutExerciseRepository.deleteByWorkoutId(workout.getId());
		}

		workoutRepository.deleteAll(workouts);

		exerciseRepository.deleteAll(exerciseRepository.findByCreatedByIdAndIsCustomTrue(userId));

		entityManager.createNativeQuery("UPDATE exercises SET created_by = NULL WHERE created_by = :userId AND is_custom = false")
				.setParameter("userId", userId)
				.executeUpdate();

		entityManager.createNativeQuery("DELETE FROM user_settings WHERE user_id = :userId")
				.setParameter("userId", userId)
				.executeUpdate();

		entityManager.createNativeQuery("DELETE FROM reports WHERE user_id = :userId")
				.setParameter("userId", userId)
				.executeUpdate();

		userRepository.delete(user);
		AppLog.success(log, "Usunięto użytkownika i powiązane dane, adminId={}, userId={}", adminId, userId);
	}

	private static void ensureNotSelf(Integer adminId, Integer targetUserId) {
		if (adminId != null && adminId.equals(targetUserId)) {
			log.warn("Próba operacji administracyjnej na własnym koncie, adminId={}", adminId);
			throw new IllegalArgumentException(SELF_OPERATION_ERROR);
		}
	}

	private static String normalizeRole(String roleName) {
		return roleName == null ? null : roleName.trim().toUpperCase();
	}

	private static void validateRole(String roleName) {
		if (!"USER".equals(roleName) && !"TRAINER".equals(roleName) && !"ADMIN".equals(roleName)) {
			log.warn("Niepoprawna rola: {}", roleName);
			throw new IllegalArgumentException("Niepoprawna rola");
		}
	}
}
