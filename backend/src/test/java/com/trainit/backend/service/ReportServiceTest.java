package com.trainit.backend.service;

import com.trainit.backend.pdf.model.ExerciseResultData;
import com.trainit.backend.pdf.model.SessionData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testy jednostkowe serwisu {@link ReportService}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportServiceTest {

	@Mock
	private JdbcTemplate jdbcTemplate;

	@InjectMocks
	private ReportService reportService;

	@Test
	@DisplayName("generatePdf zwraca niepusty PDF gdy użytkownik istnieje")
	void should_returnPdfBytes_when_userExists() {
		when(jdbcTemplate.queryForMap(
				eq("SELECT first_name, last_name FROM users WHERE id = ?"),
				eq(1)
		)).thenReturn(Map.of("first_name", "Jan", "last_name", "Kowalski"));
		when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1), isNull(), isNull(), isNull(), isNull()))
				.thenReturn(Collections.emptyList());
		when(jdbcTemplate.update(anyString(), eq(1), eq("PODSUMOWANIE"), isNull(), isNull()))
				.thenReturn(1);

		byte[] pdf = reportService.generatePdf(1, null, null, "PODSUMOWANIE");

		assertThat(pdf).isNotNull();
		assertThat(pdf.length).isGreaterThan(0);
		verify(jdbcTemplate).update(anyString(), eq(1), eq("PODSUMOWANIE"), isNull(), isNull());
	}

	@Test
	@DisplayName("generatePdf rzuca IllegalArgumentException gdy użytkownik nie istnieje")
	void should_returnIllegalArgumentException_when_userNotFound() {
		when(jdbcTemplate.queryForMap(
				eq("SELECT first_name, last_name FROM users WHERE id = ?"),
				eq(99)
		)).thenThrow(new EmptyResultDataAccessException(1));

		assertThatThrownBy(() -> reportService.generatePdf(99, null, null, "PODSUMOWANIE"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nie znaleziono użytkownika o id=99");
	}

	@Test
	@DisplayName("generatePdf obsługuje brak sesji treningowych")
	void should_returnPdfBytes_when_sessionsEmpty() {
		when(jdbcTemplate.queryForMap(
				eq("SELECT first_name, last_name FROM users WHERE id = ?"),
				eq(2)
		)).thenReturn(Map.of("first_name", "Anna", "last_name", "Nowak"));
		when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(2), isNull(), isNull(), isNull(), isNull()))
				.thenReturn(List.of());

		byte[] pdf = reportService.generatePdf(2, null, null, "TYGODNIOWY");

		assertThat(pdf).isNotNull();
		assertThat(pdf.length).isGreaterThan(0);
	}

	@Test
	@DisplayName("generatePdf domyślnie ustawia typ PODSUMOWANIE gdy type jest pusty")
	void should_returnPdfWithDefaultType_when_typeBlank() {
		when(jdbcTemplate.queryForMap(
				eq("SELECT first_name, last_name FROM users WHERE id = ?"),
				eq(3)
		)).thenReturn(Map.of("first_name", "Piotr", "last_name", "Zieliński"));
		when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(3), isNull(), isNull(), isNull(), isNull()))
				.thenReturn(Collections.emptyList());
		when(jdbcTemplate.update(anyString(), eq(3), eq("PODSUMOWANIE"), isNull(), isNull()))
				.thenReturn(1);

		reportService.generatePdf(3, null, null, "   ");

		ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
		verify(jdbcTemplate).update(anyString(), eq(3), typeCaptor.capture(), isNull(), isNull());
		assertThat(typeCaptor.getValue()).isEqualTo("PODSUMOWANIE");
	}

	@Test
	@DisplayName("generatePdf uwzględnia zakres dat i typ raportu")
	void should_returnPdfBytes_when_dateRangeAndTypeProvided() {
		when(jdbcTemplate.queryForMap(
				eq("SELECT first_name, last_name FROM users WHERE id = ?"),
				eq(5)
		)).thenReturn(Map.of("first_name", "Ewa", "last_name", "Lis"));
		SessionData session = new SessionData(
				"FBW",
				"2026-01-15 10:00",
				55,
				List.of(new ExerciseResultData("Przysiad", 4, 8, 90.0, null))
		);
		when(jdbcTemplate.query(
				anyString(),
				any(RowMapper.class),
				eq(5),
				eq("2026-01-01"),
				eq("2026-01-01"),
				eq("2026-01-31"),
				eq("2026-01-31")
		)).thenReturn(List.of(session));
		when(jdbcTemplate.update(anyString(), eq(5), eq("POSTĘPY"), eq("2026-01-01"), eq("2026-01-31")))
				.thenReturn(1);

		byte[] pdf = reportService.generatePdf(5, "2026-01-01", "2026-01-31", "POSTĘPY");

		assertThat(pdf).isNotEmpty();
		verify(jdbcTemplate).update(anyString(), eq(5), eq("POSTĘPY"), eq("2026-01-01"), eq("2026-01-31"));
	}

	@Test
	@DisplayName("generatePdf rzuca błąd gdy użytkownik bez imienia i nazwiska")
	void should_throwIllegalArgumentException_when_userNameEmpty() {
		when(jdbcTemplate.queryForMap(
				eq("SELECT first_name, last_name FROM users WHERE id = ?"),
				eq(4)
		)).thenReturn(Map.of("first_name", "", "last_name", ""));

		assertThatThrownBy(() -> reportService.generatePdf(4, null, null, "PODSUMOWANIE"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Nie znaleziono użytkownika");
	}

	@Test
	@DisplayName("generatePdf wywołuje mapper sesji i wyników ćwiczeń z bazy")
	void should_returnPdfWithExerciseRows_when_sessionMapperRuns() throws Exception {
		when(jdbcTemplate.queryForMap(
				eq("SELECT first_name, last_name FROM users WHERE id = ?"),
				eq(6)
		)).thenReturn(Map.of("first_name", "Jan", "last_name", "Kowalski"));
		java.sql.ResultSet sessionRs = org.mockito.Mockito.mock(java.sql.ResultSet.class);
		when(sessionRs.getInt("id")).thenReturn(10);
		when(sessionRs.getString("workout_name")).thenReturn("Push");
		when(sessionRs.getTimestamp("completed_date"))
				.thenReturn(java.sql.Timestamp.valueOf("2026-01-10 12:00:00"));
		when(sessionRs.getObject("duration")).thenReturn(50);
		when(sessionRs.getInt("duration")).thenReturn(50);

		java.sql.ResultSet resultRs = org.mockito.Mockito.mock(java.sql.ResultSet.class);
		when(resultRs.getString("exercise_name")).thenReturn("Wyciskanie");
		when(resultRs.getObject("sets_done")).thenReturn(4);
		when(resultRs.getInt("sets_done")).thenReturn(4);
		when(resultRs.getObject("reps_done")).thenReturn(8);
		when(resultRs.getInt("reps_done")).thenReturn(8);
		when(resultRs.getBigDecimal("weight_used")).thenReturn(new java.math.BigDecimal("70"));
		when(resultRs.getString("notes")).thenReturn("OK");

		when(jdbcTemplate.query(
				contains("FROM workout_sessions ws"),
				any(RowMapper.class),
				eq(6),
				isNull(),
				isNull(),
				isNull(),
				isNull()
		)).thenAnswer(invocation -> {
			RowMapper<SessionData> sessionMapper = invocation.getArgument(1);
			SessionData session = sessionMapper.mapRow(sessionRs, 0);
			assertThat(session.getResults()).hasSize(1);
			return List.of(session);
		});
		when(jdbcTemplate.query(
				contains("FROM exercise_results er"),
				any(RowMapper.class),
				eq(10)
		)).thenAnswer(invocation -> {
			RowMapper<ExerciseResultData> resultMapper = invocation.getArgument(1);
			return List.of(resultMapper.mapRow(resultRs, 0));
		});
		when(jdbcTemplate.update(anyString(), eq(6), eq("PODSUMOWANIE"), isNull(), isNull()))
				.thenReturn(1);

		byte[] pdf = reportService.generatePdf(6, null, null, "PODSUMOWANIE");

		assertThat(pdf).isNotEmpty();
	}

	@Test
	@DisplayName("generatePdf traktuje puste dateFrom/dateTo jako null w zapytaniu")
	void should_passNullDates_when_dateStringsBlank() {
		when(jdbcTemplate.queryForMap(
				eq("SELECT first_name, last_name FROM users WHERE id = ?"),
				eq(7)
		)).thenReturn(Map.of("first_name", "A", "last_name", "B"));
		when(jdbcTemplate.query(
				anyString(),
				any(RowMapper.class),
				eq(7),
				isNull(),
				isNull(),
				isNull(),
				isNull()
		)).thenReturn(Collections.emptyList());

		reportService.generatePdf(7, "  ", "  ", "TYGODNIOWY");

		verify(jdbcTemplate).query(
				anyString(),
				any(RowMapper.class),
				eq(7),
				isNull(),
				isNull(),
				isNull(),
				isNull()
		);
	}

	@Test
	@DisplayName("generatePdf mapper obsługuje null w dacie, czasie i wynikach ćwiczeń")
	void should_mapSessionWithNullFields_when_rowMapperRuns() throws Exception {
		when(jdbcTemplate.queryForMap(
				eq("SELECT first_name, last_name FROM users WHERE id = ?"),
				eq(8)
		)).thenReturn(Map.of("first_name", "Test", "last_name", "User"));
		java.sql.ResultSet sessionRs = org.mockito.Mockito.mock(java.sql.ResultSet.class);
		when(sessionRs.getInt("id")).thenReturn(20);
		when(sessionRs.getString("workout_name")).thenReturn(null);
		when(sessionRs.getTimestamp("completed_date")).thenReturn(null);
		when(sessionRs.getObject("duration")).thenReturn(null);

		java.sql.ResultSet resultRs = org.mockito.Mockito.mock(java.sql.ResultSet.class);
		when(resultRs.getString("exercise_name")).thenReturn("Plank");
		when(resultRs.getObject("sets_done")).thenReturn(null);
		when(resultRs.getObject("reps_done")).thenReturn(null);
		when(resultRs.getBigDecimal("weight_used")).thenReturn(null);
		when(resultRs.getString("notes")).thenReturn(null);

		when(jdbcTemplate.query(
				contains("FROM workout_sessions ws"),
				any(RowMapper.class),
				eq(8),
				isNull(),
				isNull(),
				isNull(),
				isNull()
		)).thenAnswer(invocation -> {
			RowMapper<SessionData> sessionMapper = invocation.getArgument(1);
			return List.of(sessionMapper.mapRow(sessionRs, 0));
		});
		when(jdbcTemplate.query(
				contains("FROM exercise_results er"),
				any(RowMapper.class),
				eq(20)
		)).thenAnswer(invocation -> {
			RowMapper<ExerciseResultData> resultMapper = invocation.getArgument(1);
			return List.of(resultMapper.mapRow(resultRs, 0));
		});

		byte[] pdf = reportService.generatePdf(8, null, null, "PODSUMOWANIE");

		assertThat(pdf).isNotEmpty();
	}
}
