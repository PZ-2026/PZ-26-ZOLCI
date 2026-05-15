package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: WorkoutExerciseLineDto.
 * @param id identyfikator rekordu
 * @param exerciseId identyfikator ćwiczenia
 * @param exerciseName nazwa ćwiczenia
 * @param sets liczba serii
 * @param reps liczba powtórzeń
 * @param weight ciężar
 * @param duration czas trwania w minutach
 */
/**
 * Model DTO: WorkoutExerciseLineDto.
 * @param id identyfikator rekordu
 * @param exerciseId identyfikator ćwiczenia
 * @param exerciseName nazwa ćwiczenia
 * @param sets liczba serii
 * @param reps liczba powtórzeń
 * @param weight ciężar
 * @param duration czas trwania w minutach
 */
@Serializable
data class WorkoutExerciseLineDto(
    val id: Int,
    val exerciseId: Int,
    val exerciseName: String,
    val sets: Int? = null,
    val reps: Int? = null,
    val weight: Double? = null,
    val duration: Int? = null,
)