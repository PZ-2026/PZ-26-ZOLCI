package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateExerciseRequestDto(
    val createdBy: Int,
    val name: String,
    val muscleGroup: String? = null,
    val description: String? = null,
)
