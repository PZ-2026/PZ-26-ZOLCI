package com.trainit.backend.controller;

import com.trainit.backend.dto.CompleteWorkoutSessionRequest;
import com.trainit.backend.dto.CreateWorkoutSessionRequest;
import com.trainit.backend.dto.WorkoutSessionDto;
import com.trainit.backend.service.WorkoutSessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkoutSessionControllerTest {

	private final WorkoutSessionService workoutSessionService = mock(WorkoutSessionService.class);
	private final WorkoutSessionController controller = new WorkoutSessionController(workoutSessionService);

	@Test
	@DisplayName("createSession zwraca 201")
	void createSessionReturnsCreated() {
		CreateWorkoutSessionRequest request = new CreateWorkoutSessionRequest();
		request.setWorkoutId(3);
		request.setPlannedDate(LocalDateTime.now());
		WorkoutSessionDto dto = new WorkoutSessionDto();
		dto.setId(11);
		when(workoutSessionService.createSession(eq(1), any(CreateWorkoutSessionRequest.class))).thenReturn(dto);

		ResponseEntity<WorkoutSessionDto> response = controller.createSession(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(11, response.getBody().getId());
	}

	@Test
	@DisplayName("getUserSessions bez filtrów zwraca listę sesji użytkownika")
	void getUserSessionsWithoutFilters() {
		when(workoutSessionService.getUserSessions(1)).thenReturn(List.of(new WorkoutSessionDto()));

		ResponseEntity<List<WorkoutSessionDto>> response = controller.getUserSessions(null, null, null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, response.getBody().size());
	}

	@Test
	@DisplayName("getUserSessions z filtrem status wywołuje metodę status")
	void getUserSessionsWithStatusFilter() {
		when(workoutSessionService.getUserSessionsByStatus(1, "PLANNED")).thenReturn(List.of(new WorkoutSessionDto()));

		ResponseEntity<List<WorkoutSessionDto>> response = controller.getUserSessions("PLANNED", null, null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	@DisplayName("completeSession zwraca 200")
	void completeSessionReturnsOk() {
		CompleteWorkoutSessionRequest request = new CompleteWorkoutSessionRequest();
		request.setDuration(45);
		WorkoutSessionDto dto = new WorkoutSessionDto();
		dto.setStatus("COMPLETED");
		when(workoutSessionService.completeSession(eq(7), eq(1), any(CompleteWorkoutSessionRequest.class))).thenReturn(dto);

		ResponseEntity<WorkoutSessionDto> response = controller.completeSession(7, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("COMPLETED", response.getBody().getStatus());
	}

	@Test
	@DisplayName("cancelSession i deleteSession zwracają 204")
	void cancelAndDeleteReturnNoContent() {
		doNothing().when(workoutSessionService).cancelSession(4, 1);
		doNothing().when(workoutSessionService).deleteSession(4, 1);

		ResponseEntity<Void> cancel = controller.cancelSession(4);
		ResponseEntity<Void> delete = controller.deleteSession(4);

		assertEquals(HttpStatus.NO_CONTENT, cancel.getStatusCode());
		assertEquals(HttpStatus.NO_CONTENT, delete.getStatusCode());
	}
}
