package com.trainit.backend.service;

import com.trainit.backend.dto.CreateExerciseRequest;
import com.trainit.backend.dto.CreateWorkoutRequest;
import com.trainit.backend.dto.ExerciseResultRequest;
import com.trainit.backend.dto.FeatureItemResponse;
import com.trainit.backend.dto.ProfileAchievementResponse;
import com.trainit.backend.dto.ProfileOverviewResponse;
import com.trainit.backend.dto.ProfileRecordResponse;
import com.trainit.backend.dto.SessionExerciseResultResponse;
import com.trainit.backend.dto.UpdateSettingRequest;
import com.trainit.backend.dto.WorkoutExerciseLineResponse;
import com.trainit.backend.dto.WorkoutExerciseRequest;
import com.trainit.backend.dto.WorkoutPlanDetailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Rozszerzone testy jednostkowe {@link FeatureDataService} — odczyt list, CRUD i profil.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FeatureDataServiceExtendedTest {

	@Mock
	private JdbcTemplate jdbcTemplate;

	@InjectMocks
	private FeatureDataService service;

	private void stubUserExists(int userId) {
		when(jdbcTemplate.queryForObject(
				eq("SELECT COUNT(*) FROM users WHERE id = ?"),
				eq(Integer.class),
				eq(userId)
		)).thenReturn(1);
	}

	private void stubWorkoutOwned(int userId, int workoutId) {
		when(jdbcTemplate.queryForObject(
				eq("SELECT COUNT(*) FROM workouts WHERE id = ?"),
				eq(Integer.class),
				eq(workoutId)
		)).thenReturn(1);
		when(jdbcTemplate.queryForObject(
				eq("SELECT COUNT(*) FROM workouts WHERE id = ? AND user_id = ?"),
				eq(Integer.class),
				eq(workoutId),
				eq(userId)
		)).thenReturn(1);
	}

	private void stubSessionOwned(int userId, int sessionId) {
		when(jdbcTemplate.queryForObject(
				eq("SELECT COUNT(*) FROM workout_sessions WHERE id = ? AND user_id = ?"),
				eq(Integer.class),
				eq(sessionId),
				eq(userId)
		)).thenReturn(1);
	}

	private void stubInsertReturningId(int id) {
		doAnswer(invocation -> {
			KeyHolder holder = invocation.getArgument(1);
			if (holder instanceof GeneratedKeyHolder generatedKeyHolder) {
				generatedKeyHolder.getKeyList().add(Map.of("id", id));
			}
			return 1;
		}).when(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
	}

	@Test
	@DisplayName("getWorkouts zwraca listę dla użytkownika")
	void should_returnWorkouts_when_userIdProvided() {
		when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(7)))
				.thenReturn(List.of(new FeatureItemResponse(1, "Push", "Poziom: ŚREDNI, czas: 60 min")));

		List<FeatureItemResponse> result = service.getWorkouts(7);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getTitle()).isEqualTo("Push");
	}

	@Test
	@DisplayName("getWorkouts zwraca listę globalną gdy userId jest null")
	void should_returnAllWorkouts_when_userIdNull() {
		when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
				.thenReturn(List.of(new FeatureItemResponse(2, "Leg day", "Poziom: TRUDNY, czas: 75 min")));

		List<FeatureItemResponse> result = service.getWorkouts(null);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getId()).isEqualTo(2);
	}

	@Test
	@DisplayName("getExercises zwraca ćwiczenia użytkownika")
	void should_returnExercises_when_userIdProvided() {
		when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(3)))
				.thenReturn(List.of(new FeatureItemResponse(5, "Przysiad", "Nogi (własne)")));

		assertThat(service.getExercises(3)).hasSize(1);
	}

	@Test
	@DisplayName("getSessions zwraca sesje użytkownika")
	void should_returnSessions_when_userIdProvided() {
		when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(4)))
				.thenReturn(List.of(new FeatureItemResponse(10, "Sesja #10 - Plan A", "Status: UKOŃCZONE")));

		assertThat(service.getSessions(4)).hasSize(1);
	}

	@Test
	@DisplayName("getStatisticsSummary dla użytkownika liczy statystyki z filtrem user_id")
	void should_returnStatistics_when_userIdProvided() {
		when(jdbcTemplate.queryForObject(contains("FROM workouts WHERE user_id"), eq(Integer.class), eq(2)))
				.thenReturn(2);
		when(jdbcTemplate.queryForObject(contains("workout_sessions WHERE user_id"), eq(Integer.class), eq(2)))
				.thenReturn(5, 1, 120);
		when(jdbcTemplate.queryForObject(contains("FROM exercises WHERE"), eq(Integer.class), eq(2)))
				.thenReturn(8);
		when(jdbcTemplate.queryForObject(contains("date_trunc('month', now())"), eq(Double.class), eq(2)))
				.thenReturn(60.0, 50.0);

		List<FeatureItemResponse> result = service.getStatisticsSummary(2);

		assertThat(result).hasSize(7);
		assertThat(result.get(0).getSubtitle()).isEqualTo("2");
		assertThat(result.get(5).getSubtitle()).isEqualTo("20");
	}

	@Test
	@DisplayName("getProfileOverview zwraca dane profilu użytkownika")
	void should_returnProfileOverview_when_userExists() {
		stubUserExists(1);
		when(jdbcTemplate.queryForObject(contains("concat"), eq(String.class), eq(1)))
				.thenReturn("Jan Kowalski • USER");
		when(jdbcTemplate.queryForObject(contains("to_char(created_at"), eq(String.class), eq(1)))
				.thenReturn("01.01.2026");
		when(jdbcTemplate.queryForObject(contains("COUNT(*) FROM workout_sessions WHERE user_id"), eq(Integer.class), eq(1)))
				.thenReturn(10);
		when(jdbcTemplate.queryForObject(contains("SUM(duration)"), eq(Integer.class), eq(1)))
				.thenReturn(600);
		doAnswer(invocation -> null).when(jdbcTemplate).query(
				contains("EXTRACT(ISODOW"),
				any(org.springframework.jdbc.core.RowCallbackHandler.class),
				eq(1)
		);
		when(jdbcTemplate.query(contains("ORDER BY er.weight_used"), any(RowMapper.class), eq(1)))
				.thenReturn(List.of());
		when(jdbcTemplate.query(
				eq("SELECT setting_value FROM user_settings WHERE user_id = ? AND setting_key = ?"),
				any(org.springframework.jdbc.core.ResultSetExtractor.class),
				eq(1),
				eq("training_reminders")
		)).thenReturn("włączone");
		when(jdbcTemplate.query(
				eq("SELECT setting_value FROM user_settings WHERE user_id = ? AND setting_key = ?"),
				any(org.springframework.jdbc.core.ResultSetExtractor.class),
				eq(1),
				eq("privacy_mode")
		)).thenReturn("wyłączony");

		ProfileOverviewResponse overview = service.getProfileOverview(1);

		assertThat(overview.profileName()).contains("Jan Kowalski");
		assertThat(overview.workoutsText()).isEqualTo("10");
		assertThat(overview.achievements()).hasSize(6);
	}

	@Test
	@DisplayName("finishSession kończy sesję gdy update się powiedzie")
	void should_returnFinishedSession_when_updateSucceeds() {
		when(jdbcTemplate.update(
				contains("UPDATE workout_sessions"),
				eq("UKOŃCZONE"),
				any(),
				eq(45),
				eq(12),
				eq(1)
		)).thenReturn(1);

		FeatureItemResponse response = service.finishSession(1, 12, 45);

		assertThat(response.getId()).isEqualTo(12);
		assertThat(response.getSubtitle()).contains("UKOŃCZONE");
	}

	@Test
	@DisplayName("cancelSession usuwa zaplanowaną sesję")
	void should_cancelSession_when_activeSessionExists() {
		when(jdbcTemplate.update(eq("DELETE FROM exercise_results WHERE session_id = ?"), eq(20)))
				.thenReturn(0);
		when(jdbcTemplate.update(
				contains("DELETE FROM workout_sessions"),
				eq(20),
				eq(1)
		)).thenReturn(1);

		service.cancelSession(1, 20);

		verify(jdbcTemplate).update(contains("DELETE FROM workout_sessions"), eq(20), eq(1));
	}

	@Test
	@DisplayName("cancelSession rzuca błąd gdy brak aktywnej sesji")
	void should_throwIllegalArgumentException_when_cancelSessionNotFound() {
		when(jdbcTemplate.update(eq("DELETE FROM exercise_results WHERE session_id = ?"), eq(99)))
				.thenReturn(0);
		when(jdbcTemplate.update(
				contains("DELETE FROM workout_sessions"),
				eq(99),
				eq(1)
		)).thenReturn(0);

		assertThatThrownBy(() -> service.cancelSession(1, 99))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nie znaleziono aktywnej sesji");
	}

	@Test
	@DisplayName("createWorkout tworzy plan gdy użytkownik istnieje")
	void should_returnCreatedWorkout_when_userExists() {
		stubUserExists(1);
		stubInsertReturningId(100);
		CreateWorkoutRequest request = new CreateWorkoutRequest();
		request.setName("Nowy plan");
		request.setDescription("Opis");
		request.setDifficultyLevel("ŁATWY");
		request.setEstimatedDuration(30);

		FeatureItemResponse response = service.createWorkout(1, request);

		assertThat(response.getId()).isEqualTo(100);
		assertThat(response.getSubtitle()).contains("Nowy plan");
	}

	@Test
	@DisplayName("getWorkoutDetail zwraca szczegóły planu")
	void should_returnWorkoutDetail_when_userOwnsWorkout() {
		stubWorkoutOwned(1, 10);
		WorkoutPlanDetailResponse detail = new WorkoutPlanDetailResponse(10, "Plan A", "Opis", "ŚREDNI", 60);
		when(jdbcTemplate.query(
				contains("FROM workouts"),
				any(org.springframework.jdbc.core.ResultSetExtractor.class),
				eq(10),
				eq(1)
		)).thenReturn(detail);

		WorkoutPlanDetailResponse result = service.getWorkoutDetail(1, 10);

		assertThat(result.name()).isEqualTo("Plan A");
		assertThat(result.estimatedDuration()).isEqualTo(60);
	}

	@Test
	@DisplayName("updateWorkout aktualizuje plan użytkownika")
	void should_returnUpdatedWorkout_when_updateSucceeds() {
		stubWorkoutOwned(1, 5);
		when(jdbcTemplate.update(
				contains("UPDATE workouts"),
				eq("Zmieniony"),
				any(),
				any(),
				any(),
				eq(5),
				eq(1)
		)).thenReturn(1);

		CreateWorkoutRequest request = new CreateWorkoutRequest();
		request.setName("Zmieniony");

		FeatureItemResponse response = service.updateWorkout(1, 5, request);

		assertThat(response.getTitle()).isEqualTo("Zmieniony");
	}

	@Test
	@DisplayName("updateWorkout rzuca błąd gdy nazwa pusta")
	void should_throwIllegalArgumentException_when_updateWorkoutNameBlank() {
		stubWorkoutOwned(1, 5);
		CreateWorkoutRequest request = new CreateWorkoutRequest();
		request.setName("   ");

		assertThatThrownBy(() -> service.updateWorkout(1, 5, request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nazwa planu nie może być pusta");
	}

	@Test
	@DisplayName("deleteWorkout usuwa plan i powiązane dane")
	void should_deleteWorkout_when_userOwnsWorkout() {
		stubWorkoutOwned(1, 8);
		when(jdbcTemplate.update(contains("DELETE FROM exercise_results"), eq(8), eq(1))).thenReturn(1);
		when(jdbcTemplate.update(contains("DELETE FROM workout_sessions"), eq(8), eq(1))).thenReturn(1);
		when(jdbcTemplate.update(eq("DELETE FROM workout_exercises WHERE workout_id = ?"), eq(8))).thenReturn(1);
		when(jdbcTemplate.update(eq("DELETE FROM workouts WHERE id = ? AND user_id = ?"), eq(8), eq(1))).thenReturn(1);

		service.deleteWorkout(1, 8);

		verify(jdbcTemplate).update(eq("DELETE FROM workouts WHERE id = ? AND user_id = ?"), eq(8), eq(1));
	}

	@Test
	@DisplayName("listWorkoutExerciseLines zwraca pozycje planu")
	void should_returnExerciseLines_when_workoutOwned() {
		stubWorkoutOwned(1, 3);
		WorkoutExerciseLineResponse line = new WorkoutExerciseLineResponse(1, 2, "Wyciskanie", 4, 10, 80.0, null);
		when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(3), eq(1)))
				.thenReturn(List.of(line));

		List<WorkoutExerciseLineResponse> lines = service.listWorkoutExerciseLines(1, 3);

		assertThat(lines).hasSize(1);
		assertThat(lines.getFirst().exerciseName()).isEqualTo("Wyciskanie");
	}

	@Test
	@DisplayName("deleteWorkoutExerciseLine usuwa pozycję z planu")
	void should_deleteExerciseLine_when_lineExists() {
		stubWorkoutOwned(1, 3);
		when(jdbcTemplate.update(
				eq("DELETE FROM workout_exercises WHERE id = ? AND workout_id = ?"),
				eq(11),
				eq(3)
		)).thenReturn(1);

		service.deleteWorkoutExerciseLine(1, 3, 11);
	}

	@Test
	@DisplayName("createExercise tworzy własne ćwiczenie")
	void should_returnCreatedExercise_when_userExists() {
		stubUserExists(2);
		stubInsertReturningId(50);
		CreateExerciseRequest request = new CreateExerciseRequest();
		request.setName("Wyciskanie hantli");
		request.setMuscleGroup("Klatka");

		FeatureItemResponse response = service.createExercise(2, request);

		assertThat(response.getId()).isEqualTo(50);
		assertThat(response.getSubtitle()).contains("Wyciskanie hantli");
	}

	@Test
	@DisplayName("startSession tworzy sesję gdy brak aktywnej")
	void should_returnStartedSession_when_noActiveSession() {
		stubUserExists(1);
		stubWorkoutOwned(1, 5);
		when(jdbcTemplate.queryForObject(
				contains("status = 'ZAPLANOWANE'"),
				eq(Integer.class),
				eq(1)
		)).thenReturn(0);
		stubInsertReturningId(200);

		FeatureItemResponse response = service.startSession(1, 5);

		assertThat(response.getId()).isEqualTo(200);
		assertThat(response.getSubtitle()).contains("ZAPLANOWANE");
	}

	@Test
	@DisplayName("startSession rzuca IllegalStateException gdy jest aktywna sesja")
	void should_throwIllegalStateException_when_activeSessionExists() {
		stubUserExists(1);
		stubWorkoutOwned(1, 5);
		when(jdbcTemplate.queryForObject(
				contains("status = 'ZAPLANOWANE'"),
				eq(Integer.class),
				eq(1)
		)).thenReturn(1);

		assertThatThrownBy(() -> service.startSession(1, 5))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("aktywną sesję");
	}

	@Test
	@DisplayName("getSessionExerciseResults zwraca wyniki sesji")
	void should_returnSessionResults_when_sessionOwned() {
		stubSessionOwned(1, 15);
		SessionExerciseResultResponse row = new SessionExerciseResultResponse(
				1, 2, "Martwy ciąg", 3, 5, 100.0, null, "OK"
		);
		when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(15)))
				.thenReturn(List.of(row));

		List<SessionExerciseResultResponse> results = service.getSessionExerciseResults(1, 15);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).exerciseName()).isEqualTo("Martwy ciąg");
	}

	@Test
	@DisplayName("deleteSessionExerciseResult usuwa wynik")
	void should_deleteSessionExerciseResult_when_resultExists() {
		stubSessionOwned(1, 15);
		when(jdbcTemplate.update(
				eq("DELETE FROM exercise_results WHERE id = ? AND session_id = ?"),
				eq(7),
				eq(15)
		)).thenReturn(1);

		service.deleteSessionExerciseResult(1, 15, 7);
	}

	@Test
	@DisplayName("addSessionExerciseResult rzuca błąd przy ujemnych seriach")
	void should_throwIllegalArgumentException_when_negativeSets() {
		stubSessionOwned(1, 15);
		when(jdbcTemplate.queryForObject(
				contains("FROM exercises e"),
				eq(Integer.class),
				eq(2),
				eq(1)
		)).thenReturn(1);

		ExerciseResultRequest request = new ExerciseResultRequest();
		request.setExerciseId(2);
		request.setSetsDone(-1);

		assertThatThrownBy(() -> service.addSessionExerciseResult(1, 15, request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("ujemna");
	}

	@Test
	@DisplayName("updateSetting rzuca błąd dla nieznanego ustawienia")
	void should_throwIllegalArgumentException_when_unknownSettingId() {
		stubUserExists(1);
		UpdateSettingRequest request = new UpdateSettingRequest();
		request.setValue("test");

		assertThatThrownBy(() -> service.updateSetting(1, 99, request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nieznane ustawienie");
	}

	@Test
	@DisplayName("addSessionExerciseResult zapisuje wynik ćwiczenia")
	void should_returnSavedResult_when_addSessionExerciseResult() {
		stubSessionOwned(1, 15);
		when(jdbcTemplate.queryForObject(
				contains("FROM exercises e"),
				eq(Integer.class),
				eq(2),
				eq(1)
		)).thenReturn(1);
		stubInsertReturningId(77);
		SessionExerciseResultResponse saved = new SessionExerciseResultResponse(
				77, 2, "Wyciskanie", 3, 10, 60.0, null, "OK"
		);
		when(jdbcTemplate.queryForObject(contains("FROM exercise_results er"), any(RowMapper.class), eq(77)))
				.thenReturn(saved);

		ExerciseResultRequest request = new ExerciseResultRequest();
		request.setExerciseId(2);
		request.setSetsDone(3);
		request.setRepsDone(10);
		request.setWeightUsed(BigDecimal.valueOf(60));

		SessionExerciseResultResponse result = service.addSessionExerciseResult(1, 15, request);

		assertThat(result.id()).isEqualTo(77);
		assertThat(result.exerciseName()).isEqualTo("Wyciskanie");
	}

	@Test
	@DisplayName("addWorkoutExerciseLine dodaje ćwiczenie do planu")
	void should_returnExerciseLine_when_addWorkoutExerciseLine() {
		stubWorkoutOwned(1, 3);
		when(jdbcTemplate.queryForObject(
				contains("FROM exercises e"),
				eq(Integer.class),
				eq(4),
				eq(1)
		)).thenReturn(1);
		stubInsertReturningId(12);
		WorkoutExerciseLineResponse line = new WorkoutExerciseLineResponse(12, 4, "Martwy ciąg", 3, 5, 100.0, null);
		when(jdbcTemplate.queryForObject(contains("FROM workout_exercises we"), any(RowMapper.class), eq(12)))
				.thenReturn(line);

		WorkoutExerciseRequest request = new WorkoutExerciseRequest();
		request.setExerciseId(4);
		request.setSets(3);
		request.setReps(5);
		request.setWeight(BigDecimal.valueOf(100));

		WorkoutExerciseLineResponse result = service.addWorkoutExerciseLine(1, 3, request);

		assertThat(result.id()).isEqualTo(12);
		assertThat(result.exerciseName()).isEqualTo("Martwy ciąg");
	}

	@Test
	@DisplayName("updateSessionExerciseResult aktualizuje wynik")
	void should_returnUpdatedResult_when_updateSessionExerciseResult() {
		stubSessionOwned(1, 15);
		when(jdbcTemplate.queryForObject(
				contains("FROM exercises e"),
				eq(Integer.class),
				eq(2),
				eq(1)
		)).thenReturn(1);
		when(jdbcTemplate.update(
				contains("UPDATE exercise_results"),
				eq(2),
				eq(4),
				eq(8),
				eq(BigDecimal.valueOf(70)),
				isNull(),
				eq("Lepszy"),
				eq(7),
				eq(15)
		)).thenReturn(1);
		SessionExerciseResultResponse updated = new SessionExerciseResultResponse(
				7, 2, "Wyciskanie", 4, 8, 70.0, null, "Lepszy"
		);
		when(jdbcTemplate.queryForObject(contains("FROM exercise_results er"), any(RowMapper.class), eq(7), eq(15)))
				.thenReturn(updated);

		ExerciseResultRequest request = new ExerciseResultRequest();
		request.setExerciseId(2);
		request.setSetsDone(4);
		request.setRepsDone(8);
		request.setWeightUsed(BigDecimal.valueOf(70));
		request.setNotes("Lepszy");

		SessionExerciseResultResponse result = service.updateSessionExerciseResult(1, 15, 7, request);

		assertThat(result.notes()).isEqualTo("Lepszy");
	}

	@Test
	@DisplayName("updateSessionExerciseResult rzuca błąd gdy wynik nie istnieje")
	void should_throwIllegalArgumentException_when_updateResultNotFound() {
		stubSessionOwned(1, 15);
		when(jdbcTemplate.queryForObject(
				contains("FROM exercises e"),
				eq(Integer.class),
				any(),
				eq(1)
		)).thenReturn(1);
		when(jdbcTemplate.update(contains("UPDATE exercise_results"), any(), any(), any(), any(), any(), any(), eq(7), eq(15)))
				.thenReturn(0);

		ExerciseResultRequest request = new ExerciseResultRequest();
		request.setExerciseId(2);
		request.setSetsDone(3);
		request.setRepsDone(10);
		request.setWeightUsed(BigDecimal.TEN);

		assertThatThrownBy(() -> service.updateSessionExerciseResult(1, 15, 7, request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nie znaleziono wyniku");
	}

	@Test
	@DisplayName("getStatisticsSummary przy zerowych sesjach zwraca średni czas 0")
	void should_returnZeroAverageMinutes_when_noCompletedSessions() {
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workouts", Integer.class)).thenReturn(1);
		when(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM workout_sessions WHERE status = 'UKOŃCZONE'",
				Integer.class
		)).thenReturn(0);
		when(jdbcTemplate.queryForObject(contains("date_trunc('week'"), eq(Integer.class))).thenReturn(0);
		when(jdbcTemplate.queryForObject(
				"SELECT COALESCE(SUM(duration), 0) FROM workout_sessions WHERE status = 'UKOŃCZONE'",
				Integer.class
		)).thenReturn(0);
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM exercises", Integer.class)).thenReturn(3);
		when(jdbcTemplate.queryForObject(contains("date_trunc('month', now())"), eq(Double.class)))
				.thenReturn(0.0, 0.0);

		List<FeatureItemResponse> stats = service.getStatisticsSummary(null);

		assertThat(stats.get(4).getSubtitle()).isEqualTo("0");
		assertThat(stats.get(5).getSubtitle()).isEqualTo("0");
	}

	@Test
	@DisplayName("getStatisticsSummary liczy progres obciążenia 100% gdy poprzedni miesiąc bez danych")
	void should_returnHundredPercentLoadProgress_when_previousMonthWeightZero() {
		when(jdbcTemplate.queryForObject(contains("FROM workouts WHERE user_id"), eq(Integer.class), eq(3)))
				.thenReturn(1);
		when(jdbcTemplate.queryForObject(contains("workout_sessions WHERE user_id"), eq(Integer.class), eq(3)))
				.thenReturn(2, 1, 60);
		when(jdbcTemplate.queryForObject(contains("FROM exercises WHERE"), eq(Integer.class), eq(3)))
				.thenReturn(4);
		when(jdbcTemplate.queryForObject(contains("date_trunc('month', now())"), eq(Double.class), eq(3)))
				.thenReturn(80.0, 0.0);

		List<FeatureItemResponse> stats = service.getStatisticsSummary(3);

		assertThat(stats.get(5).getSubtitle()).isEqualTo("100");
	}

	@Test
	@DisplayName("getProfileOverview buduje wykres tygodnia i serię dni")
	void should_returnProfileWithWeeklyHoursAndStreak_when_trainingDataExists() {
		stubUserExists(2);
		when(jdbcTemplate.queryForObject(contains("concat"), eq(String.class), eq(2)))
				.thenReturn("Anna Nowak • USER");
		when(jdbcTemplate.queryForObject(contains("to_char(created_at"), eq(String.class), eq(2)))
				.thenReturn("15.03.2026");
		when(jdbcTemplate.queryForObject(contains("COUNT(*) FROM workout_sessions WHERE user_id"), eq(Integer.class), eq(2)))
				.thenReturn(12);
		when(jdbcTemplate.queryForObject(contains("SUM(duration)"), eq(Integer.class), eq(2)))
				.thenReturn(180);
		doAnswer(invocation -> {
			org.springframework.jdbc.core.RowCallbackHandler handler = invocation.getArgument(1);
			ResultSet rs = mock(ResultSet.class);
			when(rs.getInt("dow")).thenReturn(3);
			when(rs.getFloat("minutes")).thenReturn(120f);
			handler.processRow(rs);
			return null;
		}).when(jdbcTemplate).query(contains("EXTRACT(ISODOW"), any(org.springframework.jdbc.core.RowCallbackHandler.class), eq(2));
		LocalDate today = LocalDate.now();
		when(jdbcTemplate.query(contains("SELECT DISTINCT date"), any(RowMapper.class), eq(2)))
				.thenReturn(List.of(Date.valueOf(today), Date.valueOf(today.minusDays(1))));
		when(jdbcTemplate.query(contains("ORDER BY er.weight_used"), any(RowMapper.class), eq(2)))
				.thenReturn(List.of(new ProfileRecordResponse("Martwy ciąg", "120 kg", "01.01.2026", "5 powtórzeń")));
		when(jdbcTemplate.query(
				eq("SELECT setting_value FROM user_settings WHERE user_id = ? AND setting_key = ?"),
				any(org.springframework.jdbc.core.ResultSetExtractor.class),
				eq(2),
				eq("training_reminders")
		)).thenReturn("włączone");
		when(jdbcTemplate.query(
				eq("SELECT setting_value FROM user_settings WHERE user_id = ? AND setting_key = ?"),
				any(org.springframework.jdbc.core.ResultSetExtractor.class),
				eq(2),
				eq("privacy_mode")
		)).thenReturn("wyłączony");

		ProfileOverviewResponse overview = service.getProfileOverview(2);

		assertThat(overview.streakText()).isEqualTo("2");
		assertThat(overview.weeklyHours().get(2)).isEqualTo(2.0f);
		assertThat(overview.personalRecords()).hasSize(1);
		assertThat(overview.achievements().get(0).unlocked()).isFalse();
	}

	@Test
	@DisplayName("getProfileOverview rzuca błąd gdy użytkownik nie istnieje")
	void should_throwIllegalArgumentException_when_profileUserMissing() {
		when(jdbcTemplate.queryForObject(
				eq("SELECT COUNT(*) FROM users WHERE id = ?"),
				eq(Integer.class),
				eq(99)
		)).thenReturn(0);

		assertThatThrownBy(() -> service.getProfileOverview(99))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nie znaleziono użytkownika");
	}

	@Test
	@DisplayName("getWorkoutDetail rzuca błąd gdy plan nie istnieje")
	void should_throwIllegalArgumentException_when_workoutDetailMissing() {
		stubWorkoutOwned(1, 50);
		when(jdbcTemplate.query(
				contains("FROM workouts"),
				any(org.springframework.jdbc.core.ResultSetExtractor.class),
				eq(50),
				eq(1)
		)).thenAnswer(invocation -> {
			throw new IllegalArgumentException("Nie znaleziono planu o id=50");
		});

		assertThatThrownBy(() -> service.getWorkoutDetail(1, 50))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nie znaleziono planu");
	}

	@Test
	@DisplayName("updateWorkout rzuca błąd gdy update nie zaktualizował rekordu")
	void should_throwIllegalArgumentException_when_updateWorkoutNoRows() {
		stubWorkoutOwned(1, 5);
		when(jdbcTemplate.update(
				contains("UPDATE workouts"),
				any(),
				any(),
				any(),
				any(),
				eq(5),
				eq(1)
		)).thenReturn(0);

		CreateWorkoutRequest request = new CreateWorkoutRequest();
		request.setName("Plan");

		assertThatThrownBy(() -> service.updateWorkout(1, 5, request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nie udało się zaktualizować");
	}

	@Test
	@DisplayName("deleteWorkout rzuca błąd gdy plan nie został usunięty")
	void should_throwIllegalArgumentException_when_deleteWorkoutNoRows() {
		stubWorkoutOwned(1, 8);
		when(jdbcTemplate.update(contains("DELETE FROM exercise_results"), eq(8), eq(1))).thenReturn(0);
		when(jdbcTemplate.update(contains("DELETE FROM workout_sessions"), eq(8), eq(1))).thenReturn(0);
		when(jdbcTemplate.update(eq("DELETE FROM workout_exercises WHERE workout_id = ?"), eq(8))).thenReturn(0);
		when(jdbcTemplate.update(eq("DELETE FROM workouts WHERE id = ? AND user_id = ?"), eq(8), eq(1))).thenReturn(0);

		assertThatThrownBy(() -> service.deleteWorkout(1, 8))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nie można usunąć planu");
	}

	@Test
	@DisplayName("deleteWorkoutExerciseLine rzuca błąd gdy pozycja nie istnieje")
	void should_throwIllegalArgumentException_when_deleteExerciseLineMissing() {
		stubWorkoutOwned(1, 3);
		when(jdbcTemplate.update(
				eq("DELETE FROM workout_exercises WHERE id = ? AND workout_id = ?"),
				eq(99),
				eq(3)
		)).thenReturn(0);

		assertThatThrownBy(() -> service.deleteWorkoutExerciseLine(1, 3, 99))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nie znaleziono pozycji planu");
	}

	@Test
	@DisplayName("deleteSessionExerciseResult rzuca błąd gdy wynik nie istnieje")
	void should_throwIllegalArgumentException_when_deleteSessionResultMissing() {
		stubSessionOwned(1, 15);
		when(jdbcTemplate.update(
				eq("DELETE FROM exercise_results WHERE id = ? AND session_id = ?"),
				eq(8),
				eq(15)
		)).thenReturn(0);

		assertThatThrownBy(() -> service.deleteSessionExerciseResult(1, 15, 8))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nie znaleziono wyniku");
	}

	@Test
	@DisplayName("getSessionExerciseResults rzuca błąd gdy sesja nie należy do użytkownika")
	void should_throwIllegalArgumentException_when_sessionNotOwnedForResults() {
		when(jdbcTemplate.queryForObject(
				eq("SELECT COUNT(*) FROM workout_sessions WHERE id = ? AND user_id = ?"),
				eq(Integer.class),
				eq(20),
				eq(1)
		)).thenReturn(0);

		assertThatThrownBy(() -> service.getSessionExerciseResults(1, 20))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("nie należy do użytkownika");
	}

	@Test
	@DisplayName("getSessionExerciseResults mapuje wiersz z bazy przez RowMapper")
	void should_returnSessionResults_when_rowMapperInvoked() throws Exception {
		stubSessionOwned(1, 15);
		ResultSet rs = mock(ResultSet.class);
		when(rs.getInt("id")).thenReturn(5);
		when(rs.getInt("exercise_id")).thenReturn(2);
		when(rs.getString("exercise_name")).thenReturn("Wyciskanie");
		when(rs.getObject("sets_done")).thenReturn(3);
		when(rs.getInt("sets_done")).thenReturn(3);
		when(rs.getObject("reps_done")).thenReturn(10);
		when(rs.getInt("reps_done")).thenReturn(10);
		when(rs.getObject("weight_used")).thenReturn(new BigDecimal("60.5"));
		when(rs.getBigDecimal("weight_used")).thenReturn(new BigDecimal("60.5"));
		when(rs.getObject("duration")).thenReturn(null);
		when(rs.getString("notes")).thenReturn("dobrze");

		when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(15)))
				.thenAnswer(invocation -> {
					RowMapper<SessionExerciseResultResponse> mapper = invocation.getArgument(1);
					return List.of(mapper.mapRow(rs, 0));
				});

		List<SessionExerciseResultResponse> results = service.getSessionExerciseResults(1, 15);

		assertThat(results.getFirst().weightUsed()).isEqualTo(60.5);
		assertThat(results.getFirst().notes()).isEqualTo("dobrze");
	}

	@Test
	@DisplayName("addSessionExerciseResult rzuca IllegalStateException gdy brak wygenerowanego id")
	void should_throwIllegalStateException_when_insertResultWithoutId() {
		stubSessionOwned(1, 15);
		when(jdbcTemplate.queryForObject(
				contains("FROM exercises e"),
				eq(Integer.class),
				eq(2),
				eq(1)
		)).thenReturn(1);
		doAnswer(invocation -> 1).when(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));

		ExerciseResultRequest request = new ExerciseResultRequest();
		request.setExerciseId(2);
		request.setSetsDone(1);
		request.setRepsDone(1);

		assertThatThrownBy(() -> service.addSessionExerciseResult(1, 15, request))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Nie udało się odczytać id");
	}

	@Test
	@DisplayName("addWorkoutExerciseLine rzuca IllegalStateException gdy brak id pozycji")
	void should_throwIllegalStateException_when_insertLineWithoutId() {
		stubWorkoutOwned(1, 3);
		when(jdbcTemplate.queryForObject(
				contains("FROM exercises e"),
				eq(Integer.class),
				eq(4),
				eq(1)
		)).thenReturn(1);
		doAnswer(invocation -> 1).when(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));

		WorkoutExerciseRequest request = new WorkoutExerciseRequest();
		request.setExerciseId(4);

		assertThatThrownBy(() -> service.addWorkoutExerciseLine(1, 3, request))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	@DisplayName("getSettings rzuca błąd gdy użytkownik nie istnieje")
	void should_throwIllegalArgumentException_when_getSettingsUserMissing() {
		when(jdbcTemplate.queryForObject(
				eq("SELECT COUNT(*) FROM users WHERE id = ?"),
				eq(Integer.class),
				eq(6)
		)).thenReturn(0);

		assertThatThrownBy(() -> service.getSettings(6))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nie znaleziono użytkownika");
	}

	@Test
	@DisplayName("getExercises zwraca listę globalną gdy userId jest null")
	void should_returnAllExercises_when_userIdNull() {
		when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
				.thenReturn(List.of(new FeatureItemResponse(1, "Przysiad", "Nogi (systemowe)")));

		assertThat(service.getExercises(null)).hasSize(1);
	}

	@Test
	@DisplayName("getSessions zwraca listę globalną gdy userId jest null")
	void should_returnAllSessions_when_userIdNull() {
		when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
				.thenReturn(List.of(new FeatureItemResponse(9, "Sesja #9", "Status: UKOŃCZONE")));

		assertThat(service.getSessions(null)).hasSize(1);
	}

	@Test
	@DisplayName("validateExerciseUsable rzuca błąd przy dodawaniu niedostępnego ćwiczenia")
	void should_throwIllegalArgumentException_when_exerciseNotUsable() {
		stubSessionOwned(1, 15);
		when(jdbcTemplate.queryForObject(
				contains("FROM exercises e"),
				eq(Integer.class),
				eq(999),
				eq(1)
		)).thenReturn(0);

		ExerciseResultRequest request = new ExerciseResultRequest();
		request.setExerciseId(999);
		request.setSetsDone(1);

		assertThatThrownBy(() -> service.addSessionExerciseResult(1, 15, request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("nie jest dostępne");
	}

	@Test
	@DisplayName("getWorkouts mapuje wiersz SQL przez RowMapper")
	void should_mapWorkoutRows_when_queryInvokesMapper() throws Exception {
		when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(20)))
				.thenAnswer(invocation -> {
					RowMapper<FeatureItemResponse> mapper = invocation.getArgument(1);
					ResultSet rs = mock(ResultSet.class);
					when(rs.getInt("id")).thenReturn(5);
					when(rs.getString("title")).thenReturn("Pull");
					when(rs.getString("subtitle")).thenReturn("Poziom: ŁATWY, czas: 45 min");
					return List.of(mapper.mapRow(rs, 0));
				});

		assertThat(service.getWorkouts(20).getFirst().getTitle()).isEqualTo("Pull");
	}

	@Test
	@DisplayName("getExercises mapuje wiersz SQL przez RowMapper")
	void should_mapExerciseRows_when_queryInvokesMapper() throws Exception {
		when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(21)))
				.thenAnswer(invocation -> {
					RowMapper<FeatureItemResponse> mapper = invocation.getArgument(1);
					ResultSet rs = mock(ResultSet.class);
					when(rs.getInt("id")).thenReturn(9);
					when(rs.getString("title")).thenReturn("Przysiad");
					when(rs.getString("subtitle")).thenReturn("Nogi (systemowe)");
					return List.of(mapper.mapRow(rs, 0));
				});

		assertThat(service.getExercises(21).getFirst().getSubtitle()).contains("systemowe");
	}

	@Test
	@DisplayName("getSessions mapuje wiersz SQL przez RowMapper")
	void should_mapSessionRows_when_queryInvokesMapper() throws Exception {
		when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(22)))
				.thenAnswer(invocation -> {
					RowMapper<FeatureItemResponse> mapper = invocation.getArgument(1);
					ResultSet rs = mock(ResultSet.class);
					when(rs.getInt("id")).thenReturn(33);
					when(rs.getString("title")).thenReturn("Sesja #33 - FBW");
					when(rs.getString("subtitle")).thenReturn("Status: UKOŃCZONE");
					return List.of(mapper.mapRow(rs, 0));
				});

		assertThat(service.getSessions(22).getFirst().getId()).isEqualTo(33);
	}

	@Test
	@DisplayName("getReports mapuje wiersz SQL dla użytkownika")
	void should_mapReportRows_when_userIdProvided() throws Exception {
		when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(23)))
				.thenAnswer(invocation -> {
					RowMapper<FeatureItemResponse> mapper = invocation.getArgument(1);
					ResultSet rs = mock(ResultSet.class);
					when(rs.getInt("id")).thenReturn(1);
					when(rs.getString("title")).thenReturn("Raport: PODSUMOWANIE");
					when(rs.getString("subtitle")).thenReturn("Zakres: 2026-01-01 - 2026-01-31");
					return List.of(mapper.mapRow(rs, 0));
				});

		assertThat(service.getReports(23).getFirst().getTitle()).contains("PODSUMOWANIE");
	}

	@Test
	@DisplayName("listWorkoutExerciseLines mapuje pozycję z nullowymi polami")
	void should_mapExerciseLineWithNulls_when_rowMapperRuns() throws Exception {
		stubWorkoutOwned(1, 4);
		when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(4), eq(1)))
				.thenAnswer(invocation -> {
					RowMapper<WorkoutExerciseLineResponse> mapper = invocation.getArgument(1);
					ResultSet rs = mock(ResultSet.class);
					when(rs.getInt("line_id")).thenReturn(2);
					when(rs.getInt("exercise_id")).thenReturn(3);
					when(rs.getString("exercise_name")).thenReturn("Plank");
					when(rs.getObject("sets")).thenReturn(null);
					when(rs.getObject("reps")).thenReturn(null);
					when(rs.getObject("weight")).thenReturn(null);
					when(rs.getObject("duration")).thenReturn(null);
					return List.of(mapper.mapRow(rs, 0));
				});

		WorkoutExerciseLineResponse line = service.listWorkoutExerciseLines(1, 4).getFirst();

		assertThat(line.sets()).isNull();
		assertThat(line.weight()).isNull();
	}

	@Test
	@DisplayName("createWorkout akceptuje brak szacowanego czasu trwania")
	void should_createWorkout_when_estimatedDurationNull() {
		stubUserExists(3);
		stubInsertReturningId(88);
		CreateWorkoutRequest request = new CreateWorkoutRequest();
		request.setName("  Cardio  ");
		request.setDescription("Opis");
		request.setDifficultyLevel("ŁATWY");
		request.setEstimatedDuration(null);

		FeatureItemResponse response = service.createWorkout(3, request);

		assertThat(response.getId()).isEqualTo(88);
		assertThat(response.getSubtitle()).contains("Cardio");
	}

	@Test
	@DisplayName("getStatisticsSummary dla null userId liczy średni czas sesji")
	void should_computeAverageMinutes_when_globalSessionsExist() {
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workouts", Integer.class)).thenReturn(2);
		when(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM workout_sessions WHERE status = 'UKOŃCZONE'",
				Integer.class
		)).thenReturn(4);
		when(jdbcTemplate.queryForObject(contains("date_trunc('week'"), eq(Integer.class))).thenReturn(1);
		when(jdbcTemplate.queryForObject(
				"SELECT COALESCE(SUM(duration), 0) FROM workout_sessions WHERE status = 'UKOŃCZONE'",
				Integer.class
		)).thenReturn(200);
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM exercises", Integer.class)).thenReturn(10);
		when(jdbcTemplate.queryForObject(contains("date_trunc('month', now())"), eq(Double.class)))
				.thenReturn(50.0, 40.0);

		List<FeatureItemResponse> stats = service.getStatisticsSummary(null);

		assertThat(stats.get(4).getSubtitle()).isEqualTo("50");
		assertThat(stats.get(5).getSubtitle()).isEqualTo("25");
	}

	@Test
	@DisplayName("getProfileOverview odblokowuje osiągnięcia przy wysokiej serii i liczbie treningów")
	void should_unlockAchievements_when_streakAndWorkoutsHigh() {
		stubUserExists(4);
		when(jdbcTemplate.queryForObject(contains("concat"), eq(String.class), eq(4)))
				.thenReturn("Jan Kowalski • USER");
		when(jdbcTemplate.queryForObject(contains("to_char(created_at"), eq(String.class), eq(4)))
				.thenReturn("01.01.2020");
		when(jdbcTemplate.queryForObject(contains("COUNT(*) FROM workout_sessions WHERE user_id"), eq(Integer.class), eq(4)))
				.thenReturn(100);
		when(jdbcTemplate.queryForObject(contains("SUM(duration)"), eq(Integer.class), eq(4)))
				.thenReturn(6000);
		doAnswer(invocation -> null).when(jdbcTemplate)
				.query(contains("EXTRACT(ISODOW"), any(org.springframework.jdbc.core.RowCallbackHandler.class), eq(4));
		LocalDate today = LocalDate.now();
		List<Date> streakDates = new java.util.ArrayList<>();
		for (int i = 0; i < 8; i++) {
			streakDates.add(Date.valueOf(today.minusDays(i)));
		}
		when(jdbcTemplate.query(contains("SELECT DISTINCT date"), any(RowMapper.class), eq(4)))
				.thenReturn(streakDates);
		when(jdbcTemplate.query(contains("ORDER BY er.weight_used"), any(RowMapper.class), eq(4)))
				.thenAnswer(invocation -> {
					RowMapper<ProfileRecordResponse> mapper = invocation.getArgument(1);
					ResultSet rs = mock(ResultSet.class);
					when(rs.getString("exercise_name")).thenReturn("Brzuszki");
					when(rs.getObject("weight_used")).thenReturn(null);
					when(rs.getObject("reps_done")).thenReturn(null);
					when(rs.getString("result_date")).thenReturn("10.05.2026");
					return List.of(mapper.mapRow(rs, 0));
				});
		when(jdbcTemplate.query(
				eq("SELECT setting_value FROM user_settings WHERE user_id = ? AND setting_key = ?"),
				any(org.springframework.jdbc.core.ResultSetExtractor.class),
				eq(4),
				any()
		)).thenReturn("włączone");

		ProfileOverviewResponse overview = service.getProfileOverview(4);

		assertThat(overview.achievements().stream().filter(ProfileAchievementResponse::unlocked).count())
				.isGreaterThanOrEqualTo(3);
		assertThat(overview.personalRecords().getFirst().weight()).isEqualTo("-");
	}

	@Test
	@DisplayName("validateExerciseResultRequest odrzuca ujemne powtórzenia i ciężar")
	void should_throwIllegalArgumentException_when_negativeRepsOrWeight() {
		stubSessionOwned(1, 15);
		when(jdbcTemplate.queryForObject(contains("FROM exercises e"), eq(Integer.class), eq(2), eq(1)))
				.thenReturn(1);

		ExerciseResultRequest repsRequest = new ExerciseResultRequest();
		repsRequest.setExerciseId(2);
		repsRequest.setRepsDone(-1);
		assertThatThrownBy(() -> service.addSessionExerciseResult(1, 15, repsRequest))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("powtórzeń");

		ExerciseResultRequest weightRequest = new ExerciseResultRequest();
		weightRequest.setExerciseId(2);
		weightRequest.setWeightUsed(BigDecimal.valueOf(-5));
		assertThatThrownBy(() -> service.addSessionExerciseResult(1, 15, weightRequest))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Ciężar");
	}

	@Test
	@DisplayName("getSessionExerciseResults rzuca błąd gdy sesja nie należy do użytkownika")
	void should_throwIllegalArgumentException_when_sessionNotOwned() {
		when(jdbcTemplate.queryForObject(
				eq("SELECT COUNT(*) FROM workout_sessions WHERE id = ? AND user_id = ?"),
				eq(Integer.class),
				eq(99),
				eq(1)
		)).thenReturn(0);

		assertThatThrownBy(() -> service.getSessionExerciseResults(1, 99))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("nie należy");
	}
}
