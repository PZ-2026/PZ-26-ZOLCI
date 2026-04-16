package pl.edu.ur.km131467.trainit.ui.profile

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.util.TypedValue
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.ui.common.dpToPx

/**
 * Buduje wykres słupkowy aktywności tygodniowej (Pn–Nd) w kontenerach [LinearLayout].
 *
 * @see ProfileHardcodedData.weeklyChartValues
 */
class WeeklyChartRenderer(
    private val activity: AppCompatActivity,
) {

    /**
     * Rysuje słupki ietykiety dni w przekazanych kontenerach.
     *
     * @param chartBarsContainer wiersz słupków (waga równa dla każdego dnia).
     * @param chartDaysContainer wiersz etykiet pod słupkami.
     * @param weeklyData wartości godzin treningu (indeks 0 = poniedziałek); skala maks. [maxValueHours].
     * @param dayLabels krótkie etykiety dni (ta sama liczba co [weeklyData]).
     * @param maxValueHours maksimum osi Y w godzinach (używane do skalowania wysokości słupka).
     * @param barMaxHeightDp maksymalna wysokość słupka w dp przy wartości równej [maxValueHours].
     */
    fun render(
        chartBarsContainer: LinearLayout,
        chartDaysContainer: LinearLayout,
        weeklyData: List<Float>,
        dayLabels: List<String>,
        maxValueHours: Float = 2f,
        barMaxHeightDp: Int = 120,
    ) {
        for (i in weeklyData.indices) {
            val value = weeklyData[i]

            val barWrapper = LinearLayout(activity).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            }

            val ratio = (value / maxValueHours).coerceIn(0f, 1f)
            val barHeightDp = (barMaxHeightDp * ratio).toInt()
            val barHeightPx = activity.dpToPx(barHeightDp.toFloat())

            val bar = View(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    activity.dpToPx(18f),
                    if (barHeightPx > 0) barHeightPx else activity.dpToPx(2f),
                )
                setBackgroundResource(
                    if (value > 0) R.drawable.bg_bar_chart_bar else R.drawable.bg_bar_chart_empty,
                )
            }

            barWrapper.addView(bar)
            chartBarsContainer.addView(barWrapper)
        }

        for (label in dayLabels) {
            val tv = TextView(activity).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = label
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                setTextColor(ContextCompat.getColor(activity, R.color.text_secondary))
                gravity = Gravity.CENTER
            }
            chartDaysContainer.addView(tv)
        }
    }
}
