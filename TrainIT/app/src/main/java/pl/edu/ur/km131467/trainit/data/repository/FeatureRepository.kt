package pl.edu.ur.km131467.trainit.data.repository

import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.remote.NetworkModule
import pl.edu.ur.km131467.trainit.data.remote.api.FeatureApi
import pl.edu.ur.km131467.trainit.data.remote.dto.AddSessionExerciseResultRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.AddWorkoutExerciseLineRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.CreateExerciseRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.CreateWorkoutRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.FinishSessionRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.SessionExerciseResultDto
import pl.edu.ur.km131467.trainit.data.remote.dto.StartSessionRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.UpdateSettingRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.ProfileOverviewDto
import pl.edu.ur.km131467.trainit.ui.notifications.TrainingReminderNotifier
import pl.edu.ur.km131467.trainit.data.remote.dto.WorkoutExerciseLineDto
import pl.edu.ur.km131467.trainit.data.remote.dto.WorkoutPlanDetailDto
import pl.edu.ur.km131467.trainit.ui.feature.FeatureListItem
import pl.edu.ur.km131467.trainit.ui.feature.FeatureModule

/**
 * Repozytorium danych dla ekranów modułowych.
 *
 * Repozytorium odczytuje dane z backendu i nie maskuje błędów fallbackiem,
 * dzięki czemu UI może poprawnie wyświetlić stany loading/empty/error.
 */
