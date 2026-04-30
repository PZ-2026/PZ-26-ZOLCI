package com.trainit.backend.service;

import com.trainit.backend.dto.CreateExerciseRequest;
import com.trainit.backend.dto.CreateWorkoutRequest;
import com.trainit.backend.dto.FeatureItemResponse;
import com.trainit.backend.dto.UpdateSettingRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class FeatureDataServiceTest {

	private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
	private final FeatureDataService service = new FeatureDataService(jdbcTemplate);

	@Test
	@DisplayName("getStatisticsSummary zwraca pełny zestaw liczników")
	void getStatisticsSummaryReturnsCounters() {
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workouts", Integer.class)).thenReturn(3);
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sessions WHERE status = 'UKOŃCZONE'", Integer.class)).thenReturn(2);
		when(jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*) FROM workout_sessions
				WHERE status = 'UKOŃCZONE'
				  AND COALESCE(completed_date, planned_date) >= date_trunc('week', now())
				""",
				Integer.class
		)).thenReturn(1);
		when(jdbcTemplate.queryForObject(
				"SELECT COALESCE(SUM(duration), 0) FROM workout_sessions WHERE status = 'UKOŃCZONE'",
				Integer.class
		)).thenReturn(120);
		when(jdbcTemplate.queryForObject(
				"""
				SELECT COALESCE(AVG(er.weight_used), 0)
				FROM exercise_results er
				JOIN workout_sessions ws ON ws.id = er.session_id
				WHERE ws.status = 'UKOŃCZONE'
				  AND er.weight_used IS NOT NULL
				  AND COALESCE(ws.completed_date, ws.planned_date) >= date_trunc('month', now())
				""",
				Double.class
		)).thenReturn(60.0);
		when(jdbcTemplate.queryForObject(
				"""
				SELECT COALESCE(AVG(er.weight_used), 0)
				FROM exercise_results er
				JOIN workout_sessions ws ON ws.id = er.session_id
				WHERE ws.status = 'UKOŃCZONE'
				  AND er.weight_used IS NOT NULL
				  AND COALESCE(ws.completed_date, ws.planned_date) >= date_trunc('month', now() - interval '1 month')
				  AND COALESCE(ws.completed_date, ws.planned_date) < date_trunc('month', now())
				""",
				Double.class
		)).thenReturn(50.0);
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM exercises", Integer.class)).thenReturn(8);

		List<FeatureItemResponse> result = service.getStatisticsSummary(null);

		assertEquals(7, result.size());
		assertEquals("3", result.get(0).getSubtitle());
		assertEquals("2", result.get(1).getSubtitle());
		assertEquals("1", result.get(2).getSubtitle());
		assertEquals("120", result.get(3).getSubtitle());
		assertEquals("60", result.get(4).getSubtitle());
		assertEquals("20", result.get(5).getSubtitle());
		assertEquals("8", result.get(6).getSubtitle());
	}

	@Test
	@DisplayName("createWorkout rzuca błąd gdy user nie istnieje")
	void createWorkoutThrowsWhenUserMissing() {
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, 999))
				.thenReturn(0);

		CreateWorkoutRequest request = new CreateWorkoutRequest();
		request.setName("Plan");

		assertThrows(IllegalArgumentException.class, () -> service.createWorkout(999, request));
	}

	@Test
	@DisplayName("createExercise rzuca błąd gdy user nie istnieje")
	void createExerciseThrowsWhenUserMissing() {
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, 404))
				.thenReturn(0);

		CreateExerciseRequest request = new CreateExerciseRequest();
		request.setName("Ćwiczenie");

		assertThrows(IllegalArgumentException.class, () -> service.createExercise(404, request));
	}

	@Test
	@DisplayName("startSession rzuca błąd gdy workout nie istnieje")
	void startSessionThrowsWhenWorkoutMissing() {
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, 1))
				.thenReturn(1);
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workouts WHERE id = ?", Integer.class, 123))
				.thenReturn(1);
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workouts WHERE id = ? AND user_id = ?", Integer.class, 123, 1))
				.thenReturn(0);

		assertThrows(IllegalArgumentException.class, () -> service.startSession(1, 123));
	}

	@Test
	@DisplayName("finishSession rzuca błąd gdy nie zaktualizowano żadnego rekordu")
	void finishSessionThrowsWhenSessionMissing() {
		when(jdbcTemplate.update(any(String.class), eq("UKOŃCZONE"), any(), eq(30), eq(55), eq(1)))
				.thenReturn(0);

		assertThrows(IllegalArgumentException.class, () -> service.finishSession(1, 55, 30));
	}

	@Test
	@DisplayName("getReports dla null userId zwraca dane globalne")
	void getReportsForNullUserReturnsGlobalData() {
		when(jdbcTemplate.query(any(String.class), any(RowMapper.class)))
				.thenReturn(List.of(new FeatureItemResponse(1, "Raport: TYGODNIOWY", "Zakres: 2026-01-01 - 2026-01-07")));

		List<FeatureItemResponse> result = service.getReports(null);

		assertEquals(1, result.size());
		assertEquals("Raport: TYGODNIOWY", result.get(0).getTitle());
	}

	@Test
	@DisplayName("getReports dla userId zwraca dane użytkownika")
	void getReportsForUserReturnsUserData() {
		when(jdbcTemplate.query(any(String.class), any(RowMapper.class), eq(4)))
				.thenReturn(List.of(new FeatureItemResponse(2, "Raport: MIESIĘCZNY", "Zakres: 2026-01-01 - 2026-01-31")));

		List<FeatureItemResponse> result = service.getReports(4);

		assertEquals(1, result.size());
		assertEquals("Raport: MIESIĘCZNY", result.get(0).getTitle());
	}

	@Test
	@DisplayName("getSettings dla null userId zwraca fallback")
	void getSettingsForNullUserReturnsDefaults() {
		List<FeatureItemResponse> result = service.getSettings(null);
		assertEquals(4, result.size());
		assertEquals("Jednostki", result.get(0).getTitle());
	}

	@Test
	@DisplayName("updateSetting rzuca błąd dla pustej wartości")
	void updateSettingBlankValueThrows() {
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, 1)).thenReturn(1);
		UpdateSettingRequest request = new UpdateSettingRequest();
		request.setValue("   ");
		assertThrows(IllegalArgumentException.class, () -> service.updateSetting(1, 2, request));
	}

	@Test
	@DisplayName("updateSetting zapisuje wartość i zwraca DTO")
	void updateSettingSavesValue() {
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, 1)).thenReturn(1);
		when(jdbcTemplate.update(any(String.class), eq(1), eq("training_reminders"), eq("włączone"))).thenReturn(1);

		UpdateSettingRequest request = new UpdateSettingRequest();
		request.setValue("włączone");
		FeatureItemResponse response = service.updateSetting(1, 2, request);

		assertEquals("Przypomnienia treningowe", response.getTitle());
		assertEquals("włączone", response.getSubtitle());
	}

	@Test
	@DisplayName("getNotifications dla użytkownika zwraca komplet pozycji")
	void getNotificationsForUserReturnsItems() {
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sessions WHERE status = 'ZAPLANOWANE' AND user_id = ?", Integer.class, 5))
				.thenReturn(3);
		when(jdbcTemplate.query(any(String.class), any(ResultSetExtractor.class), eq(5)))
				.thenReturn("włączone")
				.thenReturn(new FeatureItemResponse(77, "Najbliższy trening", "2026-05-01 10:00"));

		List<FeatureItemResponse> result = service.getNotifications(5);

		assertEquals(4, result.size());
		assertEquals("Przypomnienia treningowe", result.get(0).getTitle());
		assertEquals("3", result.get(1).getSubtitle());
		assertEquals("Najbliższy trening", result.get(2).getTitle());
		assertEquals("Aktywne", result.get(3).getSubtitle());
	}

	@Test
	@DisplayName("getSettings dla użytkownika wykonuje inicjalizację i mapowanie")
	void getSettingsForUserInitializesDefaultsAndMapsRows() {
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, 5)).thenReturn(1);
		when(jdbcTemplate.update(any(String.class), eq(5), any(String.class), any(String.class))).thenReturn(1);
		when(jdbcTemplate.query(any(String.class), any(RowMapper.class), eq(5)))
				.thenReturn(List.of(
						new FeatureItemResponse(1, "Jednostki", "kg"),
						new FeatureItemResponse(2, "Przypomnienia treningowe", "włączone"),
						new FeatureItemResponse(3, "Tryb prywatny", "wyłączony"),
						new FeatureItemResponse(4, "Cel tygodniowy", "5")
				));

		List<FeatureItemResponse> result = service.getSettings(5);

		assertEquals(4, result.size());
		verify(jdbcTemplate, times(4)).update(any(String.class), eq(5), any(String.class), any(String.class));
	}

	@Test
	@DisplayName("getNotifications dla null userId używa globalnych danych")
	void getNotificationsForNullUserReturnsGlobalItems() {
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sessions WHERE status = 'ZAPLANOWANE'", Integer.class))
				.thenReturn(0);

		List<FeatureItemResponse> result = service.getNotifications(null);

		assertEquals(4, result.size());
		assertEquals("Brak zaplanowanych", result.get(2).getSubtitle());
		assertEquals("Brak zaplanowanych", result.get(3).getSubtitle());
	}

	@Test
	@DisplayName("getNotifications bez najbliższego treningu zwraca fallback")
	void getNotificationsWithoutNearestSessionReturnsFallback() {
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sessions WHERE status = 'ZAPLANOWANE' AND user_id = ?", Integer.class, 9))
				.thenReturn(1);
		when(jdbcTemplate.query(any(String.class), any(ResultSetExtractor.class), eq(9)))
				.thenReturn("wyłączone")
				.thenReturn(new FeatureItemResponse("Najbliższy trening", "Brak zaplanowanych"));

		List<FeatureItemResponse> result = service.getNotifications(9);

		assertEquals(4, result.size());
		assertEquals("wyłączone", result.get(0).getSubtitle());
		assertEquals("Brak zaplanowanych", result.get(2).getSubtitle());
		assertEquals("Aktywne", result.get(3).getSubtitle());
	}
}
