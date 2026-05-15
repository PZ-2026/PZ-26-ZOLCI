package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: StartSessionRequestDto.
 * @param userId identyfikator użytkownika
 * @param workoutId identyfikator planu treningowego
 */
@Serializable
data class StartSessionRequestDto(
    val userId: Int,
    val workoutId: Int,
)