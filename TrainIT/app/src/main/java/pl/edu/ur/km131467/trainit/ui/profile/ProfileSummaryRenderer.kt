package pl.edu.ur.km131467.trainit.ui.profile

import android.graphics.Color
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.util.TypedValue
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.ui.common.dpToPx

/**
 * Buduje wiersze sekcji podsumowania profilu (ikona, etykieta, wartość).
 *
 * @see ProfileHardcodedData.summaryItems
 */
class ProfileSummaryRenderer(
    private val activity: AppCompatActivity,
) {

    /**
     * Dodaje wiersze do [summaryContainer] na podstawie [items].
     *
     * @param summaryContainer kontener pionowy.
     * @param items lista wierszy podsumowania.
     */
    fun render(
        summaryContainer: LinearLayout,
        items: List<ProfileHardcodedData.SummaryItem>,
    ) {
        for (item in items) {
            val row = LinearLayout(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { bottomMargin = activity.dpToPx(12f) }
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val icon = ImageView(activity).apply {
                layoutParams = LinearLayout.LayoutParams(activity.dpToPx(20f), activity.dpToPx(20f))
                setImageResource(item.icon)
            }

            val label = TextView(activity).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = activity.dpToPx(12f)
                }
                text = item.label
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(ContextCompat.getColor(activity, R.color.text_secondary))
            }

            val value = TextView(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                text = item.value
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColor(Color.WHITE)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                gravity = Gravity.END
            }

            row.addView(icon)
            row.addView(label)
            row.addView(value)
            summaryContainer.addView(row)
        }
    }
}
