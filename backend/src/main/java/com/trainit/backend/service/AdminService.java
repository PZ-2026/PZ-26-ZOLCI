package com.trainit.backend.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

	private static final String SELF_OPERATION_ERROR = "Nie możesz wykonać tej operacji na własnym koncie";

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final WorkoutRepository workoutRepository;
	private final WorkoutExerciseRepository workoutExerciseRepository;
	private final WorkoutSessionRepository workoutSessionRepository;
	private final ExerciseResultRepository exerciseResultRepository;
	private final ExerciseRepository exerciseRepository;

	@PersistenceContext
	private EntityManager entityManager;

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

	@Transactional(readOnly = true)
	public List<UserResponse> getAllUsers() {
		return userRepository.findAll().stream().map(UserResponse::fromEntity).toList();
	}

	@Transactional
	public UserResponse changeUserRole(Integer adminId, Integer userId, String roleName) {
		ensureNotSelf(adminId, userId);

		String normalized = normalizeRole(roleName);
		validateRole(normalized);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika"));

		Role role = roleRepository.findByName(normalized)
				.orElseThrow(() -> new IllegalStateException("Brak roli " + normalized + " w bazie"));

		user.setRole(role);
		userRepository.save(user);
		return UserResponse.fromEntity(user);
	}

	@Transactional
	public UserResponse blockUser(Integer adminId, Integer userId) {
		ensureNotSelf(adminId, userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika"));
		user.setIsActive(false);
		userRepository.save(user);
		return UserResponse.fromEntity(user);
	}

	@Transactional
	public UserResponse unblockUser(Integer adminId, Integer userId) {
		ensureNotSelf(adminId, userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika"));
		user.setIsActive(true);
		userRepository.save(user);
		return UserResponse.fromEntity(user);
	}

	@Transactional
	public void deleteUser(Integer adminId, Integer userId) {
		ensureNotSelf(adminId, userId);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika"));

		// 1. Usuń wyniki ćwiczeń z sesji użytkownika
		List<WorkoutSession> sessions = workoutSessionRepository.findByUserId(userId);
		for (WorkoutSession session : sessions) {
			exerciseResultRepository.deleteBySessionId(session.getId());
		}

		// 2. Usuń sesje treningowe
		workoutSessionRepository.deleteAll(sessions);

		// 3. Usuń workout_exercises dla planów użytkownika
		List<Workout> workouts = workoutRepository.findByUserId(userId);
		for (Workout workout : workouts) {
			workoutExerciseRepository.deleteByWorkoutId(workout.getId());
		}

		// 4. Usuń plany treningowe
		workoutRepository.deleteAll(workouts);

		// 5a. Usuń własne (custom) ćwiczenia użytkownika
		exerciseRepository.deleteAll(exerciseRepository.findByCreatedByIdAndIsCustomTrue(userId));

		// 5b. Wyzeruj created_by dla systemowych ćwiczeń przypisanych do tego usera
		entityManager.createNativeQuery("UPDATE exercises SET created_by = NULL WHERE created_by = :userId AND is_custom = false")
				.setParameter("userId", userId)
				.executeUpdate();

		// 6. Usuń ustawienia użytkownika
		entityManager.createNativeQuery("DELETE FROM user_settings WHERE user_id = :userId")
				.setParameter("userId", userId)
				.executeUpdate();

		// 7. Usuń raporty użytkownika
		entityManager.createNativeQuery("DELETE FROM reports WHERE user_id = :userId")
				.setParameter("userId", userId)
				.executeUpdate();

		// 8. Usuń użytkownika
		userRepository.delete(user);
	}

	private static void ensureNotSelf(Integer adminId, Integer targetUserId) {
		if (adminId != null && adminId.equals(targetUserId)) {
			throw new IllegalArgumentException(SELF_OPERATION_ERROR);
		}
	}

	private static String normalizeRole(String roleName) {
		return roleName == null ? null : roleName.trim().toUpperCase();
	}

	private static void validateRole(String roleName) {
		if (!"USER".equals(roleName) && !"TRAINER".equals(roleName) && !"ADMIN".equals(roleName)) {
			throw new IllegalArgumentException("Niepoprawna rola");
		}
	}
}

