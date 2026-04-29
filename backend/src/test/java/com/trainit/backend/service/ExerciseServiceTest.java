package com.trainit.backend.service;

import com.trainit.backend.dto.CreateExerciseRequest;
import com.trainit.backend.dto.ExerciseDto;
import com.trainit.backend.entity.Exercise;
import com.trainit.backend.entity.User;
import com.trainit.backend.repository.ExerciseRepository;
import com.trainit.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testy jednostkowe serwisu {@link ExerciseService}.
 */
@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

	@Mock
	private ExerciseRepository exerciseRepository;
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private ExerciseService exerciseService;

	@Test
	@DisplayName("createExercise dla isCustom=true wiąże użytkownika twórcę")
	void createExercise_custom_setsCreator() {
		User user = new User();
		user.setId(1);
		user.setEmail("owner@example.com");

		CreateExerciseRequest request = new CreateExerciseRequest();
		request.setName("Wiosłowanie");
		request.setMuscleGroup("Plecy");
		request.setDescription("Opis");
		request.setIsCustom(true);

		Exercise saved = new Exercise();
		saved.setId(10);
		saved.setName("Wiosłowanie");
		saved.setMuscleGroup("Plecy");
		saved.setDescription("Opis");
		saved.setIsCustom(true);
		saved.setCreatedBy(user);

		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		when(exerciseRepository.save(any(Exercise.class))).thenReturn(saved);

		ExerciseDto result = exerciseService.createExercise(request, 1);

		assertThat(result.getId()).isEqualTo(10);
		assertThat(result.getCreatedById()).isEqualTo(1);
		assertThat(result.getCreatedByEmail()).isEqualTo("owner@example.com");
	}

	@Test
	@DisplayName("createExercise dla isCustom=null ustawia false")
	void createExercise_nullCustom_defaultsFalse() {
		CreateExerciseRequest request = new CreateExerciseRequest();
		request.setName("Pompki");
		request.setIsCustom(null);

		Exercise saved = new Exercise();
		saved.setId(11);
		saved.setName("Pompki");
		saved.setIsCustom(false);

		when(exerciseRepository.save(any(Exercise.class))).thenReturn(saved);

		ExerciseDto result = exerciseService.createExercise(request, 2);

		assertThat(result.getIsCustom()).isFalse();
	}

	@Test
	@DisplayName("getExercise zwraca DTO dla istniejącego rekordu")
	void getExercise_returnsDto() {
		Exercise exercise = new Exercise();
		exercise.setId(3);
		exercise.setName("Plank");
		exercise.setMuscleGroup("Core");

		when(exerciseRepository.findById(3)).thenReturn(Optional.of(exercise));

		ExerciseDto result = exerciseService.getExercise(3);

		assertThat(result.getName()).isEqualTo("Plank");
		assertThat(result.getMuscleGroup()).isEqualTo("Core");
	}

	@Test
	@DisplayName("getExercise rzuca wyjątek dla braku rekordu")
	void getExercise_throwsWhenNotFound() {
		when(exerciseRepository.findById(404)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> exerciseService.getExercise(404))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Exercise not found");
	}

	@Test
	@DisplayName("updateExercise nadpisuje pola i zapisuje encję")
	void updateExercise_updatesFields() {
		Exercise exercise = new Exercise();
		exercise.setId(9);
		exercise.setName("Old");

		CreateExerciseRequest request = new CreateExerciseRequest();
		request.setName("New");
		request.setMuscleGroup("Nogi");
		request.setDescription("Nowy opis");

		when(exerciseRepository.findById(9)).thenReturn(Optional.of(exercise));
		when(exerciseRepository.save(any(Exercise.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ExerciseDto result = exerciseService.updateExercise(9, request);

		assertThat(result.getName()).isEqualTo("New");
		assertThat(result.getMuscleGroup()).isEqualTo("Nogi");
	}

	@Test
	@DisplayName("deleteExercise usuwa istniejący rekord")
	void deleteExercise_deletesEntity() {
		Exercise exercise = new Exercise();
		exercise.setId(12);
		when(exerciseRepository.findById(12)).thenReturn(Optional.of(exercise));

		exerciseService.deleteExercise(12);

		verify(exerciseRepository).delete(exercise);
	}

	@Test
	@DisplayName("getAllExercises oraz getExercisesByMuscleGroup mapują poprawnie listy")
	void listMethods_mapCollections() {
		Exercise e1 = new Exercise();
		e1.setId(1);
		e1.setName("A");
		Exercise e2 = new Exercise();
		e2.setId(2);
		e2.setName("B");

		when(exerciseRepository.findAll()).thenReturn(List.of(e1, e2));
		when(exerciseRepository.findByMuscleGroup("Plecy")).thenReturn(List.of(e1));
		when(exerciseRepository.findByCreatedByIdAndIsCustomTrue(5)).thenReturn(List.of(e2));

		assertThat(exerciseService.getAllExercises()).hasSize(2);
		assertThat(exerciseService.getExercisesByMuscleGroup("Plecy")).hasSize(1);
		assertThat(exerciseService.getUserCustomExercises(5)).hasSize(1);
	}
}
