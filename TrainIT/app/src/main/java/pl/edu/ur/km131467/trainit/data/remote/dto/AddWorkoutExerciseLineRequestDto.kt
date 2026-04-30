package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddWorkoutExerciseLineRequestDto(
    val exerciseId: Int,
    val sets: Int? = null,
    val reps: Int? = null,
    val weight: Double? = null,
    val duration: Int? = null,
)
