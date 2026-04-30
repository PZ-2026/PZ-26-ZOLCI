package pl.edu.ur.km131467.trainit.data.remote.dto

import kotlinx.serialization.Serializable

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
)

@Serializable
data class ProfileRecordDto(
    val exercise: String,
    val weight: String,
    val date: String,
    val reps: String,
)

@Serializable
data class ProfileAchievementDto(
    val key: String,
    val label: String,
    val unlocked: Boolean,
)
