package pl.edu.ur.km131467.trainit.ui.common

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView

/**
 * Funkcje pomocnicze do budowania tekstu ze spanami ([SpannableString]) —
 * branding „TrainIT” oraz klikalne fragmenty linków.
 *
 * @see ForegroundColorSpan
 * @see ClickableSpan
 */

/**
 * Ustawia tekst w [textView] i nakłada [ForegroundColorSpan] w kolorze [accentColor]
 * na zakres [[accentStart], [accentEnd]) (indeksy jak w [String.substring]).
 *
 * Typowe użycie: wyróżnienie końcówki „IT” w nazwie aplikacji.
 *
 * @param textView widok docelowy.
 * @param text pełny tekst do wyświetlenia.
 * @param accentColor kolor wyróżnienia (ARGB).
 * @param accentStart indeks początku (włącznie) fragmentu wyróżnionego.
 * @param accentEnd indeks końca (wyłącznie) fragmentu wyróżnionego.
 */
fun applyAppNameSpan(
    textView: TextView,
    text: String,
    accentColor: Int,
    accentStart: Int,
    accentEnd: Int,
) {
    val spannable = SpannableString(text)
    spannable.setSpan(
        ForegroundColorSpan(accentColor),
        accentStart,
        accentEnd,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
    )
    textView.text = spannable
}

/**
 * Nakłada na [fullText] klikalny fragment [clickablePart]: kolor [accentColor], pogrubienie,
 * [ClickableSpan] bez podkreślenia; ustawia [LinkMovementMethod] i przezroczyste podświetlenie.
 *
 * @param textView widok docelowy.
 * @param fullText pełny tekst wyświetlany w widoku.
 * @param clickablePart podciąg [fullText], który ma być klikalny (musi wystąpić dokładnie raz).
 * @param accentColor kolor linku (ARGB).
 * @param onClick akcja po kliknięciu fragmentu.
 */
fun setupClickableSpan(
    textView: TextView,
    fullText: String,
    clickablePart: String,
    accentColor: Int,
    onClick: () -> Unit,
) {
    val spannable = SpannableString(fullText)
    val startIndex = fullText.indexOf(clickablePart)
    val endIndex = startIndex + clickablePart.length

    spannable.setSpan(
        ForegroundColorSpan(accentColor),
        startIndex,
        endIndex,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
    )
    spannable.setSpan(
        StyleSpan(Typeface.BOLD),
        startIndex,
        endIndex,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
    )
    spannable.setSpan(
        object : ClickableSpan() {
            override fun onClick(widget: View) {
                onClick()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = accentColor
            }
        },
        startIndex,
        endIndex,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
    )

    textView.text = spannable
    textView.movementMethod = LinkMovementMethod.getInstance()
    textView.highlightColor = android.graphics.Color.TRANSPARENT
}
