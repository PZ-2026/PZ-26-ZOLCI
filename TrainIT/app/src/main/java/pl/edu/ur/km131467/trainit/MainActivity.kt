package pl.edu.ur.km131467.trainit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.repository.FeatureRepository
import pl.edu.ur.km131467.trainit.ui.common.BottomNavHelper
import pl.edu.ur.km131467.trainit.ui.common.applyAppNameSpan
import pl.edu.ur.km131467.trainit.ui.feature.FeatureListItem
import pl.edu.ur.km131467.trainit.ui.feature.FeatureModule
import pl.edu.ur.km131467.trainit.ui.feature.SessionsActivity
import pl.edu.ur.km131467.trainit.ui.login.LoginActivity
import pl.edu.ur.km131467.trainit.ui.main.bindDashboardStats
import pl.edu.ur.km131467.trainit.ui.main.bindWeeklyProgress
import pl.edu.ur.km131467.trainit.ui.notifications.ReminderScheduler
import pl.edu.ur.km131467.trainit.ui.notifications.TrainingReminderNotifier

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

    /** Tytuł karty ostatniej aktywności. */
    private lateinit var tvActivityTitle: TextView

    /** Meta status ostatniej aktywności. */
    private lateinit var tvActivityDate: TextView

    /** Czas trwania ostatniej aktywności. */
    private lateinit var tvActivityDuration: TextView

    /** Licznik sesji w sekcji ostatniej aktywności. */
    private lateinit var tvExerciseCount: TextView

    /** Kontener kropek postępu aktywności. */
    private lateinit var dotsContainer: LinearLayout

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
        ReminderScheduler.ensureScheduled(this)

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
        tvActivityTitle = findViewById(R.id.tvActivityTitle)
        tvActivityDate = findViewById(R.id.tvActivityDate)
        tvActivityDuration = findViewById(R.id.tvActivityDuration)
        tvExerciseCount = findViewById(R.id.tvExerciseCount)
        dotsContainer = findViewById(R.id.dotsContainer)
    }

    /**
     * Konfiguruje przejścia z sekcji aktywności do ekranów szczegółowych.
     *
     * Link „Zobacz wszystkie” i karta aktywności otwierają historię sesji.
     */
    private fun setupClickListeners() {
        tvSeeAll.setOnClickListener {
            startActivity(Intent(this, SessionsActivity::class.java))
        }
        findViewById<android.view.View>(R.id.cardRecentActivity).setOnClickListener {
            startActivity(Intent(this, SessionsActivity::class.java))
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
            runCatching {
                val statsDeferred = async { featureRepository.getItems(FeatureModule.STATISTICS, sessionManager) }
                val sessionsDeferred = async { featureRepository.getItems(FeatureModule.SESSIONS, sessionManager) }
                val settingsDeferred = async { featureRepository.getItems(FeatureModule.SETTINGS, sessionManager) }
                val notificationsDeferred = async { featureRepository.getItems(FeatureModule.NOTIFICATIONS, sessionManager) }
                DashboardPayload(
                    stats = statsDeferred.await(),
                    sessions = sessionsDeferred.await(),
                    settings = settingsDeferred.await(),
                    notifications = notificationsDeferred.await(),
                )
            }
                .onSuccess { payload ->
                    val weekSessions = statValue(payload.stats, "Sesje w tym tygodniu")
                    val exercises = statValue(payload.stats, "Liczba ćwiczeń")
                    val completedSessions = statValue(payload.stats, "Sesje ukończone")
                    val totalSessions = payload.sessions.size
                    val totalMinutes = statValue(payload.stats, "Łączny czas treningów (min)")
                    val totalHours = totalMinutes / 60
                    val streak = calculateStreakDays(payload.sessions)
                    val weeklyGoal = payload.settings
                        .firstOrNull { it.title.equals("Cel tygodniowy", ignoreCase = true) }
                        ?.subtitle
                        ?.toIntOrNull()
                        ?.coerceAtLeast(1)
                        ?: 5
                    bindWeeklyProgress(
                        progressWeekly,
                        tvWeeklyGoalProgress,
                        tvWeeklyGoalHint,
                        weekSessions.coerceAtMost(weeklyGoal),
                        weeklyGoal,
                    )
                    updateStats(
                        streak = streak,
                        weekDays = weekSessions,
                        totalHours = totalHours,
                        completedCount = exercises,
                    )
                    bindRecentActivity(payload.sessions, completedSessions, totalSessions)
                    notifyGoalReachedIfNeeded(weekSessions, weeklyGoal)
                }
                .onFailure {
                    tvWeeklyGoalHint.text = it.message ?: "Nie udało się pobrać statystyk"
                }
        }
    }

    /** Odczytuje liczbową wartość statystyki po jej tytule. */
    private fun statValue(items: List<FeatureListItem>, title: String): Int {
        return items.firstOrNull { it.title.equals(title, ignoreCase = true) }
            ?.subtitle
            ?.toIntOrNull()
            ?: 0
    }

    /** Ekstrahuje czas trwania sesji w minutach z pola subtitle. */
    private fun parseDurationMinutes(subtitle: String): Int? {
        val match = Regex("czas:\\s*(\\d+)\\s*min", RegexOption.IGNORE_CASE).find(subtitle) ?: return null
        return match.groupValues.getOrNull(1)?.toIntOrNull()
    }

    /** Oblicza serię dni z ukończonym treningiem na podstawie historii sesji. */
    private fun calculateStreakDays(sessions: List<FeatureListItem>): Int {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val completedDates = sessions
            .asSequence()
            .filter { it.subtitle.contains("UKOŃCZONE", ignoreCase = true) }
            .mapNotNull { session ->
                Regex("data:\\s*(\\d{4}-\\d{2}-\\d{2})", RegexOption.IGNORE_CASE)
                    .find(session.subtitle)
                    ?.groupValues
                    ?.getOrNull(1)
                    ?.let { runCatching { LocalDate.parse(it, formatter) }.getOrNull() }
            }
            .toSet()
        if (completedDates.isEmpty()) return 0
        var cursor = LocalDate.now()
        if (!completedDates.contains(cursor)) {
            cursor = cursor.minusDays(1)
        }
        var streak = 0
        while (completedDates.contains(cursor)) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    /** Podpina realne dane sesji pod kartę "Ostatnia aktywność". */
    private fun bindRecentActivity(sessions: List<FeatureListItem>, completedSessions: Int, totalSessions: Int) {
        val latest = sessions.firstOrNull()
        if (latest == null) {
            tvActivityTitle.text = "Brak aktywności"
            tvActivityDate.text = "Brak zapisanych sesji"
            tvActivityDuration.text = "-"
            tvExerciseCount.text = "0 sesji"
            updateDots(0, 8)
            return
        }

        val status = Regex("Status:\\s*([^,]+)", RegexOption.IGNORE_CASE)
            .find(latest.subtitle)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            ?: "N/D"
        val duration = parseDurationMinutes(latest.subtitle)
        tvActivityTitle.text = latest.title
        tvActivityDate.text = "Status: $status"
        tvActivityDuration.text = if (duration != null) "${duration} min" else "czas: -"
        tvExerciseCount.text = "Sesje ukończone: $completedSessions/$totalSessions"
        updateDots(completedSessions.coerceAtMost(8), 8)
    }

    private fun notifyGoalReachedIfNeeded(weekSessions: Int, weeklyGoal: Int) {
        if (weekSessions >= weeklyGoal && sessionManager.shouldShowGoalReachedThisWeek()) {
            TrainingReminderNotifier.showReminder(
                this,
                "Cel tygodniowy osiągnięty",
                "Brawo! Wykonałeś już $weekSessions/$weeklyGoal sesji w tym tygodniu.",
            )
            sessionManager.markGoalReachedThisWeek()
        }
    }

    private data class DashboardPayload(
        val stats: List<FeatureListItem>,
        val sessions: List<FeatureListItem>,
        val settings: List<FeatureListItem>,
        val notifications: List<FeatureListItem>,
    )

    /** Aktualizuje pasek kropek postępu aktywności. */
    private fun updateDots(completed: Int, total: Int) {
        val safeTotal = total.coerceAtLeast(1)
        for (index in 0 until dotsContainer.childCount) {
            val dot = dotsContainer.getChildAt(index)
            if (index < safeTotal) {
                dot.visibility = View.VISIBLE
                dot.setBackgroundColor(resources.getColor(if (index < completed) R.color.dot_completed else R.color.dot_empty, theme))
            } else {
                dot.visibility = View.GONE
            }
        }
    }
}
