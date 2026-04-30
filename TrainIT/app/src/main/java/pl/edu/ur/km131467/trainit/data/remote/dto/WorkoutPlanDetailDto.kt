package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutPlanDetailDto(
    val id: Int,
    val name: String,
    val description: String? = null,
    val difficultyLevel: String? = null,
    val estimatedDuration: Int? = null,
)
