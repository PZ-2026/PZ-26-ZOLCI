package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddSessionExerciseResultRequestDto(
    val exerciseId: Int,
    val setsDone: Int? = null,
    val repsDone: Int? = null,
    val weightUsed: Double? = null,
    val duration: Int? = null,
    val notes: String? = null,
)
