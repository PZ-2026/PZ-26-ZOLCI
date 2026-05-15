package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO żądania resetu hasła użytkownika.
 *
 * @param email adres e-mail konta
 * @param newPassword nowe hasło
 */
@Serializable
data class ForgotPasswordRequestDto(
    @SerialName("email")
    val email: String,
    @SerialName("newPassword")
    val newPassword: String,
)
