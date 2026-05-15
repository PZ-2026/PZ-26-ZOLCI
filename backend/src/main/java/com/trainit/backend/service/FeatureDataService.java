package com.trainit.backend.service;

import com.trainit.backend.util.AppLog;

import com.trainit.backend.dto.FeatureItemResponse;
import com.trainit.backend.dto.CreateExerciseRequest;
import com.trainit.backend.dto.CreateWorkoutRequest;
import com.trainit.backend.dto.ExerciseResultRequest;
import com.trainit.backend.dto.ProfileAchievementResponse;
import com.trainit.backend.dto.ProfileOverviewResponse;
import com.trainit.backend.dto.ProfileRecordResponse;
import com.trainit.backend.dto.SessionExerciseResultResponse;
import com.trainit.backend.dto.UpdateSettingRequest;
import com.trainit.backend.dto.WorkoutExerciseRequest;
import com.trainit.backend.dto.WorkoutExerciseLineResponse;
import com.trainit.backend.dto.WorkoutPlanDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Serwis dostarczający dane listowe dla modułowych ekranów aplikacji mobilnej.
 *
 * <p>Źródłem danych jest relacyjna baza PostgreSQL, zgodna ze skryptami
 * {@code db_init.sql} oraz {@code db_seed.sql}. Serwis zwraca uproszczony model
 * {@link FeatureItemResponse} wykorzystywany bezpośrednio przez frontend.
 */
@Service
public class FeatureDataService {

	private static final Logger log = LoggerFactory.getLogger(FeatureDataService.class);

	/** Narzędzie JDBC do wykonywania zapytań SQL bezpośrednio na bazie. */
	private final JdbcTemplate jdbcTemplate;

