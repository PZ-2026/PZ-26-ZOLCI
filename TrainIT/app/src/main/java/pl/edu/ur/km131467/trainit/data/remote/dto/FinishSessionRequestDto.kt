package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: FinishSessionRequestDto.
 * @param duration czas trwania w minutach
 */
@Serializable
data class FinishSessionRequestDto(
    val duration: Int,
)