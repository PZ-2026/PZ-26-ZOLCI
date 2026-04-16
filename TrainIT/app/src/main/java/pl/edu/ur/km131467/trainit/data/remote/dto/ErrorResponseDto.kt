package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
    val message: String = "",
    val errors: List<FieldErrorDto> = emptyList(),
)
