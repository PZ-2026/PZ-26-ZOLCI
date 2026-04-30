package pl.edu.ur.km131467.trainit.data.remote.api

import pl.edu.ur.km131467.trainit.data.remote.dto.CreateExerciseRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.AddWorkoutExerciseLineRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.AddSessionExerciseResultRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.CreateWorkoutRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.FeatureItemDto
import pl.edu.ur.km131467.trainit.data.remote.dto.FinishSessionRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.SessionExerciseResultDto
import pl.edu.ur.km131467.trainit.data.remote.dto.StartSessionRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.UpdateSettingRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.ProfileOverviewDto
import pl.edu.ur.km131467.trainit.data.remote.dto.WorkoutExerciseLineDto
import pl.edu.ur.km131467.trainit.data.remote.dto.WorkoutPlanDetailDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Kontrakt endpointów używanych przez modułowe ekrany UI.
 *
 * Endpointy są mapowane 1:1 do sekcji aplikacji, a odpowiedzi są wykorzystywane
 * jako dane listowe w ekranach opartych o [pl.edu.ur.km131467.trainit.ui.feature.BaseFeatureActivity].
 */
interface FeatureApi {
    /** Pobiera listę planów treningowych dla modułu treningów/panelu roli. */
    @GET("api/feature/workouts")
    suspend fun getWorkouts(
        @Header("Authorization") authorization: String? = null,
        @Query("userId") userId: Int? = null,
    ): Response<List<FeatureItemDto>>

    /** Pobiera listę ćwiczeń. */
    @GET("api/feature/exercises")
    suspend fun getExercises(
        @Header("Authorization") authorization: String? = null,
        @Query("userId") userId: Int? = null,
    ): Response<List<FeatureItemDto>>

    /** Pobiera listę sesji treningowych. */
    @GET("api/feature/sessions")
    suspend fun getSessions(
        @Header("Authorization") authorization: String? = null,
        @Query("userId") userId: Int? = null,
    ): Response<List<FeatureItemDto>>

    /** Pobiera dane podsumowań statystycznych. */
    @GET("api/feature/statistics/summary")
    suspend fun getStatistics(
        @Header("Authorization") authorization: String? = null,
        @Query("userId") userId: Int? = null,
    ): Response<List<FeatureItemDto>>

    /** Pobiera listę raportów użytkownika. */
    @GET("api/feature/reports")
    suspend fun getReports(
        @Header("Authorization") authorization: String? = null,
        @Query("userId") userId: Int? = null,
    ): Response<List<FeatureItemDto>>

    /** Pobiera pozycje ustawień użytkownika. */
    @GET("api/feature/settings")
    suspend fun getSettings(
        @Header("Authorization") authorization: String? = null,
        @Query("userId") userId: Int? = null,
    ): Response<List<FeatureItemDto>>

    /** Zapisuje pojedyncze ustawienie użytkownika. */
    @PUT("api/feature/settings/{settingId}")
    suspend fun updateSetting(
        @Header("Authorization") authorization: String? = null,
        @Path("settingId") settingId: Int,
        @Body request: UpdateSettingRequestDto,
    ): Response<FeatureItemDto>

    /** Pobiera konfigurację/podsumowanie powiadomień. */
    @GET("api/feature/notifications")
    suspend fun getNotifications(
        @Header("Authorization") authorization: String? = null,
        @Query("userId") userId: Int? = null,
    ): Response<List<FeatureItemDto>>

    /** Pełny podgląd danych ekranu profilu. */
    @GET("api/feature/profile-overview")
    suspend fun getProfileOverview(
        @Header("Authorization") authorization: String? = null,
    ): Response<ProfileOverviewDto>

    /** Tworzy nowy plan treningowy użytkownika. */
    @POST("api/feature/workouts")
    suspend fun createWorkout(
        @Header("Authorization") authorization: String? = null,
        @Body request: CreateWorkoutRequestDto,
    ): Response<FeatureItemDto>

    /** Usuwa istniejący plan treningowy. */
    @DELETE("api/feature/workouts/{workoutId}")
    suspend fun deleteWorkout(
        @Header("Authorization") authorization: String? = null,
        @Path("workoutId") workoutId: Int,
    ): Response<Unit>

