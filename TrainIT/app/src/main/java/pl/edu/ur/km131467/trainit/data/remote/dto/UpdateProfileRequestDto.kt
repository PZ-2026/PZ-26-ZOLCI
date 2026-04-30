package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val newPassword: String? = null,
)