	/**
	 * Tworzy serwis z wstrzykniętym szablonem JDBC.
	 *
	 * @param jdbcTemplate szablon JDBC do odczytu danych
	 */
	public FeatureDataService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		ensureUserSettingsTable();
	}

	/**
	 * Zwraca listę planów treningowych.
	 *
	 * @param userId identyfikator użytkownika lub null (wszystkie plany)
	 * @return lista pozycji modułu treningów
	 */
	public List<FeatureItemResponse> getWorkouts(Integer userId) {
		List<FeatureItemResponse> result;
		if (userId == null) {
			result = jdbcTemplate.query(
					"""
					SELECT w.id AS id,
					       w.name AS title,
					       ('Poziom: ' || COALESCE(w.difficulty_level, 'N/D') ||
					        ', czas: ' || COALESCE(w.estimated_duration::text, '?') || ' min') AS subtitle
					FROM workouts w
					ORDER BY w.id DESC
					""",
					(rs, rowNum) -> new FeatureItemResponse(
							rs.getInt("id"),
							rs.getString("title"),
							rs.getString("subtitle")
					)
			);
		} else {
			result = jdbcTemplate.query(
					"""
					SELECT w.id AS id,
					       w.name AS title,
					       ('Poziom: ' || COALESCE(w.difficulty_level, 'N/D') ||
					        ', czas: ' || COALESCE(w.estimated_duration::text, '?') || ' min') AS subtitle
					FROM workouts w
					WHERE w.user_id = ?
					ORDER BY w.id DESC
					""",
					(rs, rowNum) -> new FeatureItemResponse(
							rs.getInt("id"),
							rs.getString("title"),
							rs.getString("subtitle")
					),
					userId
			);
		}
		AppLog.success(log, "Pobrano plany treningowe (feature), userId={}, liczba={}", userId, result.size());
		return result;
	}

	/**
	 * Zwraca listę ćwiczeń.
	 *
	 * @param userId identyfikator użytkownika lub null (wszystkie ćwiczenia)
	 * @return lista pozycji modułu ćwiczeń
	 */
	public List<FeatureItemResponse> getExercises(Integer userId) {
		if (userId == null) {
			return jdbcTemplate.query(
					"""
					SELECT e.id AS id,
					       e.name AS title,
					       (COALESCE(e.muscle_group, 'Brak grupy') ||
					        CASE WHEN e.is_custom THEN ' (własne)' ELSE ' (systemowe)' END) AS subtitle
					FROM exercises e
					ORDER BY e.id DESC
					""",
					(rs, rowNum) -> new FeatureItemResponse(
							rs.getInt("id"),
							rs.getString("title"),
							rs.getString("subtitle")
					)
			);
		}
		return jdbcTemplate.query(
				"""
				SELECT e.id AS id,
				       e.name AS title,
				       (COALESCE(e.muscle_group, 'Brak grupy') ||
				        CASE WHEN e.is_custom THEN ' (własne)' ELSE ' (systemowe)' END) AS subtitle
				FROM exercises e
				WHERE e.is_custom = false OR e.created_by = ?
				ORDER BY e.id DESC
				""",
				(rs, rowNum) -> new FeatureItemResponse(
						rs.getInt("id"),
						rs.getString("title"),
						rs.getString("subtitle")
				),
				userId
		);
	}

	/**
	 * Zwraca listę sesji treningowych.
	 *
	 * @return lista pozycji modułu sesji
	 */
	public List<FeatureItemResponse> getSessions(Integer userId) {
		if (userId == null) {
			return jdbcTemplate.query(
					"""
					SELECT ws.id AS id,
					       ('Sesja #' || ws.id || ' - ' || COALESCE(w.name, 'Plan bez nazwy')) AS title,
					       ('Status: ' || COALESCE(ws.status, 'N/D') ||
					        ', czas: ' || COALESCE(ws.duration::text, '?') || ' min' ||
					        ', data: ' || COALESCE(to_char(COALESCE(ws.completed_date, ws.planned_date), 'YYYY-MM-DD HH24:MI'), 'N/D')) AS subtitle
					FROM workout_sessions ws
					LEFT JOIN workouts w ON w.id = ws.workout_id
					ORDER BY ws.id DESC
					""",
					(rs, rowNum) -> new FeatureItemResponse(
							rs.getInt("id"),
							rs.getString("title"),
							rs.getString("subtitle")
					)
			);
		}
		return jdbcTemplate.query(
				"""
				SELECT ws.id AS id,
				       ('Sesja #' || ws.id || ' - ' || COALESCE(w.name, 'Plan bez nazwy')) AS title,
				       ('Status: ' || COALESCE(ws.status, 'N/D') ||
				        ', czas: ' || COALESCE(ws.duration::text, '?') || ' min' ||
				        ', data: ' || COALESCE(to_char(COALESCE(ws.completed_date, ws.planned_date), 'YYYY-MM-DD HH24:MI'), 'N/D')) AS subtitle
				FROM workout_sessions ws
				LEFT JOIN workouts w ON w.id = ws.workout_id
				WHERE ws.user_id = ?
				ORDER BY ws.id DESC
				""",
				(rs, rowNum) -> new FeatureItemResponse(
						rs.getInt("id"),
						rs.getString("title"),
						rs.getString("subtitle")
				),
				userId
		);
	}

	/**
	 * Zwraca szczegółowe wyniki ćwiczeń zapisane dla sesji treningowej użytkownika.
	 */
	public List<SessionExerciseResultResponse> getSessionExerciseResults(Integer authenticatedUserId, Integer sessionId) {
		validateSessionOwnedByUser(authenticatedUserId, sessionId);
		return jdbcTemplate.query(
				"""
				SELECT er.id AS id,
				       e.id AS exercise_id,
				       e.name AS exercise_name,
				       er.sets_done,
				       er.reps_done,
				       er.weight_used,
				       er.duration,
				       er.notes
				FROM exercise_results er
				JOIN exercises e ON e.id = er.exercise_id
				WHERE er.session_id = ?
				ORDER BY er.id
				""",
				(rs, rowNum) -> mapSessionExerciseResultRow(rs),
				sessionId
		);
	}

	/**
	 * Dodaje wynik ćwiczenia do wskazanej sesji treningowej użytkownika.
	 */
	@Transactional
	public SessionExerciseResultResponse addSessionExerciseResult(
			Integer authenticatedUserId,
			Integer sessionId,
			ExerciseResultRequest request
	) {
		validateSessionOwnedByUser(authenticatedUserId, sessionId);
		validateExerciseUsableByUser(authenticatedUserId, request.getExerciseId());
		validateExerciseResultRequest(request);
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(
					"""
					INSERT INTO exercise_results (session_id, exercise_id, sets_done, reps_done, weight_used, duration, notes)
					VALUES (?, ?, ?, ?, ?, ?, ?)
					""",
					Statement.RETURN_GENERATED_KEYS
			);
			ps.setInt(1, sessionId);
			ps.setInt(2, request.getExerciseId());
			if (request.getSetsDone() == null) ps.setObject(3, null); else ps.setInt(3, request.getSetsDone());
			if (request.getRepsDone() == null) ps.setObject(4, null); else ps.setInt(4, request.getRepsDone());
			if (request.getWeightUsed() == null) ps.setObject(5, null); else ps.setBigDecimal(5, request.getWeightUsed());
			if (request.getDuration() == null) ps.setObject(6, null); else ps.setInt(6, request.getDuration());
			ps.setString(7, request.getNotes());
			return ps;
		}, keyHolder);
		Integer resultId = extractGeneratedId(keyHolder);
		if (resultId == null) {
			log.error("Nie udało się odczytać id zapisanego wyniku ćwiczenia, sessionId={}", sessionId);
			throw new IllegalStateException("Nie udało się odczytać id zapisanego wyniku");
		}
		SessionExerciseResultResponse response = jdbcTemplate.queryForObject(
				"""
				SELECT er.id AS id,
				       e.id AS exercise_id,
				       e.name AS exercise_name,
				       er.sets_done,
				       er.reps_done,
				       er.weight_used,
				       er.duration,
				       er.notes
				FROM exercise_results er
				JOIN exercises e ON e.id = er.exercise_id
				WHERE er.id = ?
				""",
				(rs, rowNum) -> mapSessionExerciseResultRow(rs),
				resultId
		);
		AppLog.success(log, "Dodano wynik ćwiczenia do sesji, userId={}, sessionId={}, resultId={}",
				authenticatedUserId, sessionId, resultId);
		return response;
	}

	/**
	 * Aktualizuje istniejący wynik ćwiczenia w sesji użytkownika.
	 *
	 * @param authenticatedUserId identyfikator użytkownika
	 * @param sessionId identyfikator sesji
	 * @param resultId identyfikator wyniku
	 * @param request nowe dane wyniku
	 * @return zaktualizowany wynik
	 * @throws IllegalArgumentException gdy sesja, ćwiczenie lub wynik nie istnieje
	 */
	@Transactional
	public SessionExerciseResultResponse updateSessionExerciseResult(
			Integer authenticatedUserId,
			Integer sessionId,
			Integer resultId,
			ExerciseResultRequest request
	) {
		validateSessionOwnedByUser(authenticatedUserId, sessionId);
		validateExerciseUsableByUser(authenticatedUserId, request.getExerciseId());
		validateExerciseResultRequest(request);
		int updated = jdbcTemplate.update(
				"""
				UPDATE exercise_results er
				SET exercise_id = ?, sets_done = ?, reps_done = ?, weight_used = ?, duration = ?, notes = ?
				WHERE er.id = ? AND er.session_id = ?
				""",
				request.getExerciseId(),
				request.getSetsDone(),
				request.getRepsDone(),
				request.getWeightUsed(),
				request.getDuration(),
				request.getNotes(),
				resultId,
				sessionId
		);
		if (updated == 0) {
			log.warn("Nie znaleziono wyniku ćwiczenia do aktualizacji, resultId={}, sessionId={}", resultId, sessionId);
			throw new IllegalArgumentException("Nie znaleziono wyniku ćwiczenia o id=" + resultId);
		}
		SessionExerciseResultResponse response = jdbcTemplate.queryForObject(
				"""
				SELECT er.id AS id,
				       e.id AS exercise_id,
				       e.name AS exercise_name,
				       er.sets_done,
				       er.reps_done,
				       er.weight_used,
				       er.duration,
				       er.notes
				FROM exercise_results er
				JOIN exercises e ON e.id = er.exercise_id
				WHERE er.id = ? AND er.session_id = ?
				""",
				(rs, rowNum) -> mapSessionExerciseResultRow(rs),
				resultId,
				sessionId
		);
		AppLog.success(log, "Zaktualizowano wynik ćwiczenia w sesji, userId={}, sessionId={}, resultId={}",
				authenticatedUserId, sessionId, resultId);
		return response;
	}

	/**
	 * Usuwa wynik ćwiczenia z sesji użytkownika.
	 *
	 * @param authenticatedUserId identyfikator użytkownika
	 * @param sessionId identyfikator sesji
	 * @param resultId identyfikator wyniku
	 * @throws IllegalArgumentException gdy sesja lub wynik nie istnieje
	 */
	@Transactional
	public void deleteSessionExerciseResult(Integer authenticatedUserId, Integer sessionId, Integer resultId) {
		validateSessionOwnedByUser(authenticatedUserId, sessionId);
		int deleted = jdbcTemplate.update(
				"DELETE FROM exercise_results WHERE id = ? AND session_id = ?",
				resultId,
				sessionId
		);
		if (deleted == 0) {
			log.warn("Nie znaleziono wyniku ćwiczenia do usunięcia, resultId={}, sessionId={}", resultId, sessionId);
			throw new IllegalArgumentException("Nie znaleziono wyniku ćwiczenia o id=" + resultId);
		}
		AppLog.success(log, "Usunięto wynik ćwiczenia z sesji, userId={}, sessionId={}, resultId={}",
				authenticatedUserId, sessionId, resultId);
	}

	/**
	 * Zwraca listę pozycji statystycznych.
	 *
	 * @return lista statystyk do modułu podsumowań
	 */
	public List<FeatureItemResponse> getStatisticsSummary(Integer userId) {
		List<FeatureItemResponse> result = new ArrayList<>();
		Integer workouts = userId == null
				? jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workouts", Integer.class)
				: jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workouts WHERE user_id = ?", Integer.class, userId);
		Integer sessions = userId == null
				? jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sessions WHERE status = 'UKOŃCZONE'", Integer.class)
				: jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM workout_sessions WHERE user_id = ? AND status = 'UKOŃCZONE'",
				Integer.class,
				userId
		);
		Integer weekSessions = userId == null
				? jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*) FROM workout_sessions
				WHERE status = 'UKOŃCZONE'
				  AND COALESCE(completed_date, planned_date) >= date_trunc('week', now())
				""",
				Integer.class
		)
				: jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*) FROM workout_sessions
				WHERE user_id = ?
				  AND status = 'UKOŃCZONE'
				  AND COALESCE(completed_date, planned_date) >= date_trunc('week', now())
				""",
				Integer.class,
				userId
		);
		Integer totalMinutes = userId == null
				? jdbcTemplate.queryForObject(
				"SELECT COALESCE(SUM(duration), 0) FROM workout_sessions WHERE status = 'UKOŃCZONE'",
				Integer.class
		)
				: jdbcTemplate.queryForObject(
				"SELECT COALESCE(SUM(duration), 0) FROM workout_sessions WHERE user_id = ? AND status = 'UKOŃCZONE'",
				Integer.class,
				userId
		);
		Integer exercises = userId == null
				? jdbcTemplate.queryForObject("SELECT COUNT(*) FROM exercises", Integer.class)
				: jdbcTemplate.queryForObject("SELECT COUNT(*) FROM exercises WHERE is_custom = false OR created_by = ?", Integer.class, userId);
		int safeSessions = defaultInt(sessions);
		int safeTotalMinutes = defaultInt(totalMinutes);
		int averageMinutes = safeSessions == 0 ? 0 : safeTotalMinutes / safeSessions;
		Double currentMonthAvgWeight = userId == null
				? jdbcTemplate.queryForObject(
				"""
				SELECT COALESCE(AVG(er.weight_used), 0)
				FROM exercise_results er
				JOIN workout_sessions ws ON ws.id = er.session_id
				WHERE ws.status = 'UKOŃCZONE'
				  AND er.weight_used IS NOT NULL
				  AND COALESCE(ws.completed_date, ws.planned_date) >= date_trunc('month', now())
				""",
				Double.class
		)
				: jdbcTemplate.queryForObject(
				"""
				SELECT COALESCE(AVG(er.weight_used), 0)
				FROM exercise_results er
				JOIN workout_sessions ws ON ws.id = er.session_id
				WHERE ws.user_id = ?
				  AND ws.status = 'UKOŃCZONE'
				  AND er.weight_used IS NOT NULL
				  AND COALESCE(ws.completed_date, ws.planned_date) >= date_trunc('month', now())
				""",
				Double.class,
				userId
		);
		Double previousMonthAvgWeight = userId == null
				? jdbcTemplate.queryForObject(
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
		)
				: jdbcTemplate.queryForObject(
				"""
				SELECT COALESCE(AVG(er.weight_used), 0)
				FROM exercise_results er
				JOIN workout_sessions ws ON ws.id = er.session_id
				WHERE ws.user_id = ?
				  AND ws.status = 'UKOŃCZONE'
				  AND er.weight_used IS NOT NULL
				  AND COALESCE(ws.completed_date, ws.planned_date) >= date_trunc('month', now() - interval '1 month')
				  AND COALESCE(ws.completed_date, ws.planned_date) < date_trunc('month', now())
				""",
				Double.class,
				userId
		);
		int currentWeight = currentMonthAvgWeight == null ? 0 : (int) Math.round(currentMonthAvgWeight);
		int previousWeight = previousMonthAvgWeight == null ? 0 : (int) Math.round(previousMonthAvgWeight);
		int loadProgressPercent = previousWeight == 0
				? (currentWeight > 0 ? 100 : 0)
				: ((currentWeight - previousWeight) * 100) / previousWeight;

		result.add(new FeatureItemResponse("Liczba planów treningowych", String.valueOf(defaultInt(workouts))));
		result.add(new FeatureItemResponse("Sesje ukończone", String.valueOf(safeSessions)));
		result.add(new FeatureItemResponse("Sesje w tym tygodniu", String.valueOf(defaultInt(weekSessions))));
		result.add(new FeatureItemResponse("Łączny czas treningów (min)", String.valueOf(safeTotalMinutes)));
		result.add(new FeatureItemResponse("Średni czas sesji (min)", String.valueOf(averageMinutes)));
		result.add(new FeatureItemResponse("Analiza progresu obciążenia (%)", String.valueOf(loadProgressPercent)));
		result.add(new FeatureItemResponse("Liczba ćwiczeń", String.valueOf(defaultInt(exercises))));
		return result;
	}

	/**
	 * Zwraca listę raportów.
	 *
	 * @return lista raportów użytkowników
	 */
	public List<FeatureItemResponse> getReports(Integer userId) {
		if (userId == null) {
			return jdbcTemplate.query(
					"""
					SELECT r.id AS id,
					       ('Raport: ' || COALESCE(r.type, 'N/D')) AS title,
					       ('Zakres: ' || COALESCE(r.date_from::text, '?') || ' - ' || COALESCE(r.date_to::text, '?')) AS subtitle
					FROM reports r
					ORDER BY r.id DESC
					""",
					(rs, rowNum) -> new FeatureItemResponse(
							rs.getInt("id"),
							rs.getString("title"),
							rs.getString("subtitle")
					)
			);
		}
		return jdbcTemplate.query(
				"""
				SELECT r.id AS id,
				       ('Raport: ' || COALESCE(r.type, 'N/D')) AS title,
				       ('Zakres: ' || COALESCE(r.date_from::text, '?') || ' - ' || COALESCE(r.date_to::text, '?')) AS subtitle
				FROM reports r
				WHERE r.user_id = ?
				ORDER BY r.id DESC
				""",
				(rs, rowNum) -> new FeatureItemResponse(
						rs.getInt("id"),
						rs.getString("title"),
						rs.getString("subtitle")
				),
				userId
		);
	}

	/**
	 * Zwraca dane sekcji ustawień (fallback biznesowy, bo brak dedykowanej tabeli ustawień).
	 *
	 * @return lista pozycji modułu ustawień
	 */
	public List<FeatureItemResponse> getSettings(Integer userId) {
		if (userId == null) {
			return List.of(
					new FeatureItemResponse(1, "Jednostki", "kg"),
					new FeatureItemResponse(2, "Przypomnienia treningowe", "włączone"),
					new FeatureItemResponse(3, "Tryb prywatny", "wyłączony"),
					new FeatureItemResponse(4, "Cel tygodniowy", "5")
			);
		}
		validateUserExists(userId);
		Map<Integer, String> defaults = defaultSettingsById();
		for (Map.Entry<Integer, String> entry : defaults.entrySet()) {
			String key = settingKeyById(entry.getKey());
			jdbcTemplate.update(
					"""
					INSERT INTO user_settings (user_id, setting_key, setting_value, updated_at)
					VALUES (?, ?, ?, now())
					ON CONFLICT (user_id, setting_key) DO NOTHING
					""",
					userId,
					key,
					entry.getValue()
			);
		}
		return jdbcTemplate.query(
				"""
				SELECT setting_key, setting_value
				FROM user_settings
				WHERE user_id = ?
				""",
				(rs, rowNum) -> {
					String key = rs.getString("setting_key");
					String value = rs.getString("setting_value");
					int id = settingIdByKey(key);
					String title = settingTitleById(id);
					return new FeatureItemResponse(id, title, value);
				},
				userId
		);
	}

	/**
	 * Aktualizuje pojedyncze ustawienie użytkownika.
	 *
	 * @param authenticatedUserId identyfikator użytkownika
	 * @param settingId identyfikator ustawienia
	 * @param request nowa wartość ustawienia
	 * @return zaktualizowana pozycja ustawienia
	 * @throws IllegalArgumentException gdy użytkownik nie istnieje lub wartość jest pusta
	 */
	@Transactional
	public FeatureItemResponse updateSetting(Integer authenticatedUserId, Integer settingId, UpdateSettingRequest request) {
		validateUserExists(authenticatedUserId);
		String value = request.getValue() == null ? "" : request.getValue().trim();
		if (value.isBlank()) {
			log.warn("Pusta wartość ustawienia, userId={}, settingId={}", authenticatedUserId, settingId);
			throw new IllegalArgumentException("Wartość ustawienia nie może być pusta");
		}
		String key = settingKeyById(settingId);
		jdbcTemplate.update(
				"""
				INSERT INTO user_settings (user_id, setting_key, setting_value, updated_at)
				VALUES (?, ?, ?, now())
				ON CONFLICT (user_id, setting_key)
				DO UPDATE SET setting_value = EXCLUDED.setting_value, updated_at = now()
				""",
				authenticatedUserId,
				key,
				value
		);
		AppLog.success(log, "Zaktualizowano ustawienie, userId={}, settingId={}", authenticatedUserId, settingId);
		return new FeatureItemResponse(settingId, settingTitleById(settingId), value);
	}

	/**
	 * Zwraca dane sekcji powiadomień (fallback biznesowy, bo brak dedykowanej tabeli powiadomień).
	 *
	 * @return lista pozycji modułu powiadomień
	 */
	public List<FeatureItemResponse> getNotifications(Integer userId) {
		Integer plannedSessions = userId == null
				? jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM workout_sessions WHERE status = 'ZAPLANOWANE'",
				Integer.class
		)
				: jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM workout_sessions WHERE status = 'ZAPLANOWANE' AND user_id = ?",
				Integer.class,
				userId
		);
		String remindersSetting = "włączone";
		if (userId != null) {
			remindersSetting = jdbcTemplate.query(
					"""
					SELECT setting_value FROM user_settings
					WHERE user_id = ? AND setting_key = 'training_reminders'
					""",
					rs -> rs.next() ? rs.getString(1) : "włączone",
					userId
			);
		}
		FeatureItemResponse nearestTrainingItem = new FeatureItemResponse("Najbliższy trening", "Brak zaplanowanych");
		if (userId != null) {
			nearestTrainingItem = jdbcTemplate.query(
					"""
					SELECT id, to_char(planned_date, 'YYYY-MM-DD HH24:MI') AS planned_time
					FROM workout_sessions
					WHERE user_id = ?
					  AND status = 'ZAPLANOWANE'
					  AND planned_date IS NOT NULL
					ORDER BY planned_date ASC
					LIMIT 1
					""",
					rs -> {
						if (!rs.next()) {
							return new FeatureItemResponse("Najbliższy trening", "Brak zaplanowanych");
						}
						return new FeatureItemResponse(
								rs.getInt("id"),
								"Najbliższy trening",
								rs.getString("planned_time")
						);
					},
					userId
			);
		}
		return List.of(
				new FeatureItemResponse(2, "Przypomnienia treningowe", remindersSetting),
				new FeatureItemResponse("Sesje zaplanowane", String.valueOf(defaultInt(plannedSessions))),
				nearestTrainingItem,
				new FeatureItemResponse("Powiadomienia systemowe", defaultInt(plannedSessions) > 0 ? "Aktywne" : "Brak zaplanowanych")
		);
	}

	/**
	 * Zwraca pełne dane ekranu profilu użytkownika, wyliczone z danych PostgreSQL.
	 */
	public ProfileOverviewResponse getProfileOverview(Integer userId) {
		validateUserExists(userId);
		String profileName = jdbcTemplate.queryForObject(
				"""
				SELECT trim(concat(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))) || ' • ' || COALESCE(r.name, 'USER')
				FROM users u
				LEFT JOIN roles r ON r.id = u.role_id
				WHERE u.id = ?
				""",
				String.class,
				userId
		);
		String memberSince = jdbcTemplate.queryForObject(
				"SELECT to_char(created_at, 'DD.MM.YYYY') FROM users WHERE id = ?",
				String.class,
				userId
		);
		Integer workoutsCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM workout_sessions WHERE user_id = ? AND status = 'UKOŃCZONE'",
				Integer.class,
				userId
		);
		Integer totalMinutes = jdbcTemplate.queryForObject(
				"SELECT COALESCE(SUM(duration), 0) FROM workout_sessions WHERE user_id = ? AND status = 'UKOŃCZONE'",
				Integer.class,
				userId
		);
		int totalHours = defaultInt(totalMinutes) / 60;

		List<Float> weeklyHours = buildWeeklyHours(userId);
		int streak = calculateStreakDays(userId);
		List<ProfileRecordResponse> records = getTopPersonalRecords(userId);
		List<ProfileAchievementResponse> achievements = buildAchievements(defaultInt(workoutsCount), streak);
		List<FeatureItemResponse> summaryItems = List.of(
				new FeatureItemResponse("Raport", "Dane treningowe z PostgreSQL"),
				new FeatureItemResponse("Przypomnienia", settingValue(userId, "training_reminders", "włączone")),
				new FeatureItemResponse("Tryb prywatny", settingValue(userId, "privacy_mode", "wyłączony"))
		);

		ProfileOverviewResponse overview = new ProfileOverviewResponse(
				profileName == null || profileName.isBlank() ? "Użytkownik • USER" : profileName.trim(),
				memberSince == null ? "Konto aktywne" : "Członek od " + memberSince,
				String.valueOf(defaultInt(workoutsCount)),
				totalHours + "h",
				String.valueOf(streak),
				weeklyHours,
				records,
				achievements,
				summaryItems
		);
		AppLog.success(log, "Pobrano podsumowanie profilu, userId={}", userId);
		return overview;
	}

	/**
	 * Normalizuje wartość liczbową pobraną z bazy.
	 *
	 * @param value wartość z bazy, potencjalnie {@code null}
	 * @return wartość liczby lub 0
	 */
	private int defaultInt(Integer value) {
		return value == null ? 0 : value;
	}

	/**
	 * Tworzy nowy plan treningowy użytkownika.
	 *
	 * @param request dane planu treningowego
	 * @return utworzona pozycja w formacie listowym
	 */
	@Transactional
	public FeatureItemResponse createWorkout(Integer authenticatedUserId, CreateWorkoutRequest request) {
		validateUserExists(authenticatedUserId);
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(
					"""
					INSERT INTO workouts (user_id, name, description, difficulty_level, estimated_duration, created_at)
					VALUES (?, ?, ?, ?, ?, ?)
					""",
					Statement.RETURN_GENERATED_KEYS
			);
			ps.setInt(1, authenticatedUserId);
			ps.setString(2, request.getName().trim());
			ps.setString(3, request.getDescription());
			ps.setString(4, request.getDifficultyLevel());
			if (request.getEstimatedDuration() == null) {
				ps.setObject(5, null);
			} else {
				ps.setInt(5, request.getEstimatedDuration());
			}
			ps.setObject(6, LocalDateTime.now());
			return ps;
		}, keyHolder);

		Integer id = extractGeneratedId(keyHolder);
		AppLog.success(log, "Utworzono plan treningowy (feature), userId={}, workoutId={}", authenticatedUserId, id);
		return new FeatureItemResponse(
				id,
				"Plan #" + (id == null ? "N/A" : id),
				"Utworzono: " + request.getName().trim()
		);
	}

	/**
	 * Zwraca szczegóły planu treningowego użytkownika (np. do formularza edycji).
	 *
	 * @param authenticatedUserId identyfikator użytkownika z tokena
	 * @param workoutId identyfikator planu
	 * @return dane planu
	 */
	public WorkoutPlanDetailResponse getWorkoutDetail(Integer authenticatedUserId, Integer workoutId) {
		validateUserOwnsWorkout(authenticatedUserId, workoutId);
		return jdbcTemplate.query(
				"""
				SELECT id, name, description, difficulty_level, estimated_duration
				FROM workouts
				WHERE id = ? AND user_id = ?
				""",
				rs -> {
					if (!rs.next()) {
						throw new IllegalArgumentException("Nie znaleziono planu o id=" + workoutId);
					}
					return new WorkoutPlanDetailResponse(
							rs.getInt("id"),
							rs.getString("name"),
							rs.getString("description"),
							rs.getString("difficulty_level"),
							rs.getObject("estimated_duration") == null ? null : rs.getInt("estimated_duration")
					);
				},
				workoutId,
				authenticatedUserId
		);
	}

	/**
	 * Aktualizuje istniejący plan treningowy użytkownika.
	 *
	 * @param authenticatedUserId identyfikator użytkownika z tokena
	 * @param workoutId identyfikator planu
	 * @param request nowe dane planu
	 * @return pozycja w formacie listowym (jak przy tworzeniu)
	 */
	@Transactional
	public FeatureItemResponse updateWorkout(Integer authenticatedUserId, Integer workoutId, CreateWorkoutRequest request) {
		validateUserOwnsWorkout(authenticatedUserId, workoutId);
		String name = request.getName() == null ? "" : request.getName().trim();
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Nazwa planu nie może być pusta");
		}
		int updated = jdbcTemplate.update(
				"""
				UPDATE workouts
				SET name = ?, description = ?, difficulty_level = ?, estimated_duration = ?
				WHERE id = ? AND user_id = ?
				""",
				name,
				request.getDescription(),
				request.getDifficultyLevel(),
				request.getEstimatedDuration(),
				workoutId,
				authenticatedUserId
		);
		if (updated == 0) {
			log.warn("Nie udało się zaktualizować planu, workoutId={}, userId={}", workoutId, authenticatedUserId);
			throw new IllegalArgumentException("Nie udało się zaktualizować planu o id=" + workoutId);
		}
		AppLog.success(log, "Zaktualizowano plan treningowy (feature), userId={}, workoutId={}", authenticatedUserId, workoutId);
		return new FeatureItemResponse(
				workoutId,
				name,
				"Zaktualizowano: " + name
		);
	}

	/**
	 * Zwraca listę ćwiczeń przypiętych do planu (z parametrami serii / powtórzeń itd.).
	 */
	public List<WorkoutExerciseLineResponse> listWorkoutExerciseLines(Integer authenticatedUserId, Integer workoutId) {
		validateUserOwnsWorkout(authenticatedUserId, workoutId);
		return jdbcTemplate.query(
				"""
				SELECT we.id AS line_id,
				       e.id AS exercise_id,
				       e.name AS exercise_name,
				       we.sets,
				       we.reps,
				       we.weight,
				       we.duration
				FROM workout_exercises we
				JOIN exercises e ON e.id = we.exercise_id
				JOIN workouts w ON w.id = we.workout_id
				WHERE we.workout_id = ? AND w.user_id = ?
				ORDER BY we.id
				""",
				(rs, rowNum) -> mapWorkoutExerciseLineRow(rs),
				workoutId,
				authenticatedUserId
		);
	}

	/**
	 * Dodaje ćwiczenie do planu użytkownika z parametrami treningowymi.
	 */
	@Transactional
	public WorkoutExerciseLineResponse addWorkoutExerciseLine(
			Integer authenticatedUserId,
			Integer workoutId,
			WorkoutExerciseRequest request
	) {
		validateUserOwnsWorkout(authenticatedUserId, workoutId);
		validateExerciseUsableByUser(authenticatedUserId, request.getExerciseId());
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(
					"""
					INSERT INTO workout_exercises (workout_id, exercise_id, sets, reps, weight, duration)
					VALUES (?, ?, ?, ?, ?, ?)
					""",
					Statement.RETURN_GENERATED_KEYS
			);
			ps.setInt(1, workoutId);
			ps.setInt(2, request.getExerciseId());
			if (request.getSets() == null) {
				ps.setObject(3, null);
			} else {
				ps.setInt(3, request.getSets());
			}
			if (request.getReps() == null) {
				ps.setObject(4, null);
			} else {
				ps.setInt(4, request.getReps());
			}
			if (request.getWeight() == null) {
				ps.setObject(5, null);
			} else {
				ps.setBigDecimal(5, request.getWeight());
			}
			if (request.getDuration() == null) {
				ps.setObject(6, null);
			} else {
				ps.setInt(6, request.getDuration());
			}
			return ps;
		}, keyHolder);
		Integer newId = extractGeneratedId(keyHolder);
		if (newId == null) {
			throw new IllegalStateException("Nie udało się odczytać id nowej pozycji planu");
		}
		return jdbcTemplate.queryForObject(
				"""
				SELECT we.id AS line_id,
				       e.id AS exercise_id,
				       e.name AS exercise_name,
				       we.sets,
				       we.reps,
				       we.weight,
				       we.duration
				FROM workout_exercises we
				JOIN exercises e ON e.id = we.exercise_id
				WHERE we.id = ?
				""",
				(rs, rowNum) -> mapWorkoutExerciseLineRow(rs),
				newId
		);
	}

	/**
	 * Usuwa pojedynczą pozycję ćwiczenia z planu.
	 */
	@Transactional
	public void deleteWorkoutExerciseLine(Integer authenticatedUserId, Integer workoutId, Integer lineId) {
		validateUserOwnsWorkout(authenticatedUserId, workoutId);
		int deleted = jdbcTemplate.update(
				"DELETE FROM workout_exercises WHERE id = ? AND workout_id = ?",
				lineId,
				workoutId
		);
		if (deleted == 0) {
			throw new IllegalArgumentException("Nie znaleziono pozycji planu o id=" + lineId);
		}
	}

	private WorkoutExerciseLineResponse mapWorkoutExerciseLineRow(ResultSet rs) throws SQLException {
		Double weight = null;
		if (rs.getObject("weight") != null) {
			weight = rs.getBigDecimal("weight").doubleValue();
		}
		return new WorkoutExerciseLineResponse(
				rs.getInt("line_id"),
				rs.getInt("exercise_id"),
				rs.getString("exercise_name"),
				rs.getObject("sets") == null ? null : rs.getInt("sets"),
				rs.getObject("reps") == null ? null : rs.getInt("reps"),
				weight,
				rs.getObject("duration") == null ? null : rs.getInt("duration")
		);
	}

	private SessionExerciseResultResponse mapSessionExerciseResultRow(ResultSet rs) throws SQLException {
		Double weightUsed = null;
		if (rs.getObject("weight_used") != null) {
			weightUsed = rs.getBigDecimal("weight_used").doubleValue();
		}
		return new SessionExerciseResultResponse(
				rs.getInt("id"),
				rs.getInt("exercise_id"),
				rs.getString("exercise_name"),
				rs.getObject("sets_done") == null ? null : rs.getInt("sets_done"),
				rs.getObject("reps_done") == null ? null : rs.getInt("reps_done"),
				weightUsed,
				rs.getObject("duration") == null ? null : rs.getInt("duration"),
				rs.getString("notes")
		);
	}

	private void validateExerciseUsableByUser(Integer userId, Integer exerciseId) {
		Integer count = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*) FROM exercises e
				WHERE e.id = ? AND (e.is_custom = false OR e.created_by = ?)
				""",
				Integer.class,
				exerciseId,
				userId
		);
		if (defaultInt(count) == 0) {
			log.warn("Ćwiczenie niedostępne dla użytkownika, userId={}, exerciseId={}", userId, exerciseId);
			throw new IllegalArgumentException("Ćwiczenie nie istnieje lub nie jest dostępne dla użytkownika");
		}
	}

	private void validateSessionOwnedByUser(Integer userId, Integer sessionId) {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM workout_sessions WHERE id = ? AND user_id = ?",
				Integer.class,
				sessionId,
				userId
		);
		if (defaultInt(count) == 0) {
			log.warn("Sesja nie należy do użytkownika, userId={}, sessionId={}", userId, sessionId);
			throw new IllegalArgumentException("Sesja o id=" + sessionId + " nie należy do użytkownika");
		}
	}

	private void validateExerciseResultRequest(ExerciseResultRequest request) {
		if (request.getSetsDone() != null && request.getSetsDone() < 0) {
			throw new IllegalArgumentException("Liczba serii nie może być ujemna");
		}
		if (request.getRepsDone() != null && request.getRepsDone() < 0) {
			throw new IllegalArgumentException("Liczba powtórzeń nie może być ujemna");
		}
		if (request.getDuration() != null && request.getDuration() < 0) {
			throw new IllegalArgumentException("Czas ćwiczenia nie może być ujemny");
		}
		if (request.getWeightUsed() != null && request.getWeightUsed().signum() < 0) {
			throw new IllegalArgumentException("Ciężar nie może być ujemny");
		}
	}

	private void ensureUserSettingsTable() {
		jdbcTemplate.execute(
				"""
				CREATE TABLE IF NOT EXISTS user_settings (
					user_id INTEGER NOT NULL,
					setting_key VARCHAR(100) NOT NULL,
					setting_value TEXT NOT NULL,
					updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
					PRIMARY KEY (user_id, setting_key),
					CONSTRAINT fk_user_settings_user
						FOREIGN KEY (user_id) REFERENCES users(id)
				)
				"""
		);
	}

	private String settingValue(Integer userId, String key, String fallback) {
		return jdbcTemplate.query(
				"SELECT setting_value FROM user_settings WHERE user_id = ? AND setting_key = ?",
				rs -> rs.next() ? rs.getString(1) : fallback,
				userId,
				key
		);
	}

	private List<Float> buildWeeklyHours(Integer userId) {
		Float[] hoursByDay = new Float[] {0f, 0f, 0f, 0f, 0f, 0f, 0f};
		jdbcTemplate.query(
				"""
				SELECT EXTRACT(ISODOW FROM COALESCE(completed_date, planned_date))::int AS dow,
				       COALESCE(SUM(duration), 0) AS minutes
				FROM workout_sessions
				WHERE user_id = ?
				  AND status = 'UKOŃCZONE'
				  AND COALESCE(completed_date, planned_date) >= date_trunc('week', now())
				GROUP BY dow
				""",
				rs -> {
					int dow = rs.getInt("dow");
					float minutes = rs.getFloat("minutes");
					int index = dow - 1;
					if (index >= 0 && index < hoursByDay.length) {
						hoursByDay[index] = minutes / 60f;
					}
				},
				userId
		);
		return Arrays.asList(hoursByDay);
	}

	private int calculateStreakDays(Integer userId) {
		List<java.sql.Date> dates = jdbcTemplate.query(
				"""
				SELECT DISTINCT date(COALESCE(completed_date, planned_date)) AS d
				FROM workout_sessions
				WHERE user_id = ? AND status = 'UKOŃCZONE'
				ORDER BY d DESC
				""",
				(rs, rowNum) -> rs.getDate("d"),
				userId
		);
		if (dates.isEmpty()) return 0;
		java.time.LocalDate cursor = java.time.LocalDate.now();
		java.util.Set<java.time.LocalDate> set = new java.util.HashSet<>();
		for (java.sql.Date d : dates) {
			if (d != null) set.add(d.toLocalDate());
		}
		if (!set.contains(cursor)) {
			cursor = cursor.minusDays(1);
		}
		int streak = 0;
		while (set.contains(cursor)) {
			streak++;
			cursor = cursor.minusDays(1);
		}
		return streak;
	}

	private List<ProfileRecordResponse> getTopPersonalRecords(Integer userId) {
		return jdbcTemplate.query(
				"""
				SELECT e.name AS exercise_name,
				       er.weight_used,
				       er.reps_done,
				       to_char(COALESCE(ws.completed_date, ws.planned_date), 'DD.MM.YYYY') AS result_date
				FROM exercise_results er
				JOIN workout_sessions ws ON ws.id = er.session_id
				JOIN exercises e ON e.id = er.exercise_id
				WHERE ws.user_id = ?
				ORDER BY er.weight_used DESC NULLS LAST, er.reps_done DESC NULLS LAST
				LIMIT 2
				""",
				(rs, rowNum) -> {
					String weight = rs.getObject("weight_used") == null
							? "-"
							: rs.getBigDecimal("weight_used").stripTrailingZeros().toPlainString() + " kg";
					String reps = rs.getObject("reps_done") == null
							? "-"
							: rs.getInt("reps_done") + " powtórzeń";
					return new ProfileRecordResponse(
							rs.getString("exercise_name"),
							weight,
							rs.getString("result_date"),
							reps
					);
				},
				userId
		);
	}

	private List<ProfileAchievementResponse> buildAchievements(int workoutsCount, int streak) {
		return List.of(
				new ProfileAchievementResponse("fire", "Seria 7 dni", streak >= 7),
				new ProfileAchievementResponse("muscle", "50\ntreningów", workoutsCount >= 50),
				new ProfileAchievementResponse("trophy", "Mistrz Push\nDay", workoutsCount >= 20),
				new ProfileAchievementResponse("target", "100\ntreningów", workoutsCount >= 100),
				new ProfileAchievementResponse("star", "Seria 30 dni", streak >= 30),
				new ProfileAchievementResponse("muscle", "Wszystkie\nmięśnie", workoutsCount >= 80)
		);
	}

	private Map<Integer, String> defaultSettingsById() {
		Map<Integer, String> map = new LinkedHashMap<>();
		map.put(1, "kg");
		map.put(2, "włączone");
		map.put(3, "wyłączony");
		map.put(4, "5");
		return map;
	}

	private String settingTitleById(Integer settingId) {
		return switch (settingId) {
			case 1 -> "Jednostki";
			case 2 -> "Przypomnienia treningowe";
			case 3 -> "Tryb prywatny";
			case 4 -> "Cel tygodniowy";
			default -> throw new IllegalArgumentException("Nieznane ustawienie id=" + settingId);
		};
	}

	private String settingKeyById(Integer settingId) {
		return switch (settingId) {
			case 1 -> "units";
			case 2 -> "training_reminders";
			case 3 -> "privacy_mode";
			case 4 -> "weekly_goal";
			default -> throw new IllegalArgumentException("Nieznane ustawienie id=" + settingId);
		};
	}

	private int settingIdByKey(String key) {
		return switch (key) {
			case "units" -> 1;
			case "training_reminders" -> 2;
			case "privacy_mode" -> 3;
			case "weekly_goal" -> 4;
			default -> throw new IllegalArgumentException("Nieznany klucz ustawienia: " + key);
		};
	}

	/**
	 * Usuwa plan treningowy użytkownika.
	 *
	 * @param workoutId identyfikator planu
	 */
	@Transactional
	public void deleteWorkout(Integer authenticatedUserId, Integer workoutId) {
		validateUserOwnsWorkout(authenticatedUserId, workoutId);

		jdbcTemplate.update(
				"""
				DELETE FROM exercise_results
				WHERE session_id IN (
					SELECT id FROM workout_sessions WHERE workout_id = ? AND user_id = ?
				)
				""",
				workoutId,
				authenticatedUserId
		);

		jdbcTemplate.update(
				"DELETE FROM workout_sessions WHERE workout_id = ? AND user_id = ?",
				workoutId,
				authenticatedUserId
		);

		jdbcTemplate.update("DELETE FROM workout_exercises WHERE workout_id = ?", workoutId);

		int deleted = jdbcTemplate.update("DELETE FROM workouts WHERE id = ? AND user_id = ?", workoutId, authenticatedUserId);
		if (deleted == 0) {
			log.warn("Nie można usunąć planu treningowego, workoutId={}, userId={}", workoutId, authenticatedUserId);
			throw new IllegalArgumentException("Nie można usunąć planu treningowego o id=" + workoutId);
		}
		AppLog.success(log, "Usunięto plan treningowy (feature), userId={}, workoutId={}", authenticatedUserId, workoutId);
	}

	/**
	 * Tworzy własne ćwiczenie użytkownika.
	 *
	 * @param request dane ćwiczenia
	 * @return utworzona pozycja w formacie listowym
	 */
	@Transactional
	public FeatureItemResponse createExercise(Integer authenticatedUserId, CreateExerciseRequest request) {
		validateUserExists(authenticatedUserId);
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(
					"""
					INSERT INTO exercises (name, muscle_group, description, is_custom, created_by)
					VALUES (?, ?, ?, ?, ?)
					""",
					Statement.RETURN_GENERATED_KEYS
			);
			ps.setString(1, request.getName().trim());
			ps.setString(2, request.getMuscleGroup());
			ps.setString(3, request.getDescription());
			ps.setBoolean(4, true);
			ps.setInt(5, authenticatedUserId);
			return ps;
		}, keyHolder);
		Integer id = extractGeneratedId(keyHolder);
		AppLog.success(log, "Utworzono ćwiczenie (feature), userId={}, exerciseId={}", authenticatedUserId, id);
		return new FeatureItemResponse(
				id,
				"Ćwiczenie #" + (id == null ? "N/A" : id),
				"Dodano: " + request.getName().trim()
		);
	}

	/**
	 * Rozpoczyna nową sesję treningową na podstawie planu.
	 *
	 * @param userId identyfikator użytkownika
	 * @param workoutId identyfikator planu treningowego
	 * @return utworzona pozycja sesji
	 */
	@Transactional
	public FeatureItemResponse startSession(Integer authenticatedUserId, Integer workoutId) {
		validateUserExists(authenticatedUserId);
		validateWorkoutOwnedByUser(authenticatedUserId, workoutId);
		Integer activeSessions = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM workout_sessions WHERE user_id = ? AND status = 'ZAPLANOWANE'",
				Integer.class,
				authenticatedUserId
		);
		if (defaultInt(activeSessions) > 0) {
			log.warn("Próba rozpoczęcia drugiej aktywnej sesji, userId={}", authenticatedUserId);
			throw new IllegalStateException("Masz już aktywną sesję. Zakończ ją przed rozpoczęciem kolejnej.");
		}
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(
					"""
					INSERT INTO workout_sessions (user_id, workout_id, planned_date, completed_date, status, duration)
					VALUES (?, ?, ?, ?, ?, ?)
					""",
					Statement.RETURN_GENERATED_KEYS
			);
			ps.setInt(1, authenticatedUserId);
			ps.setInt(2, workoutId);
			ps.setObject(3, LocalDateTime.now());
			ps.setObject(4, null);
			ps.setString(5, "ZAPLANOWANE");
			ps.setObject(6, null);
			return ps;
		}, keyHolder);
		Integer id = extractGeneratedId(keyHolder);
		AppLog.success(log, "Rozpoczęto sesję treningową (feature), userId={}, sessionId={}, workoutId={}",
				authenticatedUserId, id, workoutId);
		return new FeatureItemResponse(
				id,
				"Sesja #" + (id == null ? "N/A" : id),
				"Status: ZAPLANOWANE"
		);
	}

	/**
	 * Kończy sesję treningową i zapisuje jej czas trwania.
	 *
	 * @param sessionId identyfikator sesji
	 * @param duration czas trwania w minutach
	 * @return zaktualizowana pozycja sesji
	 */
	@Transactional
	public FeatureItemResponse finishSession(Integer authenticatedUserId, Integer sessionId, Integer duration) {
		int updated = jdbcTemplate.update(
				"""
				UPDATE workout_sessions
				SET status = ?, completed_date = ?, duration = ?
				WHERE id = ? AND user_id = ?
				""",
				"UKOŃCZONE",
				LocalDateTime.now(),
				duration,
				sessionId,
				authenticatedUserId
		);
		if (updated == 0) {
			log.warn("Nie znaleziono sesji do zakończenia, sessionId={}, userId={}", sessionId, authenticatedUserId);
			throw new IllegalArgumentException("Nie znaleziono sesji użytkownika o id=" + sessionId);
		}
		AppLog.success(log, "Zakończono sesję treningową (feature), userId={}, sessionId={}, duration={}",
				authenticatedUserId, sessionId, duration);
		return new FeatureItemResponse(
				sessionId,
				"Sesja #" + sessionId,
				"Status: UKOŃCZONE, czas: " + duration + " min"
		);
	}

	/**
	 * Anuluje aktywną sesję użytkownika (status ZAPLANOWANE).
	 *
	 * @param authenticatedUserId identyfikator użytkownika
	 * @param sessionId identyfikator sesji
	 */
	@Transactional
	public void cancelSession(Integer authenticatedUserId, Integer sessionId) {
		jdbcTemplate.update("DELETE FROM exercise_results WHERE session_id = ?", sessionId);
		int deleted = jdbcTemplate.update(
				"DELETE FROM workout_sessions WHERE id = ? AND user_id = ? AND status = 'ZAPLANOWANE'",
				sessionId,
				authenticatedUserId
		);
		if (deleted == 0) {
			log.warn("Nie znaleziono aktywnej sesji do anulowania, sessionId={}, userId={}",
					sessionId, authenticatedUserId);
			throw new IllegalArgumentException("Nie znaleziono aktywnej sesji do anulowania");
		}
		AppLog.success(log, "Anulowano sesję treningową (feature), userId={}, sessionId={}", authenticatedUserId, sessionId);
	}

	/**
	 * Sprawdza istnienie użytkownika.
	 *
	 * @param userId identyfikator użytkownika
	 */
	private void validateUserExists(Integer userId) {
		Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, userId);
		if (defaultInt(count) == 0) {
			log.warn("Nie znaleziono użytkownika, userId={}", userId);
			throw new IllegalArgumentException("Nie znaleziono użytkownika o id=" + userId);
		}
	}

	/**
	 * Sprawdza istnienie planu treningowego.
	 *
	 * @param workoutId identyfikator planu
	 */
	private void validateWorkoutExists(Integer workoutId) {
		Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workouts WHERE id = ?", Integer.class, workoutId);
		if (defaultInt(count) == 0) {
			throw new IllegalArgumentException("Nie znaleziono planu o id=" + workoutId);
		}
	}

	/**
	 * Sprawdza czy plan treningowy należy do użytkownika.
	 *
	 * @param userId identyfikator użytkownika
	 * @param workoutId identyfikator planu
	 */
	private void validateWorkoutOwnedByUser(Integer userId, Integer workoutId) {
		validateWorkoutExists(workoutId);
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM workouts WHERE id = ? AND user_id = ?",
				Integer.class,
				workoutId,
				userId
		);
		if (defaultInt(count) == 0) {
			throw new IllegalArgumentException("Plan o id=" + workoutId + " nie należy do użytkownika id=" + userId);
		}
	}

	/**
	 * Weryfikuje własność planu treningowego przed operacją usunięcia.
	 *
	 * @param userId identyfikator użytkownika
	 * @param workoutId identyfikator planu
	 */
	private void validateUserOwnsWorkout(Integer userId, Integer workoutId) {
		validateWorkoutOwnedByUser(userId, workoutId);
	}

	/**
	 * Odczytuje wygenerowany identyfikator z obiektu {@link KeyHolder}.
	 *
	 * <p>Dla PostgreSQL sterownik może zwracać mapę wielu kolumn, dlatego najpierw
	 * pobierane jest pole {@code id} z {@link KeyHolder#getKeys()}.
	 *
	 * @param keyHolder nośnik wygenerowanych kluczy SQL
	 * @return identyfikator rekordu lub {@code null} gdy brak danych
	 */
	private Integer extractGeneratedId(KeyHolder keyHolder) {
		if (keyHolder.getKeys() != null) {
			Object rawId = keyHolder.getKeys().get("id");
			if (rawId instanceof Number number) {
				return number.intValue();
			}
		}
		if (keyHolder.getKey() != null) {
			return keyHolder.getKey().intValue();
		}
		return null;
	}
}
