package pl.edu.ur.km131467.trainit.data.repository

import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.remote.NetworkModule
import pl.edu.ur.km131467.trainit.data.remote.api.FeatureApi
import pl.edu.ur.km131467.trainit.data.remote.dto.CreateExerciseRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.CreateWorkoutRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.FinishSessionRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.StartSessionRequestDto
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
                    "Uruchomiono nową sesję"
                }
            }
            FeatureModule.REPORTS -> "Raporty są odczytem danych (bez akcji zapisu)"
            FeatureModule.STATISTICS -> "Statystyki odświeżone"
            FeatureModule.SETTINGS -> "Ustawienia zapiszesz po wdrożeniu formularza"
            FeatureModule.NOTIFICATIONS -> "Powiadomienia skonfigurowane"
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
