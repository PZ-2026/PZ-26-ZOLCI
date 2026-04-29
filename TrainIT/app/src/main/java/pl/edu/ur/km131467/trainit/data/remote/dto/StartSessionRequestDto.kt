package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class StartSessionRequestDto(
    val userId: Int,
    val workoutId: Int,
)
