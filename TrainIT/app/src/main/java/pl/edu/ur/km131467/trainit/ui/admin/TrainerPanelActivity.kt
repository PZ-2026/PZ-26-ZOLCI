package pl.edu.ur.km131467.trainit.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.ui.common.BottomNavHelper
import pl.edu.ur.km131467.trainit.ui.feature.FeatureModule
import pl.edu.ur.km131467.trainit.ui.login.LoginActivity
import pl.edu.ur.km131467.trainit.ui.workouts.AddWorkoutActivity

/**
 * Główny ekran panelu trenera personalnego.
 *
 * Umożliwia tworzenie planów treningowych dla klientów, przeglądanie historii
 * ich treningów oraz analizę osiąganych wyników.
 */
class TrainerPanelActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var etTargetUserId: TextInputEditText

    // Plans
    private lateinit var btnCreatePlanForClient: MaterialButton
    private lateinit var btnClientPlans: MaterialButton
    private lateinit var btnAllPlans: MaterialButton

    // History & results
    private lateinit var btnClientHistory: MaterialButton
    private lateinit var btnClientResults: MaterialButton
    private lateinit var btnAllSessions: MaterialButton

    // Reports & stats
    private lateinit var btnClientReports: MaterialButton
    private lateinit var btnGlobalReports: MaterialButton
    private lateinit var btnClientStatistics: MaterialButton
    private lateinit var btnGlobalStatistics: MaterialButton
    private lateinit var btnGenerateReport: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_trainer_panel)
        initViews()
        BottomNavHelper.setupBottomNav(bottomNavigation, this, R.id.nav_profile)
        setupPlanButtons()
        setupHistoryButtons()
        setupReportButtons()
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
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
            Toast.makeText(this, "Funkcja w przygotowaniu", Toast.LENGTH_SHORT).show()
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
