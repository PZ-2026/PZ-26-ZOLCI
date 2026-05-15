package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: LoginRequestDto.
 * @param email adres e-mail
 * @param password hasło
 */
@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)