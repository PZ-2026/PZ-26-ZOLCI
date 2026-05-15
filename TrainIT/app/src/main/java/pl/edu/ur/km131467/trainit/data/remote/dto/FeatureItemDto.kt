package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: FeatureItemDto.
 * @param id identyfikator rekordu
 * @param title tytuł pozycji
 * @param subtitle podtytuł pozycji
 */
@Serializable
data class FeatureItemDto(
    val id: Int? = null,
    val title: String,
    val subtitle: String,
)