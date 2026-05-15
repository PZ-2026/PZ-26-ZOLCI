package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: LoginResponseDto.
 * @param id identyfikator rekordu
 * @param email adres e-mail
 * @param firstName imię
 * @param lastName nazwisko
 * @param role rola użytkownika
 * @param token token JWT
 */
/**
 * Model DTO: LoginResponseDto.
 * @param id identyfikator rekordu
 * @param email adres e-mail
 * @param firstName imię
 * @param lastName nazwisko
 * @param role rola użytkownika
 * @param token token JWT
 */
@Serializable
data class LoginResponseDto(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val token: String,
)