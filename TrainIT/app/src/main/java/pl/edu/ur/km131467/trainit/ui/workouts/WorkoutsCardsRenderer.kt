package pl.edu.ur.km131467.trainit.ui.workouts

import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import pl.edu.ur.km131467.trainit.R

/**
 * Influje karty treningów ([R.layout.item_workout_card]) i dodaje je do kontenera listy.
 *
 * @see WorkoutsHardcodedData
 */
object WorkoutsCardsRenderer {

    /**
     * Tworzy widoki kart dla każdego elementu [plans] i dodaje je do [container].
     *
     * @param activity aktywność używana do [Toast] i [LayoutInflater].
     * @param container kontener pionowy na karty.
     * @param plans lista stubów planów.
     */
    fun populate(
        activity: WorkoutsActivity,
        container: LinearLayout,
        plans: List<WorkoutsHardcodedData.WorkoutPlan>,
    ) {
        val inflater = LayoutInflater.from(activity)

        for (plan in plans) {
            val cardView = inflater.inflate(R.layout.item_workout_card, container, false)

            cardView.findViewById<TextView>(R.id.tvWorkoutName).text = plan.name

            val badgeView = cardView.findViewById<TextView>(R.id.tvDifficultyBadge)
            badgeView.text = plan.difficulty
            badgeView.setTextColor(plan.difficultyColor)
            badgeView.setBackgroundResource(plan.difficultyBgRes)

            cardView.findViewById<TextView>(R.id.tvLastPerformed).text = plan.lastPerformed
            cardView.findViewById<TextView>(R.id.tvExerciseCount).text =
                "${plan.exerciseCount} \u0107wicze\u0144"
            cardView.findViewById<TextView>(R.id.tvDuration).text = plan.duration

            cardView.findViewById<MaterialButton>(R.id.btnStartWorkout).setOnClickListener {
                Toast.makeText(
                    activity,
                    "Rozpoczynam: ${plan.name} (stub)",
                    Toast.LENGTH_SHORT,
                ).show()
            }

            container.addView(cardView)
        }
    }
}
