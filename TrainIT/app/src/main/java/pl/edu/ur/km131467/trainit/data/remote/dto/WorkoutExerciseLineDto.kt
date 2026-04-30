package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutExerciseLineDto(
    val id: Int,
    val exerciseId: Int,
    val exerciseName: String,
    val sets: Int? = null,
    val reps: Int? = null,
    val weight: Double? = null,
    val duration: Int? = null,
)
