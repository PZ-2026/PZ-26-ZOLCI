package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: AddSessionExerciseResultRequestDto.
 * @param exerciseId identyfikator ćwiczenia
 * @param setsDone wykonane serie
 * @param repsDone wykonane powtórzenia
 * @param weightUsed użyty ciężar
 * @param duration czas trwania w minutach
 * @param notes notatki
 */
/**
 * Model DTO: AddSessionExerciseResultRequestDto.
 * @param exerciseId identyfikator ćwiczenia
 * @param setsDone wykonane serie
 * @param repsDone wykonane powtórzenia
 * @param weightUsed użyty ciężar
 * @param duration czas trwania w minutach
 * @param notes notatki
 */
@Serializable
data class AddSessionExerciseResultRequestDto(
    val exerciseId: Int,
    val setsDone: Int? = null,
    val repsDone: Int? = null,
    val weightUsed: Double? = null,
    val duration: Int? = null,
    val notes: String? = null,
)