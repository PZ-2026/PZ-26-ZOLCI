package pl.edu.ur.km131467.trainit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.repository.FeatureRepository
import pl.edu.ur.km131467.trainit.ui.common.BottomNavHelper
import pl.edu.ur.km131467.trainit.ui.common.applyAppNameSpan
import pl.edu.ur.km131467.trainit.ui.feature.FeatureModule
import pl.edu.ur.km131467.trainit.ui.feature.ReportsActivity
import pl.edu.ur.km131467.trainit.ui.feature.StatisticsActivity
import pl.edu.ur.km131467.trainit.ui.login.LoginActivity
import pl.edu.ur.km131467.trainit.ui.main.bindDashboardStats
import pl.edu.ur.km131467.trainit.ui.main.bindWeeklyProgress

/**
 * Główna aktywność aplikacji TrainIT pełniąca rolę dashboardu.
 *
 * Punkt wejścia (launcher): przy braku sesji ([SessionManager.isLoggedIn]) przekierowuje do
 * [LoginActivity]. Statystyki i cel tygodniowy są odczytywane z backendu przez
 * [FeatureRepository].
 *
 * @see LoginActivity
 * @see pl.edu.ur.km131467.trainit.ui.workouts.WorkoutsActivity
 * @see pl.edu.ur.km131467.trainit.ui.profile.ProfileActivity
 */
class MainActivity : AppCompatActivity() {
    /** Sesja aktualnego użytkownika. */
    private lateinit var sessionManager: SessionManager

    /** Repozytorium danych modułów. */
    private val featureRepository = FeatureRepository()

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
     * Sprawdza sesję, ustawia layout i wypełnia dashboard danymi bieżącego użytkownika.
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

        setContentView(R.layout.activity_home)

        initViews()
        applyAppNameSpan(tvHeaderAppName, "TrainIT", headerAccentColor, 5, 7)
        BottomNavHelper.setupBottomNav(bottomNavigation, this, R.id.nav_home)
        setupClickListeners()
        loadDashboardData()
    }

    /** Inicjalizuje referencje kontrolek dashboardu. */
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

    /**
     * Konfiguruje przejścia z sekcji aktywności do ekranów szczegółowych.
     *
     * Link „Zobacz wszystkie” otwiera moduł raportów, a karta aktywności moduł statystyk.
     */
    private fun setupClickListeners() {
        tvSeeAll.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        findViewById<android.view.View>(R.id.cardRecentActivity).setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
    }

    /**
     * Aktualizuje wartości kart statystyk.
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
            pl.edu.ur.km131467.trainit.ui.main.MainHardcodedData.DashboardStats(
                streak,
                weekDays,
                totalHours,
                completedCount,
            ),
        )
    }

    /** Pobiera dane podsumowania użytkownika i odświeża dashboard. */
    private fun loadDashboardData() {
        lifecycleScope.launch {
            runCatching { featureRepository.getItems(FeatureModule.STATISTICS, sessionManager) }
                .onSuccess { items ->
                    val workouts = items.getOrNull(0)?.subtitle?.toIntOrNull() ?: 0
                    val sessions = items.getOrNull(1)?.subtitle?.toIntOrNull() ?: 0
                    val exercises = items.getOrNull(2)?.subtitle?.toIntOrNull() ?: 0
                    bindWeeklyProgress(
                        progressWeekly,
                        tvWeeklyGoalProgress,
                        tvWeeklyGoalHint,
                        sessions.coerceAtMost(5),
                        5,
                    )
                    updateStats(
                        streak = sessions,
                        weekDays = workouts,
                        totalHours = sessions,
                        completedCount = exercises,
                    )
                }
                .onFailure {
                    tvWeeklyGoalHint.text = it.message ?: "Nie udało się pobrać statystyk"
                }
        }
    }
}
