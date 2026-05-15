package com.trainit.backend.controller;

import com.trainit.backend.dto.CreateWorkoutRequest;
import com.trainit.backend.dto.WorkoutDto;
import com.trainit.backend.service.WorkoutService;
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

class WorkoutControllerTest {

	private final WorkoutService workoutService = mock(WorkoutService.class);
	private final WorkoutController controller = new WorkoutController(workoutService);

	@Test
	@DisplayName("createWorkout zwraca 201 i utworzony plan")
	void createWorkoutReturnsCreated() {
		CreateWorkoutRequest request = new CreateWorkoutRequest();
		request.setName("Push Day");
		WorkoutDto dto = new WorkoutDto();
		dto.setId(10);
		dto.setName("Push Day");
		when(workoutService.createWorkout(eq(1), any(CreateWorkoutRequest.class))).thenReturn(dto);

		ResponseEntity<WorkoutDto> response = controller.createWorkout(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("Push Day", response.getBody().getName());
	}

	@Test
	@DisplayName("getWorkout zwraca 200 i plan po id")
	void getWorkoutReturnsOk() {
		WorkoutDto dto = new WorkoutDto();
		dto.setId(9);
		dto.setName("Pull Day");
		when(workoutService.getWorkout(9, 1)).thenReturn(dto);

		ResponseEntity<WorkoutDto> response = controller.getWorkout(9);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Pull Day", response.getBody().getName());
	}

	@Test
	@DisplayName("getUserWorkouts zwraca 200 i listę planów")
	void getUserWorkoutsReturnsOk() {
		WorkoutDto dto = new WorkoutDto();
		dto.setId(1);
		dto.setName("Leg Day");
		when(workoutService.getUserWorkouts(1)).thenReturn(List.of(dto));

		ResponseEntity<List<WorkoutDto>> response = controller.getUserWorkouts();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, response.getBody().size());
	}

	@Test
	@DisplayName("updateWorkout zwraca 200 i zaktualizowany plan")
	void updateWorkoutReturnsOk() {
		CreateWorkoutRequest request = new CreateWorkoutRequest();
		request.setName("Updated");
		WorkoutDto dto = new WorkoutDto();
		dto.setId(7);
		dto.setName("Updated");
		when(workoutService.updateWorkout(eq(7), eq(1), any(CreateWorkoutRequest.class))).thenReturn(dto);

		ResponseEntity<WorkoutDto> response = controller.updateWorkout(7, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Updated", response.getBody().getName());
	}

	@Test
	@DisplayName("deleteWorkout zwraca 204")
	void deleteWorkoutReturnsNoContent() {
		doNothing().when(workoutService).deleteWorkout(5, 1);

		ResponseEntity<Void> response = controller.deleteWorkout(5);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}
}
