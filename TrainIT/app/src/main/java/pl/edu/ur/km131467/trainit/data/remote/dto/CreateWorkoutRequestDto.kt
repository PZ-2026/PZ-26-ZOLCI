package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO żądania utworzenia planu treningowego.
 *
 * @param userId identyfikator właściciela planu
 * @param name nazwa planu
 * @param description opcjonalny opis
 * @param difficultyLevel poziom trudności (np. ŁATWY, ŚREDNI, TRUDNY)
 * @param estimatedDuration szacowany czas trwania w minutach
 */
@Serializable
data class CreateWorkoutRequestDto(
    val userId: Int,
    val name: String,
    val description: String? = null,
    val difficultyLevel: String? = null,
    val estimatedDuration: Int? = null,
)
