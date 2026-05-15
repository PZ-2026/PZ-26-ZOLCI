package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: ErrorResponseDto.
 * @param message komunikat błędu
 * @param errors lista błędów walidacji
 */
@Serializable
data class ErrorResponseDto(
    val message: String = "",
    val errors: List<FieldErrorDto> = emptyList(),
)
