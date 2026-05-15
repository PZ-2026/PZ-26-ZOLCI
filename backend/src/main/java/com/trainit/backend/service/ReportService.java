package com.trainit.backend.service;

import com.trainit.backend.util.AppLog;
import com.trainit.backend.pdf.model.ExerciseResultData;
import com.trainit.backend.pdf.model.ReportData;
import com.trainit.backend.pdf.model.SessionData;
import com.trainit.backend.pdf.service.PdfReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Serwis generujący raporty PDF na podstawie danych z bazy PostgreSQL.
 */
@Service
public class ReportService {

	private static final Logger log = LoggerFactory.getLogger(ReportService.class);

	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	/** Szablon JDBC do odczytu danych raportu z PostgreSQL. */
	private final JdbcTemplate jdbcTemplate;

	/**
	 * Tworzy serwis z wymaganym szablonem JDBC.
	 *
	 * @param jdbcTemplate szablon JDBC do odczytu danych
	 */
	public ReportService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Buduje raport PDF dla użytkownika w podanym zakresie dat.
	 *
	 * @param userId identyfikator użytkownika
	 * @param dateFrom data początkowa (yyyy-MM-dd) lub null
	 * @param dateTo data końcowa (yyyy-MM-dd) lub null
	 * @param type typ raportu
	 * @return zawartość pliku PDF
	 * @throws IllegalArgumentException gdy użytkownik nie istnieje
	 */
	@Transactional
	public byte[] generatePdf(Integer userId, String dateFrom, String dateTo, String type) {
		String userName = loadUserName(userId);
		String effectiveType = isBlank(type) ? "PODSUMOWANIE" : type.trim();
		String effectiveDateFrom = blankToNull(dateFrom);
		String effectiveDateTo = blankToNull(dateTo);

		List<SessionData> sessions = loadSessions(userId, effectiveDateFrom, effectiveDateTo);

		ReportData reportData = new ReportData(
				userName,
				effectiveType,
				effectiveDateFrom != null ? effectiveDateFrom : "—",
				effectiveDateTo != null ? effectiveDateTo : "—",
				sessions
		);

		byte[] pdf;
		try {
			pdf = new PdfReportService().generateReport(reportData);
		} catch (Exception ex) {
			log.error("Błąd generowania raportu PDF, userId={}, type={}", userId, effectiveType, ex);
			throw ex;
		}

		jdbcTemplate.update(
				"""
				INSERT INTO reports (user_id, type, date_from, date_to, generated_at, file_path)
				VALUES (?, ?, CAST(? AS DATE), CAST(? AS DATE), NOW(), 'generated')
				""",
				userId,
				effectiveType,
				effectiveDateFrom,
				effectiveDateTo
		);

		AppLog.success(log, "Wygenerowano raport PDF, userId={}, type={}, sesje={}, rozmiarBajtow={}",
				userId, effectiveType, sessions.size(), pdf.length);
		return pdf;
	}

	private String loadUserName(Integer userId) {
		try {
			Map<String, Object> row = jdbcTemplate.queryForMap(
					"SELECT first_name, last_name FROM users WHERE id = ?",
					userId
			);
			String firstName = row.get("first_name") != null ? row.get("first_name").toString() : "";
			String lastName = row.get("last_name") != null ? row.get("last_name").toString() : "";
			String fullName = (firstName + " " + lastName).trim();
			if (fullName.isEmpty()) {
				log.warn("Użytkownik bez imienia i nazwiska, userId={}", userId);
				throw new IllegalArgumentException("Nie znaleziono użytkownika o id=" + userId);
			}
			return fullName;
		} catch (EmptyResultDataAccessException ex) {
			log.warn("Nie znaleziono użytkownika do raportu, userId={}", userId);
			throw new IllegalArgumentException("Nie znaleziono użytkownika o id=" + userId);
		}
	}

	private List<SessionData> loadSessions(Integer userId, String dateFrom, String dateTo) {
		return jdbcTemplate.query(
				"""
				SELECT ws.id, w.name AS workout_name,
				       ws.completed_date, ws.duration
				FROM workout_sessions ws
				LEFT JOIN workouts w ON w.id = ws.workout_id
				WHERE ws.user_id = ?
				  AND ws.status = 'UKOŃCZONE'
				  AND (CAST(? AS DATE) IS NULL OR ws.completed_date >= CAST(? AS TIMESTAMP))
				  AND (CAST(? AS DATE) IS NULL OR ws.completed_date <= CAST(? AS TIMESTAMP))
				ORDER BY ws.completed_date DESC
				""",
				(rs, rowNum) -> {
					int sessionId = rs.getInt("id");
					String workoutName = rs.getString("workout_name");
					String completedDate = formatCompletedDate(rs.getTimestamp("completed_date"));
					Integer duration = rs.getObject("duration") != null ? rs.getInt("duration") : null;
					List<ExerciseResultData> results = loadExerciseResults(sessionId);
					return new SessionData(workoutName, completedDate, duration, results);
				},
				userId,
				dateFrom,
				dateFrom,
				dateTo,
				dateTo
		);
	}

	private List<ExerciseResultData> loadExerciseResults(int sessionId) {
		return jdbcTemplate.query(
				"""
				SELECT e.name AS exercise_name,
				       er.sets_done, er.reps_done, er.weight_used, er.notes
				FROM exercise_results er
				JOIN exercises e ON e.id = er.exercise_id
				WHERE er.session_id = ?
				""",
				(rs, rowNum) -> new ExerciseResultData(
						rs.getString("exercise_name"),
						rs.getObject("sets_done") != null ? rs.getInt("sets_done") : null,
						rs.getObject("reps_done") != null ? rs.getInt("reps_done") : null,
						toDouble(rs.getBigDecimal("weight_used")),
						rs.getString("notes")
				),
				sessionId
		);
	}

	private String formatCompletedDate(Timestamp completedDate) {
		if (completedDate == null) {
			return "—";
		}
		return completedDate.toLocalDateTime().format(DATE_TIME_FORMAT);
	}

	private Double toDouble(BigDecimal value) {
		return value != null ? value.doubleValue() : null;
	}

	private String blankToNull(String value) {
		return isBlank(value) ? null : value.trim();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
