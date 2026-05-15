package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: RegisterRequestDto.
 * @param email adres e-mail
 * @param password hasło
 * @param firstName imię
 * @param lastName nazwisko
 */
@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
)