package pl.edu.ur.km131467.trainit.ui.workouts

import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.ui.feature.FeatureListItem

/**
 * Influje karty treningów ([R.layout.item_workout_card]) i dodaje je do kontenera listy.
 *
 * Dane wejściowe są mapowane z [FeatureListItem] zwróconych przez backend.
 */
object WorkoutsCardsRenderer {
    private val levelRegex = Regex("Poziom:\\s*([^,]+)", RegexOption.IGNORE_CASE)
    private val durationRegex = Regex("czas:\\s*(\\d+)\\s*min", RegexOption.IGNORE_CASE)

    /**
     * Tworzy widoki kart dla każdego elementu [plans] i dodaje je do [container].
     *
     * @param activity aktywność używana do [LayoutInflater].
     * @param container kontener pionowy na karty.
     * @param items lista planów treningowych pochodzących z API.
     * @param hasActiveSession czy użytkownik ma aktywną sesję.
     * @param onStartClicked callback uruchamiany po kliknięciu przycisku startu planu.
     * @param onStopClicked callback uruchamiany po kliknięciu przerwania aktywnej sesji.
     * @param onPlanClicked callback po kliknięciu karty planu (np. edycja).
     * @param onDeleteClicked callback uruchamiany po kliknięciu usunięcia planu.
     */
    fun populate(
        activity: WorkoutsActivity,
        container: LinearLayout,
        items: List<FeatureListItem>,
        hasActiveSession: Boolean,
        onStartClicked: (FeatureListItem) -> Unit,
        onStopClicked: () -> Unit,
        onPlanClicked: (FeatureListItem) -> Unit,
        onDeleteClicked: (FeatureListItem) -> Unit,
    ) {
        val inflater = LayoutInflater.from(activity)
        container.removeAllViews()
        for (item in items) {
            val cardView = inflater.inflate(R.layout.item_workout_card, container, false)
            val parsed = parseWorkoutMeta(item)

            cardView.findViewById<TextView>(R.id.tvWorkoutName).text = item.title

            val badgeView = cardView.findViewById<TextView>(R.id.tvDifficultyBadge)
            badgeView.text = parsed.levelLabel
            badgeView.setBackgroundResource(parsed.levelBadgeRes)

            cardView.findViewById<TextView>(R.id.tvLastPerformed).text = "Typ: ${parsed.trainingType}"
            cardView.findViewById<TextView>(R.id.tvExerciseCount).text = parsed.levelDetail
            cardView.findViewById<TextView>(R.id.tvDuration).text = parsed.durationLabel

            cardView.setOnClickListener { onPlanClicked(item) }

            val startButton = cardView.findViewById<MaterialButton>(R.id.btnStartWorkout)
            startButton.isEnabled = true
            startButton.alpha = 1f
            startButton.text = if (hasActiveSession) "Zakończ sesję" else "Rozpocznij trening"
            startButton.setOnClickListener {
                if (hasActiveSession) onStopClicked() else onStartClicked(item)
            }
            cardView.findViewById<MaterialButton>(R.id.btnDeleteWorkout).setOnClickListener {
                onDeleteClicked(item)
            }

            container.addView(cardView)
        }
    }

    private fun parseWorkoutMeta(item: FeatureListItem): WorkoutCardMeta {
        val rawLevel = levelRegex.find(item.subtitle)?.groupValues?.getOrNull(1)?.trim().orEmpty()
        val durationMinutes = durationRegex.find(item.subtitle)?.groupValues?.getOrNull(1)?.toIntOrNull()
        val normalizedLevel = rawLevel.uppercase()

        val levelLabel = when {
            normalizedLevel.contains("TRUDNY") || normalizedLevel.contains("HARD") -> "Trudny"
            normalizedLevel.contains("ŁATWY") || normalizedLevel.contains("EASY") -> "Łatwy"
            rawLevel.isNotBlank() -> rawLevel.replaceFirstChar { it.uppercase() }
            else -> "Średni"
        }
        val levelBadgeRes = when {
            normalizedLevel.contains("TRUDNY") || normalizedLevel.contains("HARD") -> R.drawable.bg_badge_hard
            normalizedLevel.contains("BARDZO") -> R.drawable.bg_badge_very_hard
            else -> R.drawable.bg_badge_medium
        }
        val durationLabel = durationMinutes?.let { "Plan: $it min" } ?: "Plan: ?"
        val trainingType = workoutTypeFromTitle(item.title)
        return WorkoutCardMeta(
            levelLabel = levelLabel,
            levelBadgeRes = levelBadgeRes,
            levelDetail = "Poziom: $levelLabel",
            durationLabel = durationLabel,
            trainingType = trainingType,
        )
    }

    private fun workoutTypeFromTitle(title: String): String {
        val normalized = title.lowercase()
        return when {
            "push" in normalized -> "Push"
            "pull" in normalized -> "Pull"
            "leg" in normalized || "nogi" in normalized -> "Legs"
            "full" in normalized -> "Full Body"
            else -> "Plan użytkownika"
        }
    }

    private data class WorkoutCardMeta(
        val levelLabel: String,
        val levelBadgeRes: Int,
        val levelDetail: String,
        val durationLabel: String,
        val trainingType: String,
    )
}