class FeatureRepository(
    private val featureApi: FeatureApi = NetworkModule.featureApi,
) {
    /** Uruchamia sesję treningową dla konkretnego planu. */
    suspend fun startSessionForWorkout(sessionManager: SessionManager, workoutId: Int): FeatureListItem {
        val userId = sessionManager.getUserId() ?: throw IllegalArgumentException("Brak aktywnej sesji użytkownika")
        val authHeader = buildAuthHeader(sessionManager)
        val response = featureApi.startSession(authHeader, StartSessionRequestDto(userId, workoutId))
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Nie udało się uruchomić sesji treningowej")
        }
        val started = response.body()!!
        return FeatureListItem(started.id, started.title, started.subtitle)
    }

    /** Usuwa plan treningowy użytkownika. */
    suspend fun deleteWorkoutForUser(sessionManager: SessionManager, workoutId: Int) {
        val authHeader = buildAuthHeader(sessionManager)
        val response = featureApi.deleteWorkout(authHeader, workoutId)
        if (!response.isSuccessful) {
            throw IllegalStateException("Nie udało się usunąć planu treningowego")
        }
    }

    /** Anuluje aktywną sesję treningową i czyści lokalny timer. */
    suspend fun cancelActiveSession(sessionManager: SessionManager) {
        val userId = sessionManager.getUserId() ?: throw IllegalArgumentException("Brak aktywnej sesji użytkownika")
        val authHeader = buildAuthHeader(sessionManager)
        val sessionsResponse = featureApi.getSessions(authHeader, userId)
        if (!sessionsResponse.isSuccessful) {
            throw IllegalStateException("Nie udało się pobrać aktywnej sesji")
        }
        val activeSessionId = sessionsResponse.body()
            .orEmpty()
            .firstOrNull { it.id != null && it.subtitle.contains("ZAPLANOWANE", ignoreCase = true) }
            ?.id
            ?: throw IllegalStateException("Brak aktywnej sesji do przerwania")
        val cancelResponse = featureApi.cancelSession(authHeader, activeSessionId)
        if (!cancelResponse.isSuccessful) {
            throw IllegalStateException("Nie udało się przerwać aktywnej sesji")
        }
        sessionManager.clearActiveSession()
    }

    /** Zatrzymuje aktywną sesję treningową (oznacza jako ukończoną). */
    suspend fun stopActiveSession(sessionManager: SessionManager) {
        val userId = sessionManager.getUserId() ?: throw IllegalArgumentException("Brak aktywnej sesji użytkownika")
        val authHeader = buildAuthHeader(sessionManager)
        val sessionsResponse = featureApi.getSessions(authHeader, userId)
        if (!sessionsResponse.isSuccessful) {
            throw IllegalStateException("Nie udało się pobrać aktywnej sesji")
        }
        val activeSessionId = sessionsResponse.body()
            .orEmpty()
            .firstOrNull { it.id != null && it.subtitle.contains("ZAPLANOWANE", ignoreCase = true) }
            ?.id
            ?: throw IllegalStateException("Brak aktywnej sesji do zatrzymania")

        val startedAt = sessionManager.getActiveSessionStartedAt()
        val elapsedMinutes = startedAt
            // Zaokrąglamy w górę: 1:01 -> 2 min (bardziej intuicyjne dla użytkownika).
            ?.let { ((System.currentTimeMillis() - it + 59_999L) / 60_000L).toInt().coerceAtLeast(1) }
            ?: 1

        val finishResponse = featureApi.finishSession(
            authorization = authHeader,
            sessionId = activeSessionId,
            request = FinishSessionRequestDto(duration = elapsedMinutes),
        )
        if (!finishResponse.isSuccessful || finishResponse.body() == null) {
            throw IllegalStateException("Nie udało się zatrzymać aktywnej sesji")
        }
        sessionManager.clearActiveSession()
    }

    /**
     * Tworzy plan treningowy użytkownika z danymi z formularza UI.
     *
     * @return utworzony element listy planów
     */
    suspend fun createWorkoutForUser(
        sessionManager: SessionManager,
        name: String,
        description: String? = null,
        difficultyLevel: String = "ŚREDNI",
        estimatedDuration: Int = 60,
    ): FeatureListItem {
        val userId = sessionManager.getUserId() ?: throw IllegalArgumentException("Brak aktywnej sesji użytkownika")
        val authHeader = buildAuthHeader(sessionManager)
        val response = featureApi.createWorkout(
            authHeader,
            CreateWorkoutRequestDto(
                userId = userId,
                name = name,
                description = description,
                difficultyLevel = difficultyLevel,
                estimatedDuration = estimatedDuration,
            ),
        )
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Nie udało się utworzyć planu użytkownika")
        }
        val created = response.body()!!
        return FeatureListItem(created.id, created.title, created.subtitle)
    }

    /** Pobiera szczegóły planu (formularz edycji). */
    suspend fun getWorkoutPlanDetail(sessionManager: SessionManager, workoutId: Int): WorkoutPlanDetailDto {
        val authHeader = buildAuthHeader(sessionManager)
        val response = featureApi.getWorkoutDetail(authHeader, workoutId)
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Nie udało się pobrać planu treningowego")
        }
        return response.body()!!
    }

    /** Aktualizuje plan treningowy użytkownika. */
    suspend fun updateWorkoutForUser(
        sessionManager: SessionManager,
        workoutId: Int,
        name: String,
        description: String? = null,
        difficultyLevel: String = "ŚREDNI",
        estimatedDuration: Int = 60,
    ): FeatureListItem {
        val userId = sessionManager.getUserId() ?: throw IllegalArgumentException("Brak aktywnej sesji użytkownika")
        val authHeader = buildAuthHeader(sessionManager)
        val response = featureApi.updateWorkout(
            authHeader,
            workoutId,
            CreateWorkoutRequestDto(
                userId = userId,
                name = name,
                description = description,
                difficultyLevel = difficultyLevel,
                estimatedDuration = estimatedDuration,
            ),
        )
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Nie udało się zapisać zmian planu")
        }
        val updated = response.body()!!
        return FeatureListItem(updated.id, updated.title, updated.subtitle)
    }

    /** Lista ćwiczeń w planie (serie, powtórzenia itd.). */
    suspend fun getWorkoutExerciseLines(sessionManager: SessionManager, workoutId: Int): List<WorkoutExerciseLineDto> {
        val authHeader = buildAuthHeader(sessionManager)
        val response = featureApi.getWorkoutExercises(authHeader, workoutId)
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Nie udało się pobrać ćwiczeń planu")
        }
        return response.body()!!
    }

    /** Dodaje ćwiczenie do planu. */
    suspend fun addWorkoutExerciseLine(
        sessionManager: SessionManager,
        workoutId: Int,
        request: AddWorkoutExerciseLineRequestDto,
    ): WorkoutExerciseLineDto {
        val authHeader = buildAuthHeader(sessionManager)
        val response = featureApi.addWorkoutExercise(authHeader, workoutId, request)
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Nie udało się dodać ćwiczenia do planu")
        }
        return response.body()!!
    }

    /** Usuwa pozycję ćwiczenia z planu. */
    suspend fun deleteWorkoutExerciseLine(sessionManager: SessionManager, workoutId: Int, lineId: Int) {
        val authHeader = buildAuthHeader(sessionManager)
        val response = featureApi.deleteWorkoutExercise(authHeader, workoutId, lineId)
        if (!response.isSuccessful) {
            throw IllegalStateException("Nie udało się usunąć ćwiczenia z planu")
        }
    }

    /** Wyniki ćwiczeń zapisane dla sesji treningowej. */
    suspend fun getSessionResults(sessionManager: SessionManager, sessionId: Int): List<SessionExerciseResultDto> {
        val authHeader = buildAuthHeader(sessionManager)
        val response = featureApi.getSessionResults(authHeader, sessionId)
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Nie udało się pobrać wyników sesji")
        }
        return response.body()!!
    }

    /** Dodaje wynik ćwiczenia do sesji treningowej. */
    suspend fun addSessionResult(
        sessionManager: SessionManager,
        sessionId: Int,
        request: AddSessionExerciseResultRequestDto,
    ): SessionExerciseResultDto {
        val authHeader = buildAuthHeader(sessionManager)
        val response = featureApi.addSessionResult(authHeader, sessionId, request)
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Nie udało się dodać wyniku ćwiczenia")
        }
        return response.body()!!
    }

    /** Aktualizuje zapisany wynik ćwiczenia w sesji. */
    suspend fun updateSessionResult(
        sessionManager: SessionManager,
        sessionId: Int,
        resultId: Int,
        request: AddSessionExerciseResultRequestDto,
    ): SessionExerciseResultDto {
        val authHeader = buildAuthHeader(sessionManager)
        val response = featureApi.updateSessionResult(authHeader, sessionId, resultId, request)
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Nie udało się zaktualizować wyniku ćwiczenia")
        }
        return response.body()!!
    }

    /** Usuwa zapisany wynik ćwiczenia z sesji. */
    suspend fun deleteSessionResult(
        sessionManager: SessionManager,
        sessionId: Int,
        resultId: Int,
    ) {
        val authHeader = buildAuthHeader(sessionManager)
        val response = featureApi.deleteSessionResult(authHeader, sessionId, resultId)
        if (!response.isSuccessful) {
            throw IllegalStateException("Nie udało się usunąć wyniku ćwiczenia")
        }
    }

    /** Zapisuje pojedyncze ustawienie użytkownika. */
    suspend fun updateSettingForUser(
        sessionManager: SessionManager,
        settingId: Int,
        value: String,
    ): FeatureListItem {
        val authHeader = buildAuthHeader(sessionManager)
        val response = featureApi.updateSetting(
            authorization = authHeader,
            settingId = settingId,
            request = UpdateSettingRequestDto(value = value),
        )
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Nie udało się zapisać ustawienia")
        }
        val updated = response.body()!!
        return FeatureListItem(updated.id, updated.title, updated.subtitle)
    }

    suspend fun getProfileOverview(sessionManager: SessionManager): ProfileOverviewDto {
        val authHeader = buildAuthHeader(sessionManager)
        val response = featureApi.getProfileOverview(authHeader)
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Nie udało się pobrać danych profilu")
        }
        return response.body()!!
    }

    /**
     * Zwraca dane listowe dla wskazanego modułu.
     *
     * @param module moduł ekranu, dla którego pobierane są dane.
     * @param sessionManager źródło roli użytkownika, używane w module panelu roli.
     * @return lista pozycji do wyświetlenia.
     */
    suspend fun getItems(module: FeatureModule, sessionManager: SessionManager): List<FeatureListItem> {
        val authHeader = buildAuthHeader(sessionManager)
        val userId = sessionManager.getUserId()
        val remoteResponse = runCatching {
            when (module) {
                FeatureModule.EXERCISES -> featureApi.getExercises(authHeader, userId)
                FeatureModule.SESSIONS -> featureApi.getSessions(authHeader, userId)
                FeatureModule.REPORTS -> featureApi.getReports(authHeader, userId)
                FeatureModule.STATISTICS -> featureApi.getStatistics(authHeader, userId)
                FeatureModule.SETTINGS -> featureApi.getSettings(authHeader, userId)
                FeatureModule.NOTIFICATIONS -> featureApi.getNotifications(authHeader, userId)
                FeatureModule.ROLE_PANEL -> featureApi.getWorkouts(authHeader, userId)
            }
        }.getOrElse { throwable ->
            throw IllegalStateException("Błąd połączenia z serwerem: ${throwable.message}", throwable)
        }
        if (!remoteResponse.isSuccessful) {
            throw IllegalStateException("Serwer zwrócił błąd: HTTP ${remoteResponse.code()}")
        }
        return remoteResponse.body().orEmpty().map { FeatureListItem(it.id, it.title, it.subtitle) }
    }

    /**
     * Wykonuje domyślną akcję biznesową dla modułu wywoływaną przez przycisk główny.
     *
     * @return komunikat sukcesu do wyświetlenia użytkownikowi
     */
    suspend fun runPrimaryAction(module: FeatureModule, sessionManager: SessionManager): String {
        val userId = sessionManager.getUserId() ?: throw IllegalArgumentException("Brak aktywnej sesji użytkownika")
        val authHeader = buildAuthHeader(sessionManager)
        return when (module) {
            FeatureModule.EXERCISES -> {
                val request = CreateExerciseRequestDto(
                    createdBy = userId,
                    name = "Ćwiczenie użytkownika ${System.currentTimeMillis() % 10000}",
                    muscleGroup = "Ogólne",
                    description = "Dodane z aplikacji mobilnej",
                )
                val response = featureApi.createExercise(authHeader, request)
                if (!response.isSuccessful || response.body() == null) {
                    throw IllegalStateException("Nie udało się dodać ćwiczenia")
                }
                "Dodano nowe ćwiczenie"
            }
            FeatureModule.SESSIONS -> {
                val sessionsResponse = featureApi.getSessions(authHeader, userId)
                val plannedSession = sessionsResponse.body()
                    ?.firstOrNull { it.id != null && it.subtitle.contains("ZAPLANOWANE", ignoreCase = true) }
                if (plannedSession?.id != null) {
                    val finishResponse = featureApi.finishSession(
                        authorization = authHeader,
                        sessionId = plannedSession.id,
                        request = FinishSessionRequestDto(duration = 45),
                    )
                    if (!finishResponse.isSuccessful || finishResponse.body() == null) {
                        throw IllegalStateException("Nie udało się zakończyć sesji")
                    }
                    sessionManager.clearActiveSession()
                    "Zakończono zaplanowaną sesję"
                } else {
                    val workoutsResponse = featureApi.getWorkouts(authHeader, userId)
                    val workoutId = workoutsResponse.body()
                        ?.firstOrNull { it.id != null }
                        ?.id
                        ?: throw IllegalStateException("Brak planu treningowego do uruchomienia sesji")
                    val startResponse = featureApi.startSession(authHeader, StartSessionRequestDto(userId, workoutId))
                    if (!startResponse.isSuccessful || startResponse.body() == null) {
                        throw IllegalStateException("Nie udało się uruchomić sesji")
                    }
                    sessionManager.setActiveSessionStartedAt(System.currentTimeMillis())
                    "Uruchomiono nową sesję"
                }
            }
            FeatureModule.REPORTS -> "Raporty są odczytem danych (bez akcji zapisu)"
            FeatureModule.STATISTICS -> "Statystyki odświeżone"
            FeatureModule.SETTINGS -> "Kliknij pozycję na liście, aby zmienić ustawienie"
            FeatureModule.NOTIFICATIONS -> {
                val settings = featureApi.getSettings(authHeader, userId).body().orEmpty()
                val reminders = settings.firstOrNull { it.id == 2 || it.title.equals("Przypomnienia treningowe", true) }
                val currentlyEnabled = reminders?.subtitle?.contains("włącz", ignoreCase = true) == true
                val newValue = if (currentlyEnabled) "wyłączone" else "włączone"
                val updateResponse = featureApi.updateSetting(authHeader, 2, UpdateSettingRequestDto(newValue))
                if (!updateResponse.isSuccessful || updateResponse.body() == null) {
                    throw IllegalStateException("Nie udało się zapisać ustawienia powiadomień")
                }
                if (!currentlyEnabled) {
                    TrainingReminderNotifier.showReminder(
                        sessionManager.getAppContext(),
                        title = "TrainIT",
                        message = "Przypomnienia treningowe są włączone.",
                    )
                }
                if (currentlyEnabled) "Powiadomienia treningowe wyłączone" else "Powiadomienia treningowe włączone"
            }
            FeatureModule.ROLE_PANEL -> when (sessionManager.getRole().uppercase()) {
                "ADMIN" -> "Panel administratora aktywny"
                "TRAINER" -> "Panel trenera aktywny"
                else -> {
                    val workouts = featureApi.getWorkouts(authHeader, userId).body().orEmpty()
                    val lastWorkoutId = workouts.firstOrNull()?.id
                    if (lastWorkoutId != null) {
                        val deleteResponse = featureApi.deleteWorkout(authHeader, lastWorkoutId)
                        if (!deleteResponse.isSuccessful) {
                            throw IllegalStateException("Nie udało się usunąć planu użytkownika")
                        }
                        "Usunięto ostatni plan użytkownika"
                    } else {
                        val createResponse = featureApi.createWorkout(
                            authHeader,
                            CreateWorkoutRequestDto(
                                userId = userId,
                                name = "Plan użytkownika ${System.currentTimeMillis() % 10000}",
                                description = "Utworzony z panelu roli",
                                difficultyLevel = "ŚREDNI",
                                estimatedDuration = 60,
                            )
                        )
                        if (!createResponse.isSuccessful || createResponse.body() == null) {
                            throw IllegalStateException("Nie udało się utworzyć planu użytkownika")
                        }
                        "Utworzono nowy plan użytkownika"
                    }
                }
            }
        }
    }

    /**
     * Buduje nagłówek autoryzacji Bearer z tokena sesji.
     *
     * @param sessionManager menedżer sesji aktualnego użytkownika.
     * @return wartość nagłówka Authorization.
     */
    private fun buildAuthHeader(sessionManager: SessionManager): String {
        val token = sessionManager.getToken()
            ?: throw IllegalArgumentException("Brak tokena sesji użytkownika")
        return "Bearer $token"
    }
}
