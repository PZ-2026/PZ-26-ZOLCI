package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FinishSessionRequestDto(
    val duration: Int,
)