    /** Szczegóły planu (formularz edycji). */
    @GET("api/feature/workouts/{workoutId}")
    suspend fun getWorkoutDetail(
        @Header("Authorization") authorization: String? = null,
        @Path("workoutId") workoutId: Int,
    ): Response<WorkoutPlanDetailDto>

    /** Aktualizuje plan treningowy. */
    @PUT("api/feature/workouts/{workoutId}")
    suspend fun updateWorkout(
        @Header("Authorization") authorization: String? = null,
        @Path("workoutId") workoutId: Int,
        @Body request: CreateWorkoutRequestDto,
    ): Response<FeatureItemDto>

    /** Ćwiczenia przypięte do planu. */
    @GET("api/feature/workouts/{workoutId}/exercises")
    suspend fun getWorkoutExercises(
        @Header("Authorization") authorization: String? = null,
        @Path("workoutId") workoutId: Int,
    ): Response<List<WorkoutExerciseLineDto>>

    /** Dodaje ćwiczenie do planu. */
    @POST("api/feature/workouts/{workoutId}/exercises")
    suspend fun addWorkoutExercise(
        @Header("Authorization") authorization: String? = null,
        @Path("workoutId") workoutId: Int,
        @Body request: AddWorkoutExerciseLineRequestDto,
    ): Response<WorkoutExerciseLineDto>

    /** Usuwa pozycję ćwiczenia z planu. */
    @DELETE("api/feature/workouts/{workoutId}/exercises/{lineId}")
    suspend fun deleteWorkoutExercise(
        @Header("Authorization") authorization: String? = null,
        @Path("workoutId") workoutId: Int,
        @Path("lineId") lineId: Int,
    ): Response<Unit>

    /** Tworzy własne ćwiczenie użytkownika. */
    @POST("api/feature/exercises")
    suspend fun createExercise(
        @Header("Authorization") authorization: String? = null,
        @Body request: CreateExerciseRequestDto,
    ): Response<FeatureItemDto>

    /** Rozpoczyna nową sesję treningową. */
    @POST("api/feature/sessions/start")
    suspend fun startSession(
        @Header("Authorization") authorization: String? = null,
        @Body request: StartSessionRequestDto,
    ): Response<FeatureItemDto>

    /** Kończy sesję treningową i zapisuje czas. */
    @POST("api/feature/sessions/{sessionId}/finish")
    suspend fun finishSession(
        @Header("Authorization") authorization: String? = null,
        @Path("sessionId") sessionId: Int,
        @Body request: FinishSessionRequestDto,
    ): Response<FeatureItemDto>

    /** Anuluje aktywną (zaplanowaną) sesję treningową. */
    @DELETE("api/feature/sessions/{sessionId}")
    suspend fun cancelSession(
        @Header("Authorization") authorization: String? = null,
        @Path("sessionId") sessionId: Int,
    ): Response<Unit>

    /** Wyniki ćwiczeń zapisane w konkretnej sesji. */
    @GET("api/feature/sessions/{sessionId}/results")
    suspend fun getSessionResults(
        @Header("Authorization") authorization: String? = null,
        @Path("sessionId") sessionId: Int,
    ): Response<List<SessionExerciseResultDto>>

    /** Dodaje wynik ćwiczenia do sesji. */
    @POST("api/feature/sessions/{sessionId}/results")
    suspend fun addSessionResult(
        @Header("Authorization") authorization: String? = null,
        @Path("sessionId") sessionId: Int,
        @Body request: AddSessionExerciseResultRequestDto,
    ): Response<SessionExerciseResultDto>

    /** Aktualizuje wynik ćwiczenia w sesji. */
    @PUT("api/feature/sessions/{sessionId}/results/{resultId}")
    suspend fun updateSessionResult(
        @Header("Authorization") authorization: String? = null,
        @Path("sessionId") sessionId: Int,
        @Path("resultId") resultId: Int,
        @Body request: AddSessionExerciseResultRequestDto,
    ): Response<SessionExerciseResultDto>

    /** Usuwa wynik ćwiczenia z sesji. */
    @DELETE("api/feature/sessions/{sessionId}/results/{resultId}")
    suspend fun deleteSessionResult(
        @Header("Authorization") authorization: String? = null,
        @Path("sessionId") sessionId: Int,
        @Path("resultId") resultId: Int,
    ): Response<Unit>
}
