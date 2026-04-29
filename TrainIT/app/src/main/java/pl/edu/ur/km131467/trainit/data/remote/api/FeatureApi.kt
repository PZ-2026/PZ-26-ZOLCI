package pl.edu.ur.km131467.trainit.data.remote.api

import pl.edu.ur.km131467.trainit.data.remote.dto.CreateExerciseRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.CreateWorkoutRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.FeatureItemDto
import pl.edu.ur.km131467.trainit.data.remote.dto.FinishSessionRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.StartSessionRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
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

    /** Pobiera konfigurację/podsumowanie powiadomień. */
    @GET("api/feature/notifications")
    suspend fun getNotifications(
        @Header("Authorization") authorization: String? = null,
        @Query("userId") userId: Int? = null,
    ): Response<List<FeatureItemDto>>

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
}
