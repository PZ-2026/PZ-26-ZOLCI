package pl.edu.ur.km131467.trainit.ui.admin

import android.content.Context
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

/**
 * Główny ekran panelu administratora / trenera.
 *
 * Umożliwia przeglądanie danych globalnych (wszystkie rekordy w systemie)
 * oraz filtrowanie danych po konkretnym użytkowniku (podanie raw userId).
 */
class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var tvAdminRoleBadge: android.widget.TextView
    private lateinit var etTargetUserId: TextInputEditText

    // Global buttons
    private lateinit var btnGlobalWorkouts: MaterialButton
    private lateinit var btnGlobalExercises: MaterialButton
    private lateinit var btnGlobalSessions: MaterialButton
    private lateinit var btnGlobalStatistics: MaterialButton
    private lateinit var btnGlobalReports: MaterialButton
    private lateinit var btnGlobalSettings: MaterialButton
    private lateinit var btnGlobalNotifications: MaterialButton

    // User buttons
    private lateinit var btnUserWorkouts: MaterialButton
    private lateinit var btnUserExercises: MaterialButton
    private lateinit var btnUserSessions: MaterialButton
    private lateinit var btnUserStatistics: MaterialButton
    private lateinit var btnUserReports: MaterialButton
    private lateinit var btnUserSettings: MaterialButton
    private lateinit var btnUserNotifications: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_admin_dashboard)
        initViews()
        bindRoleBadge()
        BottomNavHelper.setupBottomNav(bottomNavigation, this, R.id.nav_profile)
        setupGlobalButtons()
        setupUserButtons()
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        tvAdminRoleBadge = findViewById(R.id.tvAdminRoleBadge)
        etTargetUserId = findViewById(R.id.etTargetUserId)

        btnGlobalWorkouts = findViewById(R.id.btnGlobalWorkouts)
        btnGlobalExercises = findViewById(R.id.btnGlobalExercises)
        btnGlobalSessions = findViewById(R.id.btnGlobalSessions)
        btnGlobalStatistics = findViewById(R.id.btnGlobalStatistics)
        btnGlobalReports = findViewById(R.id.btnGlobalReports)
        btnGlobalSettings = findViewById(R.id.btnGlobalSettings)
        btnGlobalNotifications = findViewById(R.id.btnGlobalNotifications)

        btnUserWorkouts = findViewById(R.id.btnUserWorkouts)
        btnUserExercises = findViewById(R.id.btnUserExercises)
        btnUserSessions = findViewById(R.id.btnUserSessions)
        btnUserStatistics = findViewById(R.id.btnUserStatistics)
        btnUserReports = findViewById(R.id.btnUserReports)
        btnUserSettings = findViewById(R.id.btnUserSettings)
        btnUserNotifications = findViewById(R.id.btnUserNotifications)
    }

    private fun bindRoleBadge() {
        val role = sessionManager.getRole().uppercase()
        tvAdminRoleBadge.text = role
        when (role) {
            "ADMIN" -> tvAdminRoleBadge.setBackgroundResource(R.drawable.bg_badge_hard)
            "TRAINER" -> tvAdminRoleBadge.setBackgroundResource(R.drawable.bg_badge_medium)
            else -> tvAdminRoleBadge.setBackgroundResource(R.drawable.bg_badge_very_hard)
        }
    }

    private fun setupGlobalButtons() {
        btnGlobalWorkouts.setOnClickListener {
            startActivity(AdminGlobalModuleActivity.createIntent(this, FeatureModule.ROLE_PANEL))
        }
        btnGlobalExercises.setOnClickListener {
            startActivity(AdminGlobalModuleActivity.createIntent(this, FeatureModule.EXERCISES))
        }
        btnGlobalSessions.setOnClickListener {
            startActivity(AdminGlobalModuleActivity.createIntent(this, FeatureModule.SESSIONS))
        }
        btnGlobalStatistics.setOnClickListener {
            startActivity(AdminGlobalModuleActivity.createIntent(this, FeatureModule.STATISTICS))
        }
        btnGlobalReports.setOnClickListener {
            startActivity(AdminGlobalModuleActivity.createIntent(this, FeatureModule.REPORTS))
        }
        btnGlobalSettings.setOnClickListener {
            startActivity(AdminGlobalModuleActivity.createIntent(this, FeatureModule.SETTINGS))
        }
        btnGlobalNotifications.setOnClickListener {
            startActivity(AdminGlobalModuleActivity.createIntent(this, FeatureModule.NOTIFICATIONS))
        }
    }

    private fun setupUserButtons() {
        btnUserWorkouts.setOnClickListener { openUserModule(FeatureModule.ROLE_PANEL) }
        btnUserExercises.setOnClickListener { openUserModule(FeatureModule.EXERCISES) }
        btnUserSessions.setOnClickListener { openUserModule(FeatureModule.SESSIONS) }
        btnUserStatistics.setOnClickListener { openUserModule(FeatureModule.STATISTICS) }
        btnUserReports.setOnClickListener { openUserModule(FeatureModule.REPORTS) }
        btnUserSettings.setOnClickListener { openUserModule(FeatureModule.SETTINGS) }
        btnUserNotifications.setOnClickListener { openUserModule(FeatureModule.NOTIFICATIONS) }
    }

    private fun openUserModule(module: FeatureModule) {
        val raw = etTargetUserId.text?.toString()?.trim().orEmpty()
        if (raw.isBlank()) {
            Toast.makeText(this, "Podaj ID użytkownika", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = raw.toIntOrNull()
        if (userId == null || userId <= 0) {
            Toast.makeText(this, "Nieprawidłowe ID użytkownika", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(AdminUserModuleActivity.createIntent(this, module, userId))
    }
}
