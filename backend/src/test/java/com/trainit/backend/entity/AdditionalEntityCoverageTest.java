package com.trainit.backend.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dodatkowe testy encji podnoszące pokrycie klas domenowych backendu.
 */
class AdditionalEntityCoverageTest {

	@Test
	@DisplayName("Workout przechowuje pola i prePersist ustawia createdAt")
	void workout_fieldsAndPrePersist() throws Exception {
		User user = new User();
		user.setId(1);

		Workout workout = new Workout();
		workout.setId(2);
		workout.setUser(user);
		workout.setName("FBW");
		workout.setDescription("Opis");
		workout.setDifficultyLevel("EASY");
		workout.setEstimatedDuration(45);
		workout.setCreatedAt(null);

		Method prePersist = Workout.class.getDeclaredMethod("prePersist");
		prePersist.setAccessible(true);
		prePersist.invoke(workout);

		assertThat(workout.getId()).isEqualTo(2);
		assertThat(workout.getUser()).isSameAs(user);
		assertThat(workout.getName()).isEqualTo("FBW");
		assertThat(workout.getDescription()).isEqualTo("Opis");
		assertThat(workout.getDifficultyLevel()).isEqualTo("EASY");
		assertThat(workout.getEstimatedDuration()).isEqualTo(45);
		assertThat(workout.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("Exercise przechowuje pola i prePersist ustawia isCustom")
	void exercise_fieldsAndPrePersist() throws Exception {
		User creator = new User();
		creator.setId(5);
		creator.setEmail("c@example.com");

		Exercise exercise = new Exercise();
		exercise.setId(3);
		exercise.setName("Plank");
		exercise.setMuscleGroup("Core");
		exercise.setDescription("Opis");
		exercise.setIsCustom(null);
		exercise.setCreatedBy(creator);

		Method prePersist = Exercise.class.getDeclaredMethod("prePersist");
		prePersist.setAccessible(true);
		prePersist.invoke(exercise);

		assertThat(exercise.getId()).isEqualTo(3);
		assertThat(exercise.getName()).isEqualTo("Plank");
		assertThat(exercise.getMuscleGroup()).isEqualTo("Core");
		assertThat(exercise.getDescription()).isEqualTo("Opis");
		assertThat(exercise.getIsCustom()).isFalse();
		assertThat(exercise.getCreatedBy()).isSameAs(creator);
	}

	@Test
	@DisplayName("WorkoutSession przechowuje komplet pól")
	void workoutSession_fields() {
		User user = new User();
		user.setId(1);
		Workout workout = new Workout();
		workout.setId(7);
		LocalDateTime planned = LocalDateTime.now();
		LocalDateTime completed = planned.plusHours(1);

		WorkoutSession session = new WorkoutSession();
		session.setId(10);
		session.setUser(user);
		session.setWorkout(workout);
		session.setPlannedDate(planned);
		session.setCompletedDate(completed);
		session.setStatus("COMPLETED");
		session.setDuration(61);

		assertThat(session.getId()).isEqualTo(10);
		assertThat(session.getUser()).isSameAs(user);
		assertThat(session.getWorkout()).isSameAs(workout);
		assertThat(session.getPlannedDate()).isEqualTo(planned);
		assertThat(session.getCompletedDate()).isEqualTo(completed);
		assertThat(session.getStatus()).isEqualTo("COMPLETED");
		assertThat(session.getDuration()).isEqualTo(61);
	}

	@Test
	@DisplayName("WorkoutExercise przechowuje komplet pól")
	void workoutExercise_fields() {
		Workout workout = new Workout();
		workout.setId(11);
		Exercise exercise = new Exercise();
		exercise.setId(12);

		WorkoutExercise workoutExercise = new WorkoutExercise();
		workoutExercise.setId(13);
		workoutExercise.setWorkout(workout);
		workoutExercise.setExercise(exercise);
		workoutExercise.setSets(4);
		workoutExercise.setReps(10);
		workoutExercise.setWeight(new BigDecimal("42.5"));
		workoutExercise.setDuration(15);

		assertThat(workoutExercise.getId()).isEqualTo(13);
		assertThat(workoutExercise.getWorkout()).isSameAs(workout);
		assertThat(workoutExercise.getExercise()).isSameAs(exercise);
		assertThat(workoutExercise.getSets()).isEqualTo(4);
		assertThat(workoutExercise.getReps()).isEqualTo(10);
		assertThat(workoutExercise.getWeight()).isEqualByComparingTo("42.5");
		assertThat(workoutExercise.getDuration()).isEqualTo(15);
	}

	@Test
	@DisplayName("ExerciseResult przechowuje komplet pól")
	void exerciseResult_fields() {
		WorkoutSession session = new WorkoutSession();
		session.setId(20);
		Exercise exercise = new Exercise();
		exercise.setId(21);

		ExerciseResult result = new ExerciseResult();
		result.setId(22);
		result.setSession(session);
		result.setExercise(exercise);
		result.setSetsDone(3);
		result.setRepsDone(12);
		result.setWeightUsed(new BigDecimal("30.0"));
		result.setDuration(9);
		result.setNotes("Dobre tempo");

		assertThat(result.getId()).isEqualTo(22);
		assertThat(result.getSession()).isSameAs(session);
		assertThat(result.getExercise()).isSameAs(exercise);
		assertThat(result.getSetsDone()).isEqualTo(3);
		assertThat(result.getRepsDone()).isEqualTo(12);
		assertThat(result.getWeightUsed()).isEqualByComparingTo("30.0");
		assertThat(result.getDuration()).isEqualTo(9);
		assertThat(result.getNotes()).isEqualTo("Dobre tempo");
	}
}
