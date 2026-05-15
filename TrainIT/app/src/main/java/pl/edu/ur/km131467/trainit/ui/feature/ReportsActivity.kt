package pl.edu.ur.km131467.trainit.ui.feature

import android.content.Intent
import android.widget.Toast
import androidx.core.util.Pair
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.repository.ReportRepository
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Ekran modułu raportów dla użytkownika (Sportowiec).
 *
 * Wyświetla historię wygenerowanych raportów oraz umożliwia generowanie
 * nowego raportu PDF za wybrany zakres dat (WF-12 / US-7).
 */
class ReportsActivity : BaseFeatureActivity() {

    override val module: FeatureModule = FeatureModule.REPORTS
    override val bottomNavItem: Int = R.id.nav_profile

    private val dateFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC)

    private val reportRepository by lazy { ReportRepository(this) }

    /**
     * Przechwytuje kliknięcie „Generuj raport" i zastępuje domyślną akcję ViewModelu
     * oknem wyboru zakresu dat + pobraniem PDF.
     */
    override fun onPrimaryAction(): Boolean {
        pickDateRangeAndGenerate()
        return true
    }

    private fun pickDateRangeAndGenerate() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Wybierz zakres dat raportu")
            .setSelection(
                Pair(
                    MaterialDatePicker.thisMonthInUtcMilliseconds(),
                    MaterialDatePicker.todayInUtcMilliseconds(),
                )
            )
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val dateFrom = dateFormatter.format(Instant.ofEpochMilli(selection.first))
            val dateTo   = dateFormatter.format(Instant.ofEpochMilli(selection.second))
            launchGenerateReport(dateFrom, dateTo)
        }

        picker.show(supportFragmentManager, "REPORT_DATE_RANGE_USER")
    }

    private fun launchGenerateReport(dateFrom: String, dateTo: String) {
        val token = SessionManager(this).getToken() ?: run {
            Toast.makeText(this, "Brak tokenu — zaloguj się ponownie", Toast.LENGTH_SHORT).show()
            return
        }

        setPrimaryActionVisible(false)

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                reportRepository.downloadReport(
                    token    = token,
                    userId   = null,   // null = raport własny zalogowanego użytkownika
                    dateFrom = dateFrom,
                    dateTo   = dateTo,
                )
            }

            setPrimaryActionVisible(true)

            result.fold(
                onSuccess = { file ->
                    val uri = reportRepository.getUriForFile(file)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    }
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@ReportsActivity,
                            "Brak aplikacji PDF. Plik zapisany w pamięci podręcznej.",
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                },
                onFailure = { e ->
                    val msg = if (e.message?.contains("Brak danych") == true) {
                        e.message!!
                    } else {
                        "Błąd generowania raportu: ${e.message}"
                    }
                    Toast.makeText(this@ReportsActivity, msg, Toast.LENGTH_LONG).show()
                },
            )
        }
    }
}
