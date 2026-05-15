package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Model DTO: ProfileOverviewDto.
 * @param profileName nazwa wyświetlana profilu
 * @param memberSinceText member since text
 * @param workoutsText liczba treningów (tekst)
 * @param totalHoursText łączny czas (tekst)
 * @param streakText seria dni (tekst)
 * @param weeklyHours godziny treningu w tygodniu
 * @param personalRecords rekordy osobiste
 * @param achievements osiągnięcia
 * @param summaryItems pozycje podsumowania
 */
/**
 * Model DTO: ProfileOverviewDto.
 * @param profileName nazwa wyświetlana profilu
 * @param memberSinceText member since text
 * @param workoutsText liczba treningów (tekst)
 * @param totalHoursText łączny czas (tekst)
 * @param streakText seria dni (tekst)
 * @param weeklyHours godziny treningu w tygodniu
 * @param personalRecords rekordy osobiste
 * @param achievements osiągnięcia
 * @param summaryItems pozycje podsumowania
 */
@Serializable
data class ProfileOverviewDto(
    val profileName: String,
    val memberSinceText: String,
    val workoutsText: String,
    val totalHoursText: String,
    val streakText: String,
    val weeklyHours: List<Float>,
    val personalRecords: List<ProfileRecordDto>,
    val achievements: List<ProfileAchievementDto>,
    val summaryItems: List<FeatureItemDto>,
)/**
 * Model DTO: ProfileRecordDto.
 * @param exercise nazwa ćwiczenia rekordu
 * @param weight ciężar
 * @param date data rekordu
 * @param reps liczba powtórzeń
 */
/**
 * Model DTO: ProfileRecordDto.
 * @param exercise nazwa ćwiczenia rekordu
 * @param weight ciężar
 * @param date data rekordu
 * @param reps liczba powtórzeń
 */
@Serializable
data class ProfileRecordDto(
    val exercise: String,
    val weight: String,
    val date: String,
    val reps: String,
)/**
 * Model DTO: ProfileAchievementDto.
 * @param key klucz techniczny
 * @param label etykieta osiągnięcia
 * @param unlocked czy osiągnięcie odblokowane
 */
/**
 * Model DTO: ProfileAchievementDto.
 * @param key klucz techniczny
 * @param label etykieta osiągnięcia
 * @param unlocked czy osiągnięcie odblokowane
 */
@Serializable
data class ProfileAchievementDto(
    val key: String,
    val label: String,
    val unlocked: Boolean,
)