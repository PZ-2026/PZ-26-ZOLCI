package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionExerciseResultDto(
    val id: Int,
    val exerciseId: Int,
    val exerciseName: String,
    val setsDone: Int? = null,
    val repsDone: Int? = null,
    val weightUsed: Double? = null,
    val duration: Int? = null,
    val notes: String? = null,
)
