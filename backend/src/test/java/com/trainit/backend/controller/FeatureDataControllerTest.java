package com.trainit.backend.controller;

import com.trainit.backend.dto.CreateExerciseRequest;
import com.trainit.backend.dto.CreateWorkoutRequest;
import com.trainit.backend.dto.ExerciseResultRequest;
import com.trainit.backend.dto.FeatureItemResponse;
import com.trainit.backend.dto.FinishSessionRequest;
import com.trainit.backend.dto.ProfileOverviewResponse;
import com.trainit.backend.dto.SessionExerciseResultResponse;
import com.trainit.backend.dto.StartSessionRequest;
import com.trainit.backend.dto.UpdateSettingRequest;
import com.trainit.backend.dto.WorkoutExerciseLineResponse;
import com.trainit.backend.dto.WorkoutExerciseRequest;
import com.trainit.backend.dto.WorkoutPlanDetailResponse;
import com.trainit.backend.security.JwtPrincipal;
import com.trainit.backend.service.FeatureDataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

class FeatureDataControllerTest {

	private final FeatureDataService featureDataService = mock(FeatureDataService.class);
	private final FeatureDataController controller = new FeatureDataController(featureDataService);

	private static Authentication userAuthentication() {
		JwtPrincipal principal = new JwtPrincipal(1, "user@test.local", "USER");
		return new UsernamePasswordAuthenticationToken(principal, null);
	}

	@Test
	@DisplayName("workouts zwraca listę elementów")
	void workoutsReturnsList() {
		when(featureDataService.getWorkouts(1)).thenReturn(List.of(
				new FeatureItemResponse("Plan A", "Opis A")
		));

		List<FeatureItemResponse> result = controller.workouts(null, userAuthentication());
		assertEquals(1, result.size());
		assertEquals("Plan A", result.get(0).getTitle());
	}

	@Test
	@DisplayName("exercises zwraca listę elementów")
	void exercisesReturnsList() {
		when(featureDataService.getExercises(1)).thenReturn(List.of(
				new FeatureItemResponse("Ćwiczenie A", "Opis A")
		));

		List<FeatureItemResponse> result = controller.exercises(null, userAuthentication());
		assertEquals(1, result.size());
		assertEquals("Ćwiczenie A", result.get(0).getTitle());
	}

	@Test
	@DisplayName("sessions zwraca listę elementów")
	void sessionsReturnsList() {
		when(featureDataService.getSessions(1)).thenReturn(List.of(
				new FeatureItemResponse("Sesja #1", "Status: ZAPLANOWANE")
		));

		List<FeatureItemResponse> result = controller.sessions(null, userAuthentication());
		assertEquals(1, result.size());
		assertEquals("Sesja #1", result.get(0).getTitle());
	}

	@Test
	@DisplayName("statisticsSummary zwraca listę podsumowań")
	void statisticsSummaryReturnsList() {
		when(featureDataService.getStatisticsSummary(1)).thenReturn(List.of(
				new FeatureItemResponse("Liczba sesji treningowych", "5")
		));

		List<FeatureItemResponse> result = controller.statisticsSummary(null, userAuthentication());
		assertEquals(1, result.size());
		assertEquals("5", result.get(0).getSubtitle());
	}

	@Test
	@DisplayName("reports zwraca listę raportów")
	void reportsReturnsList() {
		when(featureDataService.getReports(1)).thenReturn(List.of(
				new FeatureItemResponse("Raport: TYGODNIOWY", "Zakres: ...")
		));

		List<FeatureItemResponse> result = controller.reports(null, userAuthentication());
		assertEquals(1, result.size());
		assertEquals("Raport: TYGODNIOWY", result.get(0).getTitle());
	}

	@Test
	@DisplayName("settings zwraca listę ustawień")
	void settingsReturnsList() {
		when(featureDataService.getSettings(1)).thenReturn(List.of(
				new FeatureItemResponse("Rola", "USER")
		));

		List<FeatureItemResponse> result = controller.settings(null, userAuthentication());
		assertEquals(1, result.size());
		assertEquals("Rola", result.get(0).getTitle());
	}

