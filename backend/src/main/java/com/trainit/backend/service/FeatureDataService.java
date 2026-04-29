package com.trainit.backend.service;

import com.trainit.backend.dto.FeatureItemResponse;
import com.trainit.backend.dto.CreateExerciseRequest;
import com.trainit.backend.dto.CreateWorkoutRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Serwis dostarczający dane listowe dla modułowych ekranów aplikacji mobilnej.
 *
 * <p>Źródłem danych jest relacyjna baza PostgreSQL, zgodna ze skryptami
 * {@code db_init.sql} oraz {@code db_seed.sql}. Serwis zwraca uproszczony model
 * {@link FeatureItemResponse} wykorzystywany bezpośrednio przez frontend.
 */
@Service
public class FeatureDataService {

	/** Narzędzie JDBC do wykonywania zapytań SQL bezpośrednio na bazie. */
	private final JdbcTemplate jdbcTemplate;

	/**
	 * Tworzy serwis z wstrzykniętym szablonem JDBC.
	 *
	 * @param jdbcTemplate szablon JDBC do odczytu danych
	 */
	public FeatureDataService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Zwraca listę planów treningowych.
	 *
	 * @return lista pozycji modułu treningów
	 */
	public List<FeatureItemResponse> getWorkouts(Integer userId) {
		if (userId == null) {
			return jdbcTemplate.query(
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
		}
		return jdbcTemplate.query(
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

	/**
	 * Zwraca listę ćwiczeń.
	 *
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
					       ('Sesja #' || ws.id) AS title,
					       ('Status: ' || COALESCE(ws.status, 'N/D') ||
					        ', czas: ' || COALESCE(ws.duration::text, '?') || ' min') AS subtitle
					FROM workout_sessions ws
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
				       ('Sesja #' || ws.id) AS title,
				       ('Status: ' || COALESCE(ws.status, 'N/D') ||
				        ', czas: ' || COALESCE(ws.duration::text, '?') || ' min') AS subtitle
				FROM workout_sessions ws
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
				? jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sessions", Integer.class)
				: jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sessions WHERE user_id = ?", Integer.class, userId);
		Integer exercises = userId == null
				? jdbcTemplate.queryForObject("SELECT COUNT(*) FROM exercises", Integer.class)
				: jdbcTemplate.queryForObject("SELECT COUNT(*) FROM exercises WHERE is_custom = false OR created_by = ?", Integer.class, userId);

		result.add(new FeatureItemResponse("Liczba planów treningowych", String.valueOf(defaultInt(workouts))));
		result.add(new FeatureItemResponse("Liczba sesji treningowych", String.valueOf(defaultInt(sessions))));
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
		Integer users = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
		String role = userId == null
				? "N/D"
				: jdbcTemplate.queryForObject(
				"SELECT COALESCE(r.name, 'USER') FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.id = ?",
				String.class,
				userId
		);
		return List.of(
				new FeatureItemResponse("Profil", "Edycja danych konta i hasła"),
				new FeatureItemResponse("Jednostki", "Domyślnie: kg"),
				new FeatureItemResponse("Rola", role == null ? "USER" : role),
				new FeatureItemResponse("Użytkownicy w systemie", String.valueOf(defaultInt(users)))
		);
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
		return List.of(
				new FeatureItemResponse("Przypomnienia treningowe", "Aktywne dla zaplanowanych sesji"),
				new FeatureItemResponse("Sesje zaplanowane", String.valueOf(defaultInt(plannedSessions))),
				new FeatureItemResponse("Powiadomienia systemowe", "Włączone")
		);
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
		return new FeatureItemResponse(
				id,
				"Plan #" + (id == null ? "N/A" : id),
				"Utworzono: " + request.getName().trim()
		);
	}

	/**
	 * Usuwa plan treningowy użytkownika.
	 *
	 * @param workoutId identyfikator planu
	 */
	@Transactional
	public void deleteWorkout(Integer authenticatedUserId, Integer workoutId) {
		validateUserOwnsWorkout(authenticatedUserId, workoutId);
		int deleted = jdbcTemplate.update("DELETE FROM workouts WHERE id = ? AND user_id = ?", workoutId, authenticatedUserId);
		if (deleted == 0) {
			throw new IllegalArgumentException("Nie można usunąć planu treningowego o id=" + workoutId);
		}
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
			throw new IllegalArgumentException("Nie znaleziono sesji użytkownika o id=" + sessionId);
		}
		return new FeatureItemResponse(
				sessionId,
				"Sesja #" + sessionId,
				"Status: UKOŃCZONE, czas: " + duration + " min"
		);
	}

	/**
	 * Sprawdza istnienie użytkownika.
	 *
	 * @param userId identyfikator użytkownika
	 */
	private void validateUserExists(Integer userId) {
		Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, userId);
		if (defaultInt(count) == 0) {
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
