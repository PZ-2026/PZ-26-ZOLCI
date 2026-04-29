package com.trainit.backend.service;

import com.trainit.backend.dto.CreateExerciseRequest;
import com.trainit.backend.dto.CreateWorkoutRequest;
import com.trainit.backend.dto.FeatureItemResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeatureDataServiceTest {

	private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
	private final FeatureDataService service = new FeatureDataService(jdbcTemplate);

	@Test
	@DisplayName("getStatisticsSummary zwraca trzy pozycje z licznikami")
	void getStatisticsSummaryReturnsCounters() {
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workouts", Integer.class)).thenReturn(3);
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sessions", Integer.class)).thenReturn(2);
		when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM exercises", Integer.class)).thenReturn(8);

		List<FeatureItemResponse> result = service.getStatisticsSummary(null);

		assertEquals(3, result.size());
		assertEquals("3", result.get(0).getSubtitle());
		assertEquals("2", result.get(1).getSubtitle());
		assertEquals("8", result.get(2).getSubtitle());
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
}
