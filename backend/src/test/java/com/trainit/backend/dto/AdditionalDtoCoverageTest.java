package com.trainit.backend.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dodatkowe testy DTO podnoszące pokrycie klas transferowych backendu.
 */
class AdditionalDtoCoverageTest {

	@Test
	@DisplayName("WorkoutExerciseDto poprawnie przechowuje wszystkie pola")
	void workoutExerciseDto_gettersAndSetters() {
		WorkoutExerciseDto dto = new WorkoutExerciseDto();
		dto.setId(1);
		dto.setExerciseId(2);
		dto.setExerciseName("Przysiad");
		dto.setMuscleGroup("Nogi");
		dto.setSets(4);
		dto.setReps(10);
		dto.setWeight(new BigDecimal("90.5"));
		dto.setDuration(30);

		assertThat(dto.getId()).isEqualTo(1);
		assertThat(dto.getExerciseId()).isEqualTo(2);
		assertThat(dto.getExerciseName()).isEqualTo("Przysiad");
		assertThat(dto.getMuscleGroup()).isEqualTo("Nogi");
		assertThat(dto.getSets()).isEqualTo(4);
		assertThat(dto.getReps()).isEqualTo(10);
		assertThat(dto.getWeight()).isEqualByComparingTo("90.5");
		assertThat(dto.getDuration()).isEqualTo(30);
	}

	@Test
	@DisplayName("ExerciseResultDto poprawnie przechowuje wszystkie pola")
	void exerciseResultDto_gettersAndSetters() {
		ExerciseResultDto dto = new ExerciseResultDto();
		dto.setId(10);
		dto.setExerciseId(20);
		dto.setExerciseName("Martwy ciąg");
		dto.setSetsDone(5);
		dto.setRepsDone(5);
		dto.setWeightUsed(new BigDecimal("120.0"));
		dto.setDuration(18);
		dto.setNotes("Technika OK");

		assertThat(dto.getId()).isEqualTo(10);
		assertThat(dto.getExerciseId()).isEqualTo(20);
		assertThat(dto.getExerciseName()).isEqualTo("Martwy ciąg");
		assertThat(dto.getSetsDone()).isEqualTo(5);
		assertThat(dto.getRepsDone()).isEqualTo(5);
		assertThat(dto.getWeightUsed()).isEqualByComparingTo("120.0");
		assertThat(dto.getDuration()).isEqualTo(18);
		assertThat(dto.getNotes()).isEqualTo("Technika OK");
	}

	@Test
	@DisplayName("ExerciseResultRequest poprawnie przechowuje wszystkie pola")
	void exerciseResultRequest_gettersAndSetters() {
		ExerciseResultRequest request = new ExerciseResultRequest();
		request.setExerciseId(99);
		request.setSetsDone(3);
		request.setRepsDone(12);
		request.setWeightUsed(new BigDecimal("25.0"));
		request.setDuration(12);
		request.setNotes("Bez bólu");

		assertThat(request.getExerciseId()).isEqualTo(99);
		assertThat(request.getSetsDone()).isEqualTo(3);
		assertThat(request.getRepsDone()).isEqualTo(12);
		assertThat(request.getWeightUsed()).isEqualByComparingTo("25.0");
		assertThat(request.getDuration()).isEqualTo(12);
		assertThat(request.getNotes()).isEqualTo("Bez bólu");
	}

	@Test
	@DisplayName("WorkoutExerciseRequest poprawnie przechowuje wszystkie pola")
	void workoutExerciseRequest_gettersAndSetters() {
		WorkoutExerciseRequest request = new WorkoutExerciseRequest();
		request.setExerciseId(7);
		request.setSets(4);
		request.setReps(8);
		request.setWeight(new BigDecimal("80.0"));
		request.setDuration(20);

		assertThat(request.getExerciseId()).isEqualTo(7);
		assertThat(request.getSets()).isEqualTo(4);
		assertThat(request.getReps()).isEqualTo(8);
		assertThat(request.getWeight()).isEqualByComparingTo("80.0");
		assertThat(request.getDuration()).isEqualTo(20);
	}

	@Test
	@DisplayName("WorkoutSessionDto poprawnie przechowuje listę wyników")
	void workoutSessionDto_gettersAndSetters() {
		WorkoutSessionDto dto = new WorkoutSessionDto();
		ExerciseResultDto resultDto = new ExerciseResultDto();
		resultDto.setId(1);
		LocalDateTime now = LocalDateTime.now();

		dto.setId(2);
		dto.setWorkoutId(3);
		dto.setWorkoutName("Push");
		dto.setPlannedDate(now);
		dto.setCompletedDate(now.plusHours(1));
		dto.setStatus("COMPLETED");
		dto.setDuration(60);
		dto.setExerciseResults(List.of(resultDto));

		assertThat(dto.getId()).isEqualTo(2);
		assertThat(dto.getWorkoutId()).isEqualTo(3);
		assertThat(dto.getWorkoutName()).isEqualTo("Push");
		assertThat(dto.getPlannedDate()).isEqualTo(now);
		assertThat(dto.getCompletedDate()).isEqualTo(now.plusHours(1));
		assertThat(dto.getStatus()).isEqualTo("COMPLETED");
		assertThat(dto.getDuration()).isEqualTo(60);
		assertThat(dto.getExerciseResults()).hasSize(1);
	}
}
