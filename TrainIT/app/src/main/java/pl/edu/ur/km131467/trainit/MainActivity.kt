package pl.edu.ur.km131467.trainit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.ui.common.BottomNavHelper
import pl.edu.ur.km131467.trainit.ui.common.applyAppNameSpan
import pl.edu.ur.km131467.trainit.ui.login.LoginActivity
import pl.edu.ur.km131467.trainit.ui.main.MainHardcodedData
import pl.edu.ur.km131467.trainit.ui.main.bindDashboardStats
import pl.edu.ur.km131467.trainit.ui.main.bindWeeklyProgress

/**
 * Główna aktywność aplikacji TrainIT pełniąca rolę dashboardu.
 *
 * Punkt wejścia (launcher): przy braku sesji ([SessionManager.isLoggedIn]) przekierowuje do
 * [LoginActivity]. Statystyki i cel tygodniowy wiązane są ze [MainHardcodedData] przez
 * funkcje w pakiecie [pl.edu.ur.km131467.trainit.ui.main].
 *
 * @see LoginActivity
 * @see pl.edu.ur.km131467.trainit.ui.workouts.WorkoutsActivity
 * @see pl.edu.ur.km131467.trainit.ui.profile.ProfileActivity
 */
class MainActivity : AppCompatActivity() {

    /** Kolor żółty akcentu nagłówka „TrainIT”. */
    private val headerAccentColor: Int = Color.parseColor("#FFD600")

    /** Pole tekstowe z nazwą aplikacji "TrainIT" w nagłówku. */
    private lateinit var tvHeaderAppName: TextView

    /** Okrągły wskaźnik postępu celu tygodniowego. */
    private lateinit var progressWeekly: CircularProgressIndicator

    /** Tekst informujący o postępie celu tygodniowego. */
    private lateinit var tvWeeklyGoalProgress: TextView

    /** Podpowiedź pod postępem celu. */
    private lateinit var tvWeeklyGoalHint: TextView

    /** Wartość statystyki "Seria dni". */
    private lateinit var tvStatStreak: TextView

    /** Wartość statystyki "Ten tydzień". */
    private lateinit var tvStatWeek: TextView

    /** Wartość statystyki łącznego czasu (godziny). */
    private lateinit var tvStatTotalHours: TextView

    /** Wartość statystyki "Ukończone" (treningi). */
    private lateinit var tvStatCompleted: TextView

    /** Odnośnik "Zobacz wszystkie" w sekcji ostatniej aktywności. */
    private lateinit var tvSeeAll: TextView

    /** Dolna nawigacja (Home / Treningi / Profil). */
    private lateinit var bottomNavigation: BottomNavigationView

    /**
     * Sprawdza sesję, ustawia layout i wypełnia dashboard danymi stubów.
     *
     * @param savedInstanceState zapisany stan instancji (nieużywany).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager(this).isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_home)

        initViews()
        applyAppNameSpan(tvHeaderAppName, "TrainIT", headerAccentColor, 5, 7)
        bindWeeklyProgress(
            progressWeekly,
            tvWeeklyGoalProgress,
            tvWeeklyGoalHint,
            MainHardcodedData.weeklyCompleted,
            MainHardcodedData.weeklyGoal,
        )
        bindDashboardStats(
            tvStatStreak,
            tvStatWeek,
            tvStatTotalHours,
            tvStatCompleted,
            MainHardcodedData.dashboardStats,
        )
        BottomNavHelper.setupBottomNav(bottomNavigation, this, R.id.nav_home)
        setupClickListeners()
    }

    private fun initViews() {
        tvHeaderAppName = findViewById(R.id.tvHeaderAppName)
        progressWeekly = findViewById(R.id.progressWeekly)
        tvWeeklyGoalProgress = findViewById(R.id.tvWeeklyGoalProgress)
        tvWeeklyGoalHint = findViewById(R.id.tvWeeklyGoalHint)
        tvStatStreak = findViewById(R.id.tvStatStreak)
        tvStatWeek = findViewById(R.id.tvStatWeek)
        tvStatTotalHours = findViewById(R.id.tvStatTotalHours)
        tvStatCompleted = findViewById(R.id.tvStatCompleted)
        tvSeeAll = findViewById(R.id.tvSeeAll)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupClickListeners() {
        tvSeeAll.setOnClickListener {
            Toast.makeText(this, "Zobacz wszystkie (stub)", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.cardRecentActivity).setOnClickListener {
            Toast.makeText(this, "Szczegóły treningu (stub)", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Aktualizuje wartości kart statystyk (API pod przyszłe dane z sieci).
     *
     * @param streak seria dni treningowych z rzędu.
     * @param weekDays liczba dni treningowych w bieżącym tygodniu.
     * @param totalHours łączna liczba godzin treningowych.
     * @param completedCount całkowita liczba ukończonych treningów.
     */
    fun updateStats(streak: Int, weekDays: Int, totalHours: Int, completedCount: Int) {
        bindDashboardStats(
            tvStatStreak,
            tvStatWeek,
            tvStatTotalHours,
            tvStatCompleted,
            MainHardcodedData.DashboardStats(streak, weekDays, totalHours, completedCount),
        )
    }
}
