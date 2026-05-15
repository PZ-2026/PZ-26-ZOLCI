package pl.edu.ur.km131467.trainit.ui.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Planuje cykliczne sprawdzanie zaplanowanych treningów (WorkManager).
 */
object ReminderScheduler {
    private const val WORK_NAME = "trainit_planned_training_reminder"

    fun ensureScheduled(context: Context) {
        val request = PeriodicWorkRequestBuilder<PlannedTrainingReminderWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
