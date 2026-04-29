package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateWorkoutRequestDto(
    val userId: Int,
    val name: String,
    val description: String? = null,
    val difficultyLevel: String? = null,
    val estimatedDuration: Int? = null,
)
