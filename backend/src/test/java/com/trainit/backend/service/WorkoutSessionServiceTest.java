package com.trainit.backend.service;

import com.trainit.backend.dto.CompleteWorkoutSessionRequest;
import com.trainit.backend.dto.CreateWorkoutSessionRequest;
import com.trainit.backend.dto.ExerciseResultRequest;
import com.trainit.backend.dto.WorkoutSessionDto;
import com.trainit.backend.entity.Exercise;
import com.trainit.backend.entity.ExerciseResult;
import com.trainit.backend.entity.User;
import com.trainit.backend.entity.Workout;
import com.trainit.backend.entity.WorkoutSession;
import com.trainit.backend.repository.ExerciseRepository;
import com.trainit.backend.repository.ExerciseResultRepository;
import com.trainit.backend.repository.UserRepository;
import com.trainit.backend.repository.WorkoutRepository;
import com.trainit.backend.repository.WorkoutSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testy jednostkowe serwisu {@link WorkoutSessionService}.
 */
@ExtendWith(MockitoExtension.class)
class WorkoutSessionServiceTest {

	@Mock
	private WorkoutSessionRepository workoutSessionRepository;
	@Mock
	private WorkoutRepository workoutRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private ExerciseRepository exerciseRepository;
	@Mock
	private ExerciseResultRepository exerciseResultRepository;

	@InjectMocks
	private WorkoutSessionService workoutSessionService;

	@Test
	@DisplayName("createSession tworzy sesję ze statusem PLANNED")
	void createSession_createsPlannedSession() {
		User user = new User();
		user.setId(1);
		Workout workout = new Workout();
		workout.setId(2);
		workout.setName("FBW");

		CreateWorkoutSessionRequest request = new CreateWorkoutSessionRequest();
		request.setWorkoutId(2);
		request.setPlannedDate(LocalDateTime.now().plusDays(1));

		WorkoutSession saved = new WorkoutSession();
		saved.setId(100);
		saved.setUser(user);
		saved.setWorkout(workout);
		saved.setPlannedDate(request.getPlannedDate());
		saved.setStatus("PLANNED");

		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		when(workoutRepository.findById(2)).thenReturn(Optional.of(workout));
		when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(saved);
		when(exerciseResultRepository.findBySessionId(100)).thenReturn(List.of());

		WorkoutSessionDto dto = workoutSessionService.createSession(1, request);

		assertThat(dto.getId()).isEqualTo(100);
		assertThat(dto.getStatus()).isEqualTo("PLANNED");
		assertThat(dto.getWorkoutName()).isEqualTo("FBW");
	}

	@Test
	@DisplayName("completeSession ustawia status COMPLETED i zapisuje wyniki ćwiczeń")
	void completeSession_setsCompletedAndPersistsResults() {
		User user = new User();
		user.setId(3);
		Workout workout = new Workout();
		workout.setId(10);
		workout.setName("Push");
		WorkoutSession session = new WorkoutSession();
		session.setId(15);
		session.setUser(user);
		session.setWorkout(workout);
		session.setStatus("PLANNED");

		Exercise exercise = new Exercise();
		exercise.setId(7);
		exercise.setName("Wyciskanie");

		ExerciseResultRequest resultRequest = new ExerciseResultRequest();
		resultRequest.setExerciseId(7);
		resultRequest.setSetsDone(4);
		resultRequest.setRepsDone(8);
		resultRequest.setWeightUsed(new BigDecimal("80.0"));
		resultRequest.setDuration(20);
		resultRequest.setNotes("OK");

		CompleteWorkoutSessionRequest request = new CompleteWorkoutSessionRequest();
		request.setDuration(70);
		request.setExerciseResults(List.of(resultRequest));

		ExerciseResult savedResult = new ExerciseResult();
		savedResult.setId(1);
		savedResult.setSession(session);
		savedResult.setExercise(exercise);
		savedResult.setSetsDone(4);
		savedResult.setRepsDone(8);

		when(workoutSessionRepository.findByIdAndUserId(15, 3)).thenReturn(Optional.of(session));
		when(exerciseRepository.findById(7)).thenReturn(Optional.of(exercise));
		when(workoutSessionRepository.save(any(WorkoutSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(exerciseResultRepository.findBySessionId(15)).thenReturn(List.of(savedResult));

		WorkoutSessionDto dto = workoutSessionService.completeSession(15, 3, request);

		assertThat(dto.getStatus()).isEqualTo("COMPLETED");
		assertThat(dto.getDuration()).isEqualTo(70);
		assertThat(dto.getExerciseResults()).hasSize(1);
		verify(exerciseResultRepository).save(any(ExerciseResult.class));
	}

	@Test
	@DisplayName("cancelSession zmienia status na CANCELLED")
	void cancelSession_setsCancelledStatus() {
		WorkoutSession session = new WorkoutSession();
		session.setId(20);
		session.setStatus("PLANNED");

		when(workoutSessionRepository.findByIdAndUserId(20, 9)).thenReturn(Optional.of(session));

		workoutSessionService.cancelSession(20, 9);

		assertThat(session.getStatus()).isEqualTo("CANCELLED");
		verify(workoutSessionRepository).save(session);
	}

	@Test
	@DisplayName("deleteSession usuwa wyniki i samą sesję")
	void deleteSession_deletesResultsThenSession() {
		WorkoutSession session = new WorkoutSession();
		session.setId(30);

		when(workoutSessionRepository.findByIdAndUserId(30, 6)).thenReturn(Optional.of(session));

		workoutSessionService.deleteSession(30, 6);

		verify(exerciseResultRepository).deleteBySessionId(30);
		verify(workoutSessionRepository).delete(session);
	}

	@Test
	@DisplayName("metody listujące mapują rekordy sesji")
	void listMethods_mapSessionsToDtos() {
		Workout workout = new Workout();
		workout.setId(40);
		workout.setName("Pull");

		WorkoutSession session = new WorkoutSession();
		session.setId(41);
		session.setWorkout(workout);
		session.setStatus("PLANNED");
		session.setPlannedDate(LocalDateTime.now());

		when(workoutSessionRepository.findByUserId(4)).thenReturn(List.of(session));
		when(workoutSessionRepository.findByUserIdAndStatus(4, "PLANNED")).thenReturn(List.of(session));
		when(workoutSessionRepository.findByUserIdAndPlannedDateBetween(any(), any(), any())).thenReturn(List.of(session));
		when(exerciseResultRepository.findBySessionId(41)).thenReturn(List.of());

		assertThat(workoutSessionService.getUserSessions(4)).hasSize(1);
		assertThat(workoutSessionService.getUserSessionsByStatus(4, "PLANNED")).hasSize(1);
		assertThat(workoutSessionService.getSessionsBetweenDates(4, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)))
				.hasSize(1);
	}

	@Test
	@DisplayName("getSession rzuca wyjątek gdy brak sesji użytkownika")
	void getSession_throwsWhenMissing() {
		when(workoutSessionRepository.findByIdAndUserId(500, 1)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> workoutSessionService.getSession(500, 1))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Workout session not found");
	}
}