	@Test
	@DisplayName("notifications zwraca listę powiadomień")
	void notificationsReturnsList() {
		when(featureDataService.getNotifications(1)).thenReturn(List.of(
				new FeatureItemResponse("Przypomnienia treningowe", "Aktywne")
		));

		List<FeatureItemResponse> result = controller.notifications(null, userAuthentication());
		assertEquals(1, result.size());
		assertEquals("Przypomnienia treningowe", result.get(0).getTitle());
	}

	@Test
	@DisplayName("createWorkout tworzy plan i zwraca 201")
	void createWorkoutReturnsCreated() {
		CreateWorkoutRequest request = new CreateWorkoutRequest();
		request.setName("Nowy plan");
		request.setEstimatedDuration(60);

		when(featureDataService.createWorkout(any(), any()))
				.thenReturn(new FeatureItemResponse("Plan #10", "Utworzono: Nowy plan"));

		ResponseEntity<FeatureItemResponse> response = controller.createWorkout(request, userAuthentication());
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("Plan #10", response.getBody().getTitle());
	}

	@Test
	@DisplayName("deleteWorkout zwraca 204")
	void deleteWorkoutReturnsNoContent() {
		doNothing().when(featureDataService).deleteWorkout(1, 1);

		ResponseEntity<Void> response = controller.deleteWorkout(1, userAuthentication());
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	@DisplayName("createExercise tworzy ćwiczenie i zwraca 201")
	void createExerciseReturnsCreated() {
		CreateExerciseRequest request = new CreateExerciseRequest();
		request.setName("Moje ćwiczenie");

		when(featureDataService.createExercise(any(), any()))
				.thenReturn(new FeatureItemResponse("Ćwiczenie #7", "Dodano: Moje ćwiczenie"));

		ResponseEntity<FeatureItemResponse> response = controller.createExercise(request, userAuthentication());
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("Ćwiczenie #7", response.getBody().getTitle());
	}

	@Test
	@DisplayName("startSession uruchamia sesję")
	void startSessionReturnsCreated() {
		StartSessionRequest request = new StartSessionRequest();
		request.setUserId(1);
		request.setWorkoutId(1);

		when(featureDataService.startSession(1, 1))
				.thenReturn(new FeatureItemResponse("Sesja #99", "Status: ZAPLANOWANE"));

		ResponseEntity<FeatureItemResponse> response = controller.startSession(request, userAuthentication());
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("Sesja #99", response.getBody().getTitle());
	}

	@Test
	@DisplayName("finishSession kończy sesję")
	void finishSessionReturnsOk() {
		FinishSessionRequest request = new FinishSessionRequest();
		request.setDuration(45);

		when(featureDataService.finishSession(1, 5, 45))
				.thenReturn(new FeatureItemResponse("Sesja #5", "Status: UKOŃCZONE, czas: 45 min"));

		ResponseEntity<FeatureItemResponse> response = controller.finishSession(5, request, userAuthentication());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Sesja #5", response.getBody().getTitle());
	}

	@Test
	@DisplayName("profileOverview zwraca dane profilu")
	void profileOverviewReturnsOk() {
		when(featureDataService.getProfileOverview(1)).thenReturn(
				new ProfileOverviewResponse("Jan • USER", "Członek od 01.01.2024", "2", "1h", "1", List.of(), List.of(), List.of(), List.of())
		);
		ResponseEntity<ProfileOverviewResponse> response = controller.profileOverview(userAuthentication());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Jan • USER", response.getBody().profileName());
	}

	@Test
	@DisplayName("updateSetting aktualizuje ustawienie")
	void updateSettingReturnsOk() {
		UpdateSettingRequest request = new UpdateSettingRequest();
		request.setValue("włączone");
		when(featureDataService.updateSetting(1, 2, request))
				.thenReturn(new FeatureItemResponse(2, "Przypomnienia treningowe", "włączone"));
		ResponseEntity<FeatureItemResponse> response = controller.updateSetting(2, request, userAuthentication());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("włączone", response.getBody().getSubtitle());
	}

	@Test
	@DisplayName("getWorkoutDetail zwraca szczegóły planu")
	void getWorkoutDetailReturnsOk() {
		when(featureDataService.getWorkoutDetail(1, 9))
				.thenReturn(new WorkoutPlanDetailResponse(9, "Plan", "Opis", "ŚREDNI", 60));
		ResponseEntity<WorkoutPlanDetailResponse> response = controller.getWorkoutDetail(9, userAuthentication());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Plan", response.getBody().name());
	}

	@Test
	@DisplayName("updateWorkout zwraca zaktualizowany plan")
	void updateWorkoutReturnsOk() {
		CreateWorkoutRequest request = new CreateWorkoutRequest();
		request.setName("Nowy");
		when(featureDataService.updateWorkout(1, 9, request))
				.thenReturn(new FeatureItemResponse(9, "Nowy", "Zaktualizowano: Nowy"));
		ResponseEntity<FeatureItemResponse> response = controller.updateWorkout(9, request, userAuthentication());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Nowy", response.getBody().getTitle());
	}

	@Test
	@DisplayName("listWorkoutExercises zwraca pozycje planu")
	void listWorkoutExercisesReturnsData() {
		when(featureDataService.listWorkoutExerciseLines(1, 9))
				.thenReturn(List.of(new WorkoutExerciseLineResponse(1, 2, "Przysiad", 4, 8, 100.0, null)));
		List<WorkoutExerciseLineResponse> response = controller.listWorkoutExercises(9, userAuthentication());
		assertEquals(1, response.size());
		assertEquals("Przysiad", response.get(0).exerciseName());
	}

	@Test
	@DisplayName("addWorkoutExercise zwraca 201")
	void addWorkoutExerciseReturnsCreated() {
		WorkoutExerciseRequest request = new WorkoutExerciseRequest();
		request.setExerciseId(2);
		when(featureDataService.addWorkoutExerciseLine(1, 9, request))
				.thenReturn(new WorkoutExerciseLineResponse(3, 2, "Przysiad", 4, 8, 100.0, null));
		ResponseEntity<WorkoutExerciseLineResponse> response = controller.addWorkoutExercise(9, request, userAuthentication());
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(3, response.getBody().id());
	}

	@Test
	@DisplayName("deleteWorkoutExercise zwraca 204")
	void deleteWorkoutExerciseReturnsNoContent() {
		doNothing().when(featureDataService).deleteWorkoutExerciseLine(1, 9, 3);
		ResponseEntity<Void> response = controller.deleteWorkoutExercise(9, 3, userAuthentication());
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	@DisplayName("cancelSession zwraca 204")
	void cancelSessionReturnsNoContent() {
		doNothing().when(featureDataService).cancelSession(1, 7);
		ResponseEntity<Void> response = controller.cancelSession(7, userAuthentication());
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	@DisplayName("sessionResults zwraca listę wyników")
	void sessionResultsReturnsList() {
		when(featureDataService.getSessionExerciseResults(1, 7))
				.thenReturn(List.of(new SessionExerciseResultResponse(1, 2, "Martwy ciąg", 3, 5, 120.0, null, "ok")));
		List<SessionExerciseResultResponse> result = controller.sessionResults(7, userAuthentication());
		assertEquals(1, result.size());
		assertEquals("Martwy ciąg", result.get(0).exerciseName());
	}

	@Test
	@DisplayName("addSessionResult zwraca 201")
	void addSessionResultReturnsCreated() {
		ExerciseResultRequest request = new ExerciseResultRequest();
		request.setExerciseId(2);
		when(featureDataService.addSessionExerciseResult(1, 7, request))
				.thenReturn(new SessionExerciseResultResponse(5, 2, "Martwy ciąg", 3, 5, 120.0, null, null));
		ResponseEntity<SessionExerciseResultResponse> response = controller.addSessionResult(7, request, userAuthentication());
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(5, response.getBody().id());
	}

	@Test
	@DisplayName("updateSessionResult zwraca 200")
	void updateSessionResultReturnsOk() {
		ExerciseResultRequest request = new ExerciseResultRequest();
		request.setExerciseId(2);
		when(featureDataService.updateSessionExerciseResult(1, 7, 5, request))
				.thenReturn(new SessionExerciseResultResponse(5, 2, "Martwy ciąg", 4, 6, 125.0, null, null));
		ResponseEntity<SessionExerciseResultResponse> response = controller.updateSessionResult(7, 5, request, userAuthentication());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(4, response.getBody().setsDone());
	}

	@Test
	@DisplayName("deleteSessionResult zwraca 204")
	void deleteSessionResultReturnsNoContent() {
		doNothing().when(featureDataService).deleteSessionExerciseResult(1, 7, 5);
		ResponseEntity<Void> response = controller.deleteSessionResult(7, 5, userAuthentication());
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}
}
