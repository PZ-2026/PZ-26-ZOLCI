package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: ChangeRoleRequestDto.
 * @param role rola użytkownika
 */
@Serializable
data class ChangeRoleRequestDto(
    val role: String,
)