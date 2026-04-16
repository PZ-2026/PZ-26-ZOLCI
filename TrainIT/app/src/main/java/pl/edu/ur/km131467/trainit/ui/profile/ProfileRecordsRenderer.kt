package pl.edu.ur.km131467.trainit.ui.profile

import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.util.TypedValue
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.ui.common.dpToPx

/**
 * Dynamicznie buduje karty rekordów osobistych w [LinearLayout].
 *
 * @see ProfileHardcodedData.personalRecords
 */
class ProfileRecordsRenderer(
    private val activity: AppCompatActivity,
) {

    /**
     * Dodaje karty rekordów do [recordsContainer].
     *
     * @param recordsContainer kontener pionowy na karty.
     * @param records lista modeli rekordów.
     */
    fun render(
        recordsContainer: LinearLayout,
        records: List<ProfileHardcodedData.PersonalRecord>,
    ) {
        for (record in records) {
            val card = LinearLayout(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { bottomMargin = activity.dpToPx(10f) }
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_card_dark)
                setPadding(
                    activity.dpToPx(16f),
                    activity.dpToPx(14f),
                    activity.dpToPx(16f),
                    activity.dpToPx(14f),
                )
            }

            val topRow = LinearLayout(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvExercise = TextView(activity).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = record.exercise
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColor(Color.WHITE)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val tvWeight = TextView(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                text = record.weight
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(Color.WHITE)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            topRow.addView(tvExercise)
            topRow.addView(tvWeight)

            val bottomRow = LinearLayout(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = activity.dpToPx(4f) }
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvDate = TextView(activity).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = record.date
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(ContextCompat.getColor(activity, R.color.text_secondary))
            }

            val tvReps = TextView(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                text = record.reps
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(ContextCompat.getColor(activity, R.color.text_secondary))
            }

            bottomRow.addView(tvDate)
            bottomRow.addView(tvReps)

            card.addView(topRow)
            card.addView(bottomRow)
            recordsContainer.addView(card)
        }
    }
}
