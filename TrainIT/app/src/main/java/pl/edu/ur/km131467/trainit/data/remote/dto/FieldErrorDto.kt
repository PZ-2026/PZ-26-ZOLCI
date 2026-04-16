package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FieldErrorDto(
    val field: String,
    val message: String,
)
