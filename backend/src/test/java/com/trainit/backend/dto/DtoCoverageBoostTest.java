package com.trainit.backend.dto;

import com.trainit.backend.security.JwtPrincipal;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoCoverageBoostTest {

	@Test
	void featureItemResponse_constructorsAndAccessorsWork() {
		FeatureItemResponse empty = new FeatureItemResponse();
		empty.setId(9);
		empty.setTitle("T");
		empty.setSubtitle("S");
		assertThat(empty.getId()).isEqualTo(9);
		assertThat(empty.getTitle()).isEqualTo("T");
		assertThat(empty.getSubtitle()).isEqualTo("S");

		FeatureItemResponse full = new FeatureItemResponse(2, "A", "B");
		assertThat(full.getId()).isEqualTo(2);
		assertThat(full.getTitle()).isEqualTo("A");
		assertThat(full.getSubtitle()).isEqualTo("B");

		FeatureItemResponse shortCtor = new FeatureItemResponse("X", "Y");
		assertThat(shortCtor.getId()).isNull();
		assertThat(shortCtor.getTitle()).isEqualTo("X");
		assertThat(shortCtor.getSubtitle()).isEqualTo("Y");
	}

	@Test
	void exerciseDto_accessorsWork() {
		ExerciseDto dto = new ExerciseDto();
		dto.setId(1);
		dto.setName("Przysiad");
		dto.setMuscleGroup("Nogi");
		dto.setDescription("Opis");
		dto.setIsCustom(true);
		dto.setCreatedById(4);
		dto.setCreatedByEmail("u@test.com");

		assertThat(dto.getId()).isEqualTo(1);
		assertThat(dto.getName()).isEqualTo("Przysiad");
		assertThat(dto.getMuscleGroup()).isEqualTo("Nogi");
		assertThat(dto.getDescription()).isEqualTo("Opis");
		assertThat(dto.getIsCustom()).isTrue();
		assertThat(dto.getCreatedById()).isEqualTo(4);
		assertThat(dto.getCreatedByEmail()).isEqualTo("u@test.com");

		ExerciseDto ctor = new ExerciseDto(2, "Martwy", "Plecy", "Opis2", false);
		assertThat(ctor.getId()).isEqualTo(2);
		assertThat(ctor.getName()).isEqualTo("Martwy");
		assertThat(ctor.getIsCustom()).isFalse();
	}

	@Test
	void workoutDto_accessorsWork() {
		WorkoutDto dto = new WorkoutDto();
		LocalDateTime createdAt = LocalDateTime.now();
		dto.setId(3);
		dto.setName("Plan A");
		dto.setDescription("Opis");
		dto.setDifficultyLevel("ŚREDNI");
		dto.setEstimatedDuration(45);
		dto.setCreatedAt(createdAt);
		dto.setExercises(List.of(new WorkoutExerciseDto()));

		assertThat(dto.getId()).isEqualTo(3);
		assertThat(dto.getName()).isEqualTo("Plan A");
		assertThat(dto.getDescription()).isEqualTo("Opis");
		assertThat(dto.getDifficultyLevel()).isEqualTo("ŚREDNI");
		assertThat(dto.getEstimatedDuration()).isEqualTo(45);
		assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
		assertThat(dto.getExercises()).hasSize(1);
	}

	@Test
	void recordDtosAndPrincipal_canBeInstantiated() {
		ProfileRecordResponse record = new ProfileRecordResponse("Przysiad", "100 kg", "2026-01-01", "5");
		ProfileAchievementResponse achievement = new ProfileAchievementResponse("k1", "Pierwszy trening", true);
		JwtPrincipal principal = new JwtPrincipal(1, "u@test.com", "USER");

		assertThat(record.exercise()).isEqualTo("Przysiad");
		assertThat(achievement.unlocked()).isTrue();
		assertThat(principal.role()).isEqualTo("USER");
	}
}
