package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: FieldErrorDto.
 * @param field nazwa pola formularza
 * @param message komunikat błędu
 */
@Serializable
data class FieldErrorDto(
    val field: String,
    val message: String,
)