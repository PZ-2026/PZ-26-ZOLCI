package pl.edu.ur.km131467.trainit.ui.workouts

import android.view.LayoutInflater
import android.view.View
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

    /**
     * Tworzy widoki kart dla każdego elementu [plans] i dodaje je do [container].
     *
     * @param activity aktywność używana do [LayoutInflater].
     * @param container kontener pionowy na karty.
     * @param items lista planów treningowych pochodzących z API.
     * @param onStartClicked callback uruchamiany po kliknięciu przycisku startu planu.
     */
    fun populate(
        activity: WorkoutsActivity,
        container: LinearLayout,
        items: List<FeatureListItem>,
        onStartClicked: (FeatureListItem) -> Unit,
    ) {
        val inflater = LayoutInflater.from(activity)
        container.removeAllViews()
        for (item in items) {
            val cardView = inflater.inflate(R.layout.item_workout_card, container, false)

            cardView.findViewById<TextView>(R.id.tvWorkoutName).text = item.title

            val badgeView = cardView.findViewById<TextView>(R.id.tvDifficultyBadge)
            badgeView.text = "Plan użytkownika"
            badgeView.setBackgroundResource(R.drawable.bg_badge_medium)

            cardView.findViewById<TextView>(R.id.tvLastPerformed).text = item.subtitle
            cardView.findViewById<TextView>(R.id.tvExerciseCount).visibility = View.GONE
            cardView.findViewById<TextView>(R.id.tvDuration).visibility = View.GONE

            cardView.findViewById<MaterialButton>(R.id.btnStartWorkout).setOnClickListener {
                onStartClicked(item)
            }

            container.addView(cardView)
        }
    }
}
