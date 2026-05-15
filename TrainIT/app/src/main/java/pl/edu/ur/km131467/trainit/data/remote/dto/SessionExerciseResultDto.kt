package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: SessionExerciseResultDto.
 * @param id identyfikator rekordu
 * @param exerciseId identyfikator ćwiczenia
 * @param exerciseName nazwa ćwiczenia
 * @param setsDone wykonane serie
 * @param repsDone wykonane powtórzenia
 * @param weightUsed użyty ciężar
 * @param duration czas trwania w minutach
 * @param notes notatki
 */
/**
 * Model DTO: SessionExerciseResultDto.
 * @param id identyfikator rekordu
 * @param exerciseId identyfikator ćwiczenia
 * @param exerciseName nazwa ćwiczenia
 * @param setsDone wykonane serie
 * @param repsDone wykonane powtórzenia
 * @param weightUsed użyty ciężar
 * @param duration czas trwania w minutach
 * @param notes notatki
 */
@Serializable
data class SessionExerciseResultDto(
    val id: Int,
    val exerciseId: Int,
    val exerciseName: String,
    val setsDone: Int? = null,
    val repsDone: Int? = null,
    val weightUsed: Double? = null,
    val duration: Int? = null,
    val notes: String? = null,
)