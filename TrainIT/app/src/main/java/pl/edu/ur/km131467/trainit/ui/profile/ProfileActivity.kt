package pl.edu.ur.km131467.trainit.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.repository.FeatureRepository
import pl.edu.ur.km131467.trainit.ui.common.BottomNavHelper
import pl.edu.ur.km131467.trainit.ui.feature.FeatureModule
import pl.edu.ur.km131467.trainit.ui.feature.NotificationsActivity
import pl.edu.ur.km131467.trainit.ui.feature.ReportsActivity
import pl.edu.ur.km131467.trainit.ui.feature.RolePanelActivity
import pl.edu.ur.km131467.trainit.ui.feature.SettingsActivity
import pl.edu.ur.km131467.trainit.ui.login.LoginActivity

/**
 * Aktywność ekranu profilu użytkownika.
 *
 * Deleguje budowę sekcji do rendererów ([WeeklyChartRenderer], [ProfileRecordsRenderer],
 * [AchievementsGridRenderer], [ProfileSummaryRenderer]) oraz wypełnianie nagłówka
 * przez [ProfileStatsBinder]. Dane testowe pochodzą z [ProfileHardcodedData].
 * Wylogowanie czyści sesję przez [SessionManager].
 *
 * @see ProfileHardcodedData
 * @see LoginActivity
 */
class ProfileActivity : AppCompatActivity() {
    /** Menedżer sesji użytkownika. */
    private lateinit var sessionManager: SessionManager

    /** Repozytorium danych backendowych. */
    private val featureRepository = FeatureRepository()

    /** Dolna nawigacja (Home / Treningi / Profil). */
    private lateinit var bottomNavigation: BottomNavigationView

    /** Przycisk "Wyloguj się". */
    private lateinit var btnLogout: MaterialButton
    /** Skrót do modułu raportów. */
    private lateinit var btnOpenReports: MaterialButton
    /** Skrót do modułu ustawień. */
    private lateinit var btnOpenSettings: MaterialButton
    /** Skrót do modułu powiadomień. */
    private lateinit var btnOpenNotifications: MaterialButton
    /** Skrót do ekranu panelu roli. */
    private lateinit var btnOpenRolePanel: MaterialButton

    /** Kontener na słupki wykresu aktywności tygodniowej. */
    private lateinit var chartBarsContainer: LinearLayout

    /** Kontener na etykiety dni tygodnia pod wykresem. */
    private lateinit var chartDaysContainer: LinearLayout

    /** Kontener na karty rekordów osobistych. */
    private lateinit var recordsContainer: LinearLayout

    /** Pierwszy rząd osiągnięć (odblokowane). */
    private lateinit var achievementsRow1: LinearLayout

    /** Drugi rząd osiągnięć (zablokowane). */
    private lateinit var achievementsRow2: LinearLayout

    /** Kontener na wiersze sekcji podsumowania. */
    private lateinit var summaryContainer: LinearLayout

