package pl.edu.ur.km131467.trainit.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.repository.ReportRepository
import pl.edu.ur.km131467.trainit.ui.feature.FeatureModule
import pl.edu.ur.km131467.trainit.ui.login.LoginActivity
import pl.edu.ur.km131467.trainit.ui.workouts.AddWorkoutActivity
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Główny ekran panelu trenera personalnego.
 *
 * Umożliwia tworzenie planów treningowych dla klientów, przeglądanie historii
 * ich treningów oraz analizę osiąganych wyników. Obsługuje też generowanie
 * raportów PDF za wybrany zakres dat (WF-12).
 */
class TrainerPanelActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var reportRepository: ReportRepository
    private lateinit var btnLogout: MaterialButton
    private lateinit var etTargetUserId: TextInputEditText

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC)

    /** Przyciski sekcji planów treningowych klienta. */
    private lateinit var btnCreatePlanForClient: MaterialButton
    private lateinit var btnClientPlans: MaterialButton
    private lateinit var btnAllPlans: MaterialButton

    /** Przyciski historii treningów i wyników klienta. */
    private lateinit var btnClientHistory: MaterialButton
    private lateinit var btnClientResults: MaterialButton
    private lateinit var btnAllSessions: MaterialButton

    /** Przyciski raportów, statystyk i generowania PDF. */
    private lateinit var btnClientReports: MaterialButton
    private lateinit var btnGlobalReports: MaterialButton
    private lateinit var btnClientStatistics: MaterialButton
    private lateinit var btnGlobalStatistics: MaterialButton
    private lateinit var btnGenerateReport: MaterialButton

    /**
     * Weryfikuje sesję trenera, wiąże widoki i konfiguruje akcje panelu klienta.
     *
     * @param savedInstanceState zapisany stan instancji aktywności
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        reportRepository = ReportRepository(this)
        setContentView(R.layout.activity_trainer_panel)
        initViews()
        btnLogout.setOnClickListener {
            sessionManager.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        setupPlanButtons()
        setupHistoryButtons()
        setupReportButtons()
    }

    private fun initViews() {
        btnLogout = findViewById(R.id.btnLogout)
        etTargetUserId = findViewById(R.id.etTargetUserId)

        btnCreatePlanForClient = findViewById(R.id.btnCreatePlanForClient)
        btnClientPlans = findViewById(R.id.btnClientPlans)
        btnAllPlans = findViewById(R.id.btnAllPlans)

        btnClientHistory = findViewById(R.id.btnClientHistory)
        btnClientResults = findViewById(R.id.btnClientResults)
        btnAllSessions = findViewById(R.id.btnAllSessions)

        btnClientReports = findViewById(R.id.btnClientReports)
        btnGlobalReports = findViewById(R.id.btnGlobalReports)
        btnClientStatistics = findViewById(R.id.btnClientStatistics)
        btnGlobalStatistics = findViewById(R.id.btnGlobalStatistics)
        btnGenerateReport = findViewById(R.id.btnGenerateReport)
    }

    private fun setupPlanButtons() {
        btnCreatePlanForClient.setOnClickListener {
            val userId = resolveTargetUserId()
            if (userId == null) {
                Toast.makeText(this, "Podaj ID klienta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, AddWorkoutActivity::class.java).apply {
                putExtra(AddWorkoutActivity.EXTRA_TARGET_USER_ID, userId)
            }
            startActivity(intent)
        }

        btnClientPlans.setOnClickListener { openUserModule(FeatureModule.ROLE_PANEL) }
        btnAllPlans.setOnClickListener {
            startActivity(AdminGlobalModuleActivity.createIntent(this, FeatureModule.ROLE_PANEL))
        }
    }

    private fun setupHistoryButtons() {
        btnClientHistory.setOnClickListener { openUserModule(FeatureModule.SESSIONS) }
        btnClientResults.setOnClickListener {
            val userId = resolveTargetUserId()
            if (userId == null) {
                Toast.makeText(this, "Podaj ID klienta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(TrainerClientResultsActivity.createIntent(this, userId))
        }
        btnAllSessions.setOnClickListener {
            startActivity(AdminGlobalModuleActivity.createIntent(this, FeatureModule.SESSIONS))
        }
    }

    private fun setupReportButtons() {
        btnClientReports.setOnClickListener { openUserModule(FeatureModule.REPORTS) }
        btnGlobalReports.setOnClickListener {
            startActivity(AdminGlobalModuleActivity.createIntent(this, FeatureModule.REPORTS))
        }
        btnClientStatistics.setOnClickListener { openUserModule(FeatureModule.STATISTICS) }
        btnGlobalStatistics.setOnClickListener {
            startActivity(AdminGlobalModuleActivity.createIntent(this, FeatureModule.STATISTICS))
        }
        btnGenerateReport.setOnClickListener {
            val userId = resolveTargetUserId()
            if (userId == null) {
                Toast.makeText(this, "Podaj ID klienta, aby wygenerować raport", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            pickDateRange { dateFrom, dateTo ->
                launchGenerateReport(userId, dateFrom, dateTo)
            }
        }
    }

    /**
     * Wyświetla Material date range picker (jeden dialog, dwie daty).
     * Po zatwierdzeniu wywołuje [onRangeSelected] z datami w formacie yyyy-MM-dd.
     */
    private fun pickDateRange(onRangeSelected: (String, String) -> Unit) {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Wybierz zakres dat raportu")
            .setSelection(
                androidx.core.util.Pair(
                    MaterialDatePicker.thisMonthInUtcMilliseconds(),
                    MaterialDatePicker.todayInUtcMilliseconds(),
                )
            )
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val dateFrom = dateFormatter.format(Instant.ofEpochMilli(selection.first))
            val dateTo = dateFormatter.format(Instant.ofEpochMilli(selection.second))
            onRangeSelected(dateFrom, dateTo)
        }

        picker.show(supportFragmentManager, "REPORT_DATE_RANGE")
    }

    private fun launchGenerateReport(userId: Int?, dateFrom: String, dateTo: String) {
        val token = sessionManager.getToken() ?: run {
            Toast.makeText(this, "Brak tokenu - zaloguj się ponownie", Toast.LENGTH_SHORT).show()
            return
        }

        btnGenerateReport.isEnabled = false
        btnGenerateReport.text = "Generowanie..."

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                reportRepository.downloadReport(
                    token = token,
                    userId = userId,
                    dateFrom = dateFrom,
                    dateTo = dateTo,
                )
            }

            btnGenerateReport.isEnabled = true
            btnGenerateReport.text = "Generuj raport"

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
                            this@TrainerPanelActivity,
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
                    Toast.makeText(this@TrainerPanelActivity, msg, Toast.LENGTH_LONG).show()
                },
            )
        }
    }

    private fun resolveTargetUserId(): Int? {
        val raw = etTargetUserId.text?.toString()?.trim().orEmpty()
        if (raw.isBlank()) return null
        return raw.toIntOrNull()?.takeIf { it > 0 }
    }

    private fun openUserModule(module: FeatureModule) {
        val userId = resolveTargetUserId()
        if (userId == null) {
            Toast.makeText(this, "Podaj ID klienta", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(AdminUserModuleActivity.createIntent(this, module, userId))
    }
}
