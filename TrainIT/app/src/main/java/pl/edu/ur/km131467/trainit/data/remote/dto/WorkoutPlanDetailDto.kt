package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: WorkoutPlanDetailDto.
 * @param id identyfikator rekordu
 * @param name nazwa
 * @param description opis
 * @param difficultyLevel poziom trudności
 * @param estimatedDuration szacowany czas trwania w minutach
 */
/**
 * Model DTO: WorkoutPlanDetailDto.
 * @param id identyfikator rekordu
 * @param name nazwa
 * @param description opis
 * @param difficultyLevel poziom trudności
 * @param estimatedDuration szacowany czas trwania w minutach
 */
@Serializable
data class WorkoutPlanDetailDto(
    val id: Int,
    val name: String,
    val description: String? = null,
    val difficultyLevel: String? = null,
    val estimatedDuration: Int? = null,
)