    /**
     * Wiąże layout, wypełnia dane profilu i konfiguruje nawigację oraz wylogowanie.
     *
     * @param savedInstanceState zapisany stan instancji (nieużywany).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_profile)

        initViews()
        ProfileStatsBinder.bindHeaderAndPills(
            tvProfileName = findViewById(R.id.tvProfileName),
            tvMemberSince = findViewById(R.id.tvMemberSince),
            tvProfileStatWorkouts = findViewById(R.id.tvProfileStatWorkouts),
            tvProfileStatHours = findViewById(R.id.tvProfileStatHours),
            tvProfileStatStreak = findViewById(R.id.tvProfileStatStreak),
            profileName = listOfNotNull(sessionManager.getFirstName(), sessionManager.getRole()).joinToString(" • "),
            memberSinceText = "Konto aktywne",
            workoutsText = "0",
            hoursText = "0h",
            streakText = "0",
        )

        WeeklyChartRenderer(this).render(
            chartBarsContainer = chartBarsContainer,
            chartDaysContainer = chartDaysContainer,
            weeklyData = ProfileHardcodedData.weeklyChartValues,
            dayLabels = ProfileHardcodedData.dayLabels,
        )
        ProfileRecordsRenderer(this).render(recordsContainer, ProfileHardcodedData.personalRecords)
        AchievementsGridRenderer(this).render(achievementsRow1, achievementsRow2, ProfileHardcodedData.achievements)
        ProfileSummaryRenderer(this).render(summaryContainer, ProfileHardcodedData.summaryItems)

        BottomNavHelper.setupBottomNav(bottomNavigation, this, R.id.nav_profile)
        setupFeatureShortcuts()
        setupLogout()
        loadProfileData()
    }

    /** Inicjalizuje wszystkie kontrolki ekranu profilu. */
    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        btnLogout = findViewById(R.id.btnLogout)
        btnOpenReports = findViewById(R.id.btnOpenReports)
        btnOpenSettings = findViewById(R.id.btnOpenSettings)
        btnOpenNotifications = findViewById(R.id.btnOpenNotifications)
        btnOpenRolePanel = findViewById(R.id.btnOpenRolePanel)
        chartBarsContainer = findViewById(R.id.chartBarsContainer)
        chartDaysContainer = findViewById(R.id.chartDaysContainer)
        recordsContainer = findViewById(R.id.recordsContainer)
        achievementsRow1 = findViewById(R.id.achievementsRow1)
        achievementsRow2 = findViewById(R.id.achievementsRow2)
        summaryContainer = findViewById(R.id.summaryContainer)
    }

    /**
     * Konfiguruje skróty nawigacyjne do dodatkowych modułów.
     */
    private fun setupFeatureShortcuts() {
        btnOpenReports.setOnClickListener { startActivity(Intent(this, ReportsActivity::class.java)) }
        btnOpenSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        btnOpenNotifications.setOnClickListener { startActivity(Intent(this, NotificationsActivity::class.java)) }
        if (sessionManager.getRole().uppercase() == "USER") {
            btnOpenRolePanel.isEnabled = false
            btnOpenRolePanel.alpha = 0.5f
        } else {
            btnOpenRolePanel.setOnClickListener { startActivity(Intent(this, RolePanelActivity::class.java)) }
        }
    }

    /**
     * Czyści sesję ([SessionManager.clearSession]) i otwiera [LoginActivity] z czyszczeniem stosu.
     */
    private fun setupLogout() {
        btnLogout.setOnClickListener {
            SessionManager(this).clearSession()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    /** Pobiera dane profilu użytkownika z backendu i odświeża sekcję statystyk oraz podsumowania. */
    private fun loadProfileData() {
        lifecycleScope.launch {
            val statistics = runCatching { featureRepository.getItems(FeatureModule.STATISTICS, sessionManager) }.getOrDefault(emptyList())
            val settings = runCatching { featureRepository.getItems(FeatureModule.SETTINGS, sessionManager) }.getOrDefault(emptyList())
            val notifications = runCatching {
                featureRepository.getItems(FeatureModule.NOTIFICATIONS, sessionManager)
            }.getOrDefault(emptyList())
            val reports = runCatching { featureRepository.getItems(FeatureModule.REPORTS, sessionManager) }.getOrDefault(emptyList())

            ProfileStatsBinder.bindHeaderAndPills(
                tvProfileName = findViewById(R.id.tvProfileName),
                tvMemberSince = findViewById(R.id.tvMemberSince),
                tvProfileStatWorkouts = findViewById(R.id.tvProfileStatWorkouts),
                tvProfileStatHours = findViewById(R.id.tvProfileStatHours),
                tvProfileStatStreak = findViewById(R.id.tvProfileStatStreak),
                profileName = listOfNotNull(sessionManager.getFirstName(), sessionManager.getRole()).joinToString(" • "),
                memberSinceText = settings.firstOrNull()?.subtitle ?: "Konto aktywne",
                workoutsText = statistics.getOrNull(0)?.subtitle ?: "0",
                hoursText = "${statistics.getOrNull(1)?.subtitle ?: "0"}h",
                streakText = statistics.getOrNull(2)?.subtitle ?: "0",
            )

            summaryContainer.removeAllViews()
            ProfileSummaryRenderer(this@ProfileActivity).render(
                summaryContainer,
                listOf(
                    ProfileHardcodedData.SummaryItem(
                        R.drawable.ic_dumbbell,
                        reports.firstOrNull()?.title ?: "Raport",
                        reports.firstOrNull()?.subtitle ?: "Brak",
                    ),
                    ProfileHardcodedData.SummaryItem(
                        R.drawable.ic_clock,
                        notifications.firstOrNull()?.title ?: "Powiadomienia",
                        notifications.firstOrNull()?.subtitle ?: "Brak",
                    ),
                    ProfileHardcodedData.SummaryItem(
                        R.drawable.ic_fire,
                        settings.getOrNull(1)?.title ?: "Ustawienia",
                        settings.getOrNull(1)?.subtitle ?: "Brak",
                    ),
                ),
            )
        }
    }
}
