package com.trainit.backend.service;

import com.trainit.backend.dto.UserResponse;
import com.trainit.backend.entity.Exercise;
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
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testy jednostkowe serwisu {@link AdminService}.
 *
 * <p>Testy weryfikują mapowanie danych użytkownika, zmianę roli, blokowanie/odblokowanie
 * oraz kaskadowe usuwanie zależności w {@link AdminService#deleteUser(Integer, Integer)}.
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private RoleRepository roleRepository;
	@Mock
	private WorkoutRepository workoutRepository;
	@Mock
	private WorkoutSessionRepository workoutSessionRepository;
	@Mock
	private ExerciseResultRepository exerciseResultRepository;
	@Mock
	private ExerciseRepository exerciseRepository;
	@Mock
	private WorkoutExerciseRepository workoutExerciseRepository;
	@Mock
	private EntityManager entityManager;

	/**
	 * Buduje serwis z mockami oraz ręcznie wstrzykuje {@link EntityManager} (pole {@code @PersistenceContext}).
	 */
	private AdminService buildService() {
		AdminService service = new AdminService(
				userRepository,
				roleRepository,
				workoutRepository,
				workoutExerciseRepository,
				workoutSessionRepository,
				exerciseResultRepository,
				exerciseRepository
		);
		try {
			var field = AdminService.class.getDeclaredField("entityManager");
			field.setAccessible(true);
			field.set(service, entityManager);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Nie udało się wstrzyknąć EntityManager do testu", e);
		}
		return service;
	}

	private static User user(Integer id, String email, String roleName, Boolean isActive) {
		User u = new User();
		u.setId(id);
		u.setEmail(email);
		u.setFirstName("Jan");
		u.setLastName("Kowalski");
		u.setIsActive(isActive);
		Role role = new Role();
		role.setName(roleName);
		u.setRole(role);
		return u;
	}

	@Test
	@DisplayName("getAllUsers() → zwraca listę zmapowanych UserResponse")
	void getAllUsers_returnsMappedResponses() {
		when(userRepository.findAll()).thenReturn(List.of(
				user(1, "a@test.com", "USER", true),
				user(2, "b@test.com", "ADMIN", false)
		));

		List<UserResponse> result = buildService().getAllUsers();

		assertThat(result).hasSize(2);
		assertThat(result.getFirst().id()).isEqualTo(1);
		assertThat(result.getFirst().role()).isEqualTo("USER");
		assertThat(result.getFirst().isActive()).isTrue();
		assertThat(result.getLast().id()).isEqualTo(2);
		assertThat(result.getLast().role()).isEqualTo("ADMIN");
		assertThat(result.getLast().isActive()).isFalse();
	}

	@Test
	@DisplayName("changeUserRole() → zmienia rolę i zapisuje")
	void changeUserRole_changesRoleAndSaves() {
		User target = user(10, "u@test.com", "USER", true);
		Role trainerRole = new Role();
		trainerRole.setName("TRAINER");

		when(userRepository.findById(10)).thenReturn(Optional.of(target));
		when(roleRepository.findByName("TRAINER")).thenReturn(Optional.of(trainerRole));
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

		UserResponse response = buildService().changeUserRole(1, 10, "TRAINER");

		assertThat(response.role()).isEqualTo("TRAINER");
		verify(userRepository).save(target);
	}

	@Test
	@DisplayName("changeUserRole() gdy adminId == userId → rzuca IllegalArgumentException")
	void changeUserRole_whenSelf_throwsIllegalArgumentException() {
		assertThatThrownBy(() -> buildService().changeUserRole(5, 5, "TRAINER"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nie możesz");
	}

	@Test
	@DisplayName("changeUserRole() z niepoprawną rolą → rzuca IllegalArgumentException")
	void changeUserRole_invalidRole_throwsIllegalArgumentException() {
		assertThatThrownBy(() -> buildService().changeUserRole(1, 10, "SUPERADMIN"))
				.isInstanceOf(IllegalArgumentException.class);
		verify(userRepository, never()).findById(any());
	}

	@Test
	@DisplayName("blockUser() → ustawia isActive=false i zapisuje")
	void blockUser_setsInactiveAndSaves() {
		User target = user(7, "x@test.com", "USER", true);
		when(userRepository.findById(7)).thenReturn(Optional.of(target));
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

		UserResponse response = buildService().blockUser(1, 7);

		assertThat(target.getIsActive()).isFalse();
		assertThat(response.isActive()).isFalse();
		verify(userRepository).save(target);
	}

	@Test
	@DisplayName("blockUser() gdy adminId == userId → rzuca IllegalArgumentException")
	void blockUser_whenSelf_throwsIllegalArgumentException() {
		assertThatThrownBy(() -> buildService().blockUser(7, 7))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nie możesz");
	}

	@Test
	@DisplayName("unblockUser() → ustawia isActive=true i zapisuje")
	void unblockUser_setsActiveAndSaves() {
		User target = user(7, "x@test.com", "USER", false);
		when(userRepository.findById(7)).thenReturn(Optional.of(target));
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

		UserResponse response = buildService().unblockUser(1, 7);

		assertThat(target.getIsActive()).isTrue();
		assertThat(response.isActive()).isTrue();
		verify(userRepository).save(target);
	}

	@Test
	@DisplayName("deleteUser() → weryfikuje kolejność wywołań usuwania zależności")
	void deleteUser_verifiesCallOrder() {
		AdminService service = buildService();

		User target = user(20, "del@test.com", "USER", true);
		when(userRepository.findById(20)).thenReturn(Optional.of(target));

		WorkoutSession s1 = new WorkoutSession();
		s1.setId(100);
		WorkoutSession s2 = new WorkoutSession();
		s2.setId(200);
		List<WorkoutSession> sessions = List.of(s1, s2);
		when(workoutSessionRepository.findByUserId(20)).thenReturn(sessions);

		Workout w1 = new Workout();
		w1.setId(10);
		Workout w2 = new Workout();
		w2.setId(11);
		List<Workout> workouts = List.of(w1, w2);
		when(workoutRepository.findByUserId(20)).thenReturn(workouts);

		List<Exercise> customExercises = List.of(new Exercise(), new Exercise());
		when(exerciseRepository.findByCreatedByIdAndIsCustomTrue(20)).thenReturn(customExercises);

		Query qUpdateExercises = mock(Query.class);
		Query qDeleteUserSettings = mock(Query.class);
		Query qDeleteReports = mock(Query.class);

		when(entityManager.createNativeQuery(eq("UPDATE exercises SET created_by = NULL WHERE created_by = :userId AND is_custom = false")))
				.thenReturn(qUpdateExercises);
		when(entityManager.createNativeQuery(eq("DELETE FROM user_settings WHERE user_id = :userId")))
				.thenReturn(qDeleteUserSettings);
		when(entityManager.createNativeQuery(eq("DELETE FROM reports WHERE user_id = :userId")))
				.thenReturn(qDeleteReports);

		when(qUpdateExercises.setParameter(anyString(), any())).thenReturn(qUpdateExercises);
		when(qDeleteUserSettings.setParameter(anyString(), any())).thenReturn(qDeleteUserSettings);
		when(qDeleteReports.setParameter(anyString(), any())).thenReturn(qDeleteReports);
		when(qUpdateExercises.executeUpdate()).thenReturn(1);
		when(qDeleteUserSettings.executeUpdate()).thenReturn(1);
		when(qDeleteReports.executeUpdate()).thenReturn(1);

		service.deleteUser(1, 20);

		InOrder order = inOrder(
				userRepository,
				workoutSessionRepository,
				exerciseResultRepository,
				workoutExerciseRepository,
				workoutRepository,
				exerciseRepository,
				entityManager,
				qUpdateExercises,
				qDeleteUserSettings,
				qDeleteReports
		);

		order.verify(userRepository).findById(20);
		order.verify(workoutSessionRepository).findByUserId(20);
		order.verify(exerciseResultRepository).deleteBySessionId(100);
		order.verify(exerciseResultRepository).deleteBySessionId(200);
		order.verify(workoutSessionRepository).deleteAll(sessions);
		order.verify(workoutRepository).findByUserId(20);
		order.verify(workoutExerciseRepository).deleteByWorkoutId(10);
		order.verify(workoutExerciseRepository).deleteByWorkoutId(11);
		order.verify(workoutRepository).deleteAll(workouts);
		order.verify(exerciseRepository).deleteAll(customExercises);
		order.verify(entityManager).createNativeQuery("UPDATE exercises SET created_by = NULL WHERE created_by = :userId AND is_custom = false");
		order.verify(qUpdateExercises).setParameter("userId", 20);
		order.verify(qUpdateExercises).executeUpdate();
		order.verify(entityManager).createNativeQuery("DELETE FROM user_settings WHERE user_id = :userId");
		order.verify(qDeleteUserSettings).setParameter("userId", 20);
		order.verify(qDeleteUserSettings).executeUpdate();
		order.verify(entityManager).createNativeQuery("DELETE FROM reports WHERE user_id = :userId");
		order.verify(qDeleteReports).setParameter("userId", 20);
		order.verify(qDeleteReports).executeUpdate();
		order.verify(userRepository).delete(target);
	}

	@Test
	@DisplayName("deleteUser() gdy adminId == userId → rzuca IllegalArgumentException")
	void deleteUser_whenSelf_throwsIllegalArgumentException() {
		assertThatThrownBy(() -> buildService().deleteUser(9, 9))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nie możesz");
		verify(userRepository, never()).findById(any());
	}
}

