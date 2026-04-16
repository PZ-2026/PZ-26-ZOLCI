package pl.edu.ur.km131467.trainit.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.ui.common.BottomNavHelper
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

    /** Dolna nawigacja (Home / Treningi / Profil). */
    private lateinit var bottomNavigation: BottomNavigationView

    /** Przycisk "Wyloguj się". */
    private lateinit var btnLogout: MaterialButton

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
     * Wiąże layout, wypełnia dane stubami i konfiguruje nawigację oraz wylogowanie.
     *
     * @param savedInstanceState zapisany stan instancji (nieużywany).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        ProfileStatsBinder.bindHeaderAndPills(
            tvProfileName = findViewById(R.id.tvProfileName),
            tvMemberSince = findViewById(R.id.tvMemberSince),
            tvProfileStatWorkouts = findViewById(R.id.tvProfileStatWorkouts),
            tvProfileStatHours = findViewById(R.id.tvProfileStatHours),
            tvProfileStatStreak = findViewById(R.id.tvProfileStatStreak),
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
        setupLogout()
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        btnLogout = findViewById(R.id.btnLogout)
        chartBarsContainer = findViewById(R.id.chartBarsContainer)
        chartDaysContainer = findViewById(R.id.chartDaysContainer)
        recordsContainer = findViewById(R.id.recordsContainer)
        achievementsRow1 = findViewById(R.id.achievementsRow1)
        achievementsRow2 = findViewById(R.id.achievementsRow2)
        summaryContainer = findViewById(R.id.summaryContainer)
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
}
