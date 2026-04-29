package com.trainit.backend.service;

import com.trainit.backend.dto.CreateWorkoutRequest;
import com.trainit.backend.dto.WorkoutDto;
import com.trainit.backend.dto.WorkoutExerciseRequest;
import com.trainit.backend.entity.Exercise;
import com.trainit.backend.entity.User;
import com.trainit.backend.entity.Workout;
import com.trainit.backend.entity.WorkoutExercise;
import com.trainit.backend.repository.ExerciseRepository;
import com.trainit.backend.repository.UserRepository;
import com.trainit.backend.repository.WorkoutExerciseRepository;
import com.trainit.backend.repository.WorkoutRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testy jednostkowe serwisu {@link WorkoutService}.
 */
@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

	@Mock
	private WorkoutRepository workoutRepository;
	@Mock
	private WorkoutExerciseRepository workoutExerciseRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private ExerciseRepository exerciseRepository;

	@InjectMocks
	private WorkoutService workoutService;

	@Test
	@DisplayName("createWorkout zapisuje trening i ćwiczenia oraz zwraca DTO")
	void createWorkout_savesWorkoutAndExercises() {
		User user = new User();
		user.setId(1);
		user.setEmail("user@example.com");

		Exercise exercise = new Exercise();
		exercise.setId(5);
		exercise.setName("Przysiad");
		exercise.setMuscleGroup("Nogi");

		CreateWorkoutRequest request = new CreateWorkoutRequest();
		request.setName("Leg day");
		request.setDescription("Opis");
		request.setDifficultyLevel("MEDIUM");
		request.setEstimatedDuration(60);

		WorkoutExerciseRequest exerciseRequest = new WorkoutExerciseRequest();
		exerciseRequest.setExerciseId(5);
		exerciseRequest.setSets(4);
		exerciseRequest.setReps(8);
		exerciseRequest.setWeight(new BigDecimal("100.0"));
		exerciseRequest.setDuration(0);
		request.setExercises(List.of(exerciseRequest));

		Workout savedWorkout = new Workout();
		savedWorkout.setId(11);
		savedWorkout.setUser(user);
		savedWorkout.setName("Leg day");
		savedWorkout.setDescription("Opis");
		savedWorkout.setDifficultyLevel("MEDIUM");
		savedWorkout.setEstimatedDuration(60);

		WorkoutExercise savedWorkoutExercise = new WorkoutExercise();
		savedWorkoutExercise.setId(77);
		savedWorkoutExercise.setWorkout(savedWorkout);
		savedWorkoutExercise.setExercise(exercise);
		savedWorkoutExercise.setSets(4);
		savedWorkoutExercise.setReps(8);
		savedWorkoutExercise.setWeight(new BigDecimal("100.0"));

		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		when(exerciseRepository.findById(5)).thenReturn(Optional.of(exercise));
		when(workoutRepository.save(any(Workout.class))).thenReturn(savedWorkout);
		when(workoutExerciseRepository.findByWorkoutId(11)).thenReturn(List.of(savedWorkoutExercise));

		WorkoutDto result = workoutService.createWorkout(1, request);

		assertThat(result.getId()).isEqualTo(11);
		assertThat(result.getName()).isEqualTo("Leg day");
		assertThat(result.getExercises()).hasSize(1);
		assertThat(result.getExercises().get(0).getExerciseId()).isEqualTo(5);
		verify(workoutExerciseRepository, times(1)).save(any(WorkoutExercise.class));
	}

	@Test
	@DisplayName("getWorkout rzuca wyjątek gdy trening nie istnieje dla użytkownika")
	void getWorkout_throwsWhenMissing() {
		when(workoutRepository.findByIdAndUserId(99, 1)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> workoutService.getWorkout(99, 1))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Workout not found");
	}

	@Test
	@DisplayName("updateWorkout aktualizuje dane i podmienia ćwiczenia")
	void updateWorkout_updatesAndReplacesExercises() {
		User user = new User();
		user.setId(2);
		Workout workout = new Workout();
		workout.setId(22);
		workout.setUser(user);
		workout.setName("Old");

		Exercise exercise = new Exercise();
		exercise.setId(8);
		exercise.setName("Martwy ciąg");
		exercise.setMuscleGroup("Plecy");

		CreateWorkoutRequest request = new CreateWorkoutRequest();
		request.setName("New");
		request.setDescription("Nowy");
		request.setDifficultyLevel("HARD");
		request.setEstimatedDuration(75);
		WorkoutExerciseRequest workoutExerciseRequest = new WorkoutExerciseRequest();
		workoutExerciseRequest.setExerciseId(8);
		workoutExerciseRequest.setSets(5);
		workoutExerciseRequest.setReps(5);
		request.setExercises(List.of(workoutExerciseRequest));

		WorkoutExercise mappedExercise = new WorkoutExercise();
		mappedExercise.setId(201);
		mappedExercise.setWorkout(workout);
		mappedExercise.setExercise(exercise);
		mappedExercise.setSets(5);
		mappedExercise.setReps(5);

		when(workoutRepository.findByIdAndUserId(22, 2)).thenReturn(Optional.of(workout));
		when(exerciseRepository.findById(8)).thenReturn(Optional.of(exercise));
		when(workoutRepository.save(any(Workout.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(workoutExerciseRepository.findByWorkoutId(22)).thenReturn(List.of(mappedExercise));

		WorkoutDto result = workoutService.updateWorkout(22, 2, request);

		assertThat(result.getName()).isEqualTo("New");
		assertThat(result.getDifficultyLevel()).isEqualTo("HARD");
		assertThat(result.getExercises()).hasSize(1);
		verify(workoutExerciseRepository).deleteByWorkoutId(22);
	}

	@Test
	@DisplayName("deleteWorkout usuwa rekord wraz z relacjami workout_exercises")
	void deleteWorkout_deletesWorkoutAndRelations() {
		Workout workout = new Workout();
		workout.setId(33);

		when(workoutRepository.findByIdAndUserId(33, 4)).thenReturn(Optional.of(workout));

		workoutService.deleteWorkout(33, 4);

		verify(workoutExerciseRepository).deleteByWorkoutId(33);
		verify(workoutRepository).delete(workout);
	}

	@Test
	@DisplayName("getUserWorkouts mapuje listę encji na listę DTO")
	void getUserWorkouts_mapsEntitiesToDtos() {
		Workout workout = new Workout();
		workout.setId(44);
		workout.setName("Push");
		workout.setDifficultyLevel("MEDIUM");

		when(workoutRepository.findByUserId(7)).thenReturn(List.of(workout));
		when(workoutExerciseRepository.findByWorkoutId(44)).thenReturn(List.of());

		List<WorkoutDto> result = workoutService.getUserWorkouts(7);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Push");
	}
}
