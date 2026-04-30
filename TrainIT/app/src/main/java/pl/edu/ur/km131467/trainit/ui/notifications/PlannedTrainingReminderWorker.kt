package pl.edu.ur.km131467.trainit.ui.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.repository.FeatureRepository
import pl.edu.ur.km131467.trainit.ui.feature.FeatureModule
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PlannedTrainingReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val sessionManager = SessionManager(context)
    private val featureRepository = FeatureRepository()
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    override suspend fun doWork(): Result {
        if (!sessionManager.isLoggedIn()) return Result.success()
        return runCatching {
            val notifications = featureRepository.getItems(FeatureModule.NOTIFICATIONS, sessionManager)
            val settings = featureRepository.getItems(FeatureModule.SETTINGS, sessionManager)
            val remindersEnabled = settings
                .firstOrNull { it.title.equals("Przypomnienia treningowe", ignoreCase = true) }
                ?.subtitle
                ?.contains("włącz", ignoreCase = true)
                ?: true
            if (!remindersEnabled) return Result.success()
            val nearestSession = notifications.firstOrNull {
                it.id != null && it.title.equals("Najbliższy trening", ignoreCase = true)
            }
            val sessionId = nearestSession?.id
            val plannedAt = nearestSession?.subtitle
                ?.let { runCatching { LocalDateTime.parse(it, dateTimeFormatter) }.getOrNull() }
            if (sessionId != null && plannedAt != null) {
                val minutesUntil = Duration.between(LocalDateTime.now(), plannedAt).toMinutes()
                if (minutesUntil in 0..60 && sessionManager.shouldNotifyPlannedSession(sessionId)) {
                    val message = if (minutesUntil <= 1) {
                        "Trening zaczyna się teraz. Powodzenia!"
                    } else {
                        "Trening zaczyna się za $minutesUntil min."
                    }
                    TrainingReminderNotifier.showReminder(
                        applicationContext,
                        "Nadchodzi trening",
                        message,
                    )
                    sessionManager.markPlannedSessionNotified(sessionId)
                }
            }
            val plannedSessions = notifications
                .firstOrNull { it.title.equals("Sesje zaplanowane", ignoreCase = true) }
                ?.subtitle
                ?.toIntOrNull()
                ?: 0
            if (plannedSessions > 0 && nearestSession == null) {
                TrainingReminderNotifier.showReminder(
                    applicationContext,
                    "TrainIT",
                    "Przypomnienie: masz $plannedSessions zaplanowane sesje treningowe.",
                )
            }
            Result.success()
        }.getOrElse { Result.retry() }
    }
}
