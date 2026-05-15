package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: UpdateProfileRequestDto.
 * @param firstName imię
 * @param lastName nazwisko
 * @param email adres e-mail
 * @param newPassword nowe hasło
 */
@Serializable
data class UpdateProfileRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val newPassword: String? = null,
)