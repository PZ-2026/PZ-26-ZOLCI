package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: UserDto.
 * @param id identyfikator rekordu
 * @param email adres e-mail
 * @param firstName imię
 * @param lastName nazwisko
 * @param role rola użytkownika
 * @param isActive czy konto jest aktywne
 */
/**
 * Model DTO: UserDto.
 * @param id identyfikator rekordu
 * @param email adres e-mail
 * @param firstName imię
 * @param lastName nazwisko
 * @param role rola użytkownika
 * @param isActive czy konto jest aktywne
 */
@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val isActive: Boolean = true,
)