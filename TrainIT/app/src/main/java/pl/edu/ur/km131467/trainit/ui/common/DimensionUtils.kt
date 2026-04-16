package pl.edu.ur.km131467.trainit.ui.common

import android.content.Context
import android.util.TypedValue

/**
 * Narzędzia przeliczania jednostek wymiarów dla widoków.
 *
 * @see TypedValue.applyDimension
 */

/**
 * Przelicza wartość z **dp** na **piksele** (px) dla metryki wyświetlacza bieżącego [Context].
 *
 * @param dp wartość w density-independent pixels.
 * @return odpowiednik w pikselach (zaokrąglony w dół do [Int]).
 */
fun Context.dpToPx(dp: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        resources.displayMetrics,
    ).toInt()
}
