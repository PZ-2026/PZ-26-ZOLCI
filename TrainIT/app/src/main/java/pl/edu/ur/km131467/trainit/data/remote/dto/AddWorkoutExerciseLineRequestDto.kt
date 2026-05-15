package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: AddWorkoutExerciseLineRequestDto.
 * @param exerciseId identyfikator ćwiczenia
 * @param sets liczba serii
 * @param reps liczba powtórzeń
 * @param weight ciężar
 * @param duration czas trwania w minutach
 */
/**
 * Model DTO: AddWorkoutExerciseLineRequestDto.
 * @param exerciseId identyfikator ćwiczenia
 * @param sets liczba serii
 * @param reps liczba powtórzeń
 * @param weight ciężar
 * @param duration czas trwania w minutach
 */
@Serializable
data class AddWorkoutExerciseLineRequestDto(
    val exerciseId: Int,
    val sets: Int? = null,
    val reps: Int? = null,
    val weight: Double? = null,
    val duration: Int? = null,
)