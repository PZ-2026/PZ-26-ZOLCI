package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FeatureItemDto(
    val id: Int? = null,
    val title: String,
    val subtitle: String,
)
