package com.trainit.backend.controller;

import com.trainit.backend.dto.CreateExerciseRequest;
import com.trainit.backend.dto.ExerciseDto;
import com.trainit.backend.service.ExerciseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExerciseControllerTest {

	private final ExerciseService exerciseService = mock(ExerciseService.class);
	private final ExerciseController controller = new ExerciseController(exerciseService);

	@Test
	@DisplayName("createExercise zwraca 201")
	void createExerciseReturnsCreated() {
		CreateExerciseRequest request = new CreateExerciseRequest();
		request.setName("Przysiad");
		ExerciseDto dto = new ExerciseDto();
		dto.setId(2);
		dto.setName("Przysiad");
		when(exerciseService.createExercise(any(CreateExerciseRequest.class), eq(1))).thenReturn(dto);

		ResponseEntity<ExerciseDto> response = controller.createExercise(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("Przysiad", response.getBody().getName());
	}

	@Test
	@DisplayName("getExercises bez filtra zwraca wszystkie ćwiczenia")
	void getExercisesWithoutFilterReturnsAll() {
		when(exerciseService.getAllExercises()).thenReturn(List.of(new ExerciseDto()));

		ResponseEntity<List<ExerciseDto>> response = controller.getExercises(null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, response.getBody().size());
	}

	@Test
	@DisplayName("getExercises z filtrem deleguje do muscle group")
	void getExercisesWithFilterReturnsFiltered() {
		when(exerciseService.getExercisesByMuscleGroup("PLECY")).thenReturn(List.of(new ExerciseDto(), new ExerciseDto()));

		ResponseEntity<List<ExerciseDto>> response = controller.getExercises("PLECY");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(2, response.getBody().size());
	}

	@Test
	@DisplayName("deleteExercise zwraca 204")
	void deleteExerciseReturnsNoContent() {
		doNothing().when(exerciseService).deleteExercise(9);

		ResponseEntity<Void> response = controller.deleteExercise(9);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	@DisplayName("getExercise zwraca 200 i szczegóły ćwiczenia")
	void getExerciseReturnsOk() {
		ExerciseDto dto = new ExerciseDto();
		dto.setId(3);
		dto.setName("Martwy ciąg");
		when(exerciseService.getExercise(3)).thenReturn(dto);

		ResponseEntity<ExerciseDto> response = controller.getExercise(3);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Martwy ciąg", response.getBody().getName());
	}

	@Test
	@DisplayName("getUserCustomExercises zwraca listę ćwiczeń użytkownika")
	void getUserCustomExercisesReturnsOk() {
		when(exerciseService.getUserCustomExercises(1)).thenReturn(List.of(new ExerciseDto()));

		ResponseEntity<List<ExerciseDto>> response = controller.getUserCustomExercises();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, response.getBody().size());
	}

	@Test
	@DisplayName("updateExercise zwraca 200 i zmienione ćwiczenie")
	void updateExerciseReturnsOk() {
		CreateExerciseRequest request = new CreateExerciseRequest();
		request.setName("Updated");
		ExerciseDto dto = new ExerciseDto();
		dto.setId(8);
		dto.setName("Updated");
		when(exerciseService.updateExercise(eq(8), any(CreateExerciseRequest.class))).thenReturn(dto);

		ResponseEntity<ExerciseDto> response = controller.updateExercise(8, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Updated", response.getBody().getName());
	}
}
