package pl.edu.ur.km131467.trainit.ui.workouts

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.repository.FeatureRepository
import pl.edu.ur.km131467.trainit.ui.common.BottomNavHelper
import pl.edu.ur.km131467.trainit.ui.feature.FeatureModule
import pl.edu.ur.km131467.trainit.ui.feature.FeatureListItem
import pl.edu.ur.km131467.trainit.ui.login.LoginActivity

/**
 * Aktywność ekranu planów treningowych.
 *
 * Lista kart jest ładowana z backendu przez [FeatureRepository].
 * Dolna nawigacja — [BottomNavHelper].
 *
 * @see FeatureRepository
 * @see pl.edu.ur.km131467.trainit.MainActivity
 * @see pl.edu.ur.km131467.trainit.ui.profile.ProfileActivity
 */
class WorkoutsActivity : AppCompatActivity() {

    /** Pole wyszukiwania planów treningowych (filtrowanie lokalne). */
    private lateinit var etSearch: EditText

    /** Kontener na karty treningów. */
    private lateinit var workoutCardsContainer: LinearLayout

    /** Dolna nawigacja (Home / Treningi / Profil). */
    private lateinit var bottomNavigation: BottomNavigationView

    /** FAB dodawania planu treningowego. */
    private lateinit var fabAddWorkout: FloatingActionButton

    /** Menedżer sesji użytkownika. */
    private lateinit var sessionManager: SessionManager

    /** Repozytorium danych modułów (workouts/sessions). */
    private val featureRepository = FeatureRepository()

    /** Ostatnio pobrana lista planów treningowych (używana do lokalnego filtrowania). */
    private var allWorkouts: List<FeatureListItem> = emptyList()
    private var hasActiveSession: Boolean = false
    private var latestSession: FeatureListItem? = null
    private var activeSession: FeatureListItem? = null

    private lateinit var tvSessionOverviewTitle: TextView
    private lateinit var tvSessionOverviewSubtitle: TextView
    private val timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    /** Launcher ekranu dodawania treningu z odświeżeniem listy po zapisie. */
    private val addWorkoutLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            loadWorkouts()
        }
    }

    /**
     * Inicjalizuje widoki, listę kart i nawigację.
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
        setContentView(R.layout.activity_workouts)

        initViews()
        BottomNavHelper.setupBottomNav(bottomNavigation, this, R.id.nav_workouts)
        setupFab()
        setupSearchAction()
        loadWorkouts()
    }

    /** Inicjalizuje kontrolki ekranu listy treningów. */
    private fun initViews() {
        etSearch = findViewById(R.id.etSearch)
        workoutCardsContainer = findViewById(R.id.workoutCardsContainer)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        fabAddWorkout = findViewById(R.id.fabAddWorkout)
        tvSessionOverviewTitle = findViewById(R.id.tvSessionOverviewTitle)
        tvSessionOverviewSubtitle = findViewById(R.id.tvSessionOverviewSubtitle)
    }

    /** Obsługuje FAB otwierając formularz dodania nowego planu treningowego. */
    private fun setupFab() {
        fabAddWorkout.setOnClickListener {
            addWorkoutLauncher.launch(Intent(this, AddWorkoutActivity::class.java))
        }
    }

    /**
     * Podpina akcję pola wyszukiwania.
     *
     * Enter filtruje lokalnie pobraną listę planów.
     */
    private fun setupSearchAction() {
        etSearch.setOnEditorActionListener { _, _, _ ->
            applySearchFilter()
            true
        }
    }

    /** Ładuje listę planów z backendu i renderuje karty. */
    private fun loadWorkouts() {
        lifecycleScope.launch {
            runCatching {
                val workouts = featureRepository.getItems(FeatureModule.ROLE_PANEL, sessionManager)
                val sessions = featureRepository.getItems(FeatureModule.SESSIONS, sessionManager)
                workouts to sessions
            }
                .onSuccess { (workouts, sessions) ->
                    allWorkouts = workouts
                    latestSession = sessions.firstOrNull()
                    activeSession = sessions.firstOrNull { it.subtitle.contains("ZAPLANOWANE", ignoreCase = true) }
                    hasActiveSession = activeSession != null
                    renderSessionOverview()
                    applySearchFilter()
                }
                .onFailure { error ->
                    workoutCardsContainer.removeAllViews()
                    Toast.makeText(
                        this@WorkoutsActivity,
                        error.message ?: "Nie udało się pobrać planów treningowych",
                        Toast.LENGTH_LONG,
                    ).show()
                }
        }
    }

    /** Filtruje listę po nazwie i opisie planu, bez dodatkowych wywołań backendu. */
    private fun applySearchFilter() {
        val query = etSearch.text?.toString().orEmpty().trim()
        val filtered = if (query.isEmpty()) {
            allWorkouts
        } else {
            allWorkouts.filter {
                it.title.contains(query, ignoreCase = true) || it.subtitle.contains(query, ignoreCase = true)
            }
        }
        WorkoutsCardsRenderer.populate(
            this,
            workoutCardsContainer,
            filtered,
            hasActiveSession = hasActiveSession,
            onStartClicked = { workout -> onStartWorkoutClicked(workout) },
            onStopClicked = { onStopActiveSessionClicked() },
            onPlanClicked = { workout -> onEditWorkoutClicked(workout) },
            onDeleteClicked = { workout -> onDeleteWorkoutClicked(workout) },
        )
    }

    /** Otwiera formularz edycji planu. */
    private fun onEditWorkoutClicked(workout: FeatureListItem) {
        val workoutId = workout.id ?: run {
            Toast.makeText(this, "Brak identyfikatora planu", Toast.LENGTH_SHORT).show()
            return
        }
        addWorkoutLauncher.launch(
            Intent(this, AddWorkoutActivity::class.java).putExtra(AddWorkoutActivity.EXTRA_WORKOUT_ID, workoutId),
        )
    }

    /** Uruchamia sekundnik aktywnej sesji w bloku podsumowania nad listą planów. */
    override fun onStart() {
        super.onStart()
        if (hasActiveSession) startOverviewTimer()
    }

    /** Zatrzymuje sekundnik przy opuszczeniu ekranu (oszczędność zasobów). */
    override fun onStop() {
        super.onStop()
        stopOverviewTimer()
    }

    /** Uruchamia sesję treningową dla wybranego planu. */
    private fun onStartWorkoutClicked(workout: FeatureListItem) {
        val workoutId = workout.id
        if (workoutId == null) {
            Toast.makeText(this, "Brak identyfikatora planu", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            runCatching { featureRepository.startSessionForWorkout(sessionManager, workoutId) }
                .onSuccess {
                    sessionManager.setActiveSessionStartedAt(System.currentTimeMillis())
                    hasActiveSession = true
                    renderSessionOverview()
                    applySearchFilter()
                    loadWorkouts()
                    Toast.makeText(this@WorkoutsActivity, "Uruchomiono sesję treningową", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(
                        this@WorkoutsActivity,
                        it.message ?: "Nie udało się uruchomić sesji",
                        Toast.LENGTH_LONG,
                    ).show()
                }
        }
    }

    /** Potwierdza i usuwa wybrany plan treningowy użytkownika. */
    private fun onDeleteWorkoutClicked(workout: FeatureListItem) {
        val workoutId = workout.id
        if (workoutId == null) {
            Toast.makeText(this, "Brak identyfikatora planu", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Usuń plan")
            .setMessage("Czy na pewno chcesz usunąć plan \"${workout.title}\"?")
            .setNegativeButton("Anuluj", null)
            .setPositiveButton("Usuń") { _, _ ->
                lifecycleScope.launch {
                    runCatching { featureRepository.deleteWorkoutForUser(sessionManager, workoutId) }
                        .onSuccess {
                            Toast.makeText(this@WorkoutsActivity, "Plan usunięty", Toast.LENGTH_SHORT).show()
                            loadWorkouts()
                        }
                        .onFailure {
                            Toast.makeText(
                                this@WorkoutsActivity,
                                it.message ?: "Nie udało się usunąć planu",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                }
            }
            .show()
    }

    /** Kończy aktywną sesję treningową i umożliwia nowy start. */
    private fun onStopActiveSessionClicked() {
        if (!hasActiveSession) {
            Toast.makeText(this, "Brak aktywnej sesji", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Zakończ sesję")
            .setMessage("Zakończyć aktualną sesję treningową?")
            .setNegativeButton("Anuluj", null)
            .setPositiveButton("Zakończ") { _, _ ->
                lifecycleScope.launch {
                    runCatching { featureRepository.stopActiveSession(sessionManager) }
                        .onSuccess {
                            hasActiveSession = false
                            renderSessionOverview()
                            applySearchFilter()
                            loadWorkouts()
                            Toast.makeText(this@WorkoutsActivity, "Sesja zakończona", Toast.LENGTH_SHORT).show()
                        }
                        .onFailure {
                            Toast.makeText(
                                this@WorkoutsActivity,
                                it.message ?: "Nie udało się zakończyć sesji",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                }
            }
            .show()
    }

    /** Renderuje status aktywnej/ostatniej sesji nad listą planów. */
    private fun renderSessionOverview() {
        if (hasActiveSession) {
            tvSessionOverviewTitle.text = "Sesja aktywna"
            startOverviewTimer()
        } else {
            stopOverviewTimer()
            tvSessionOverviewTitle.text = "Brak aktywnej sesji"
            val last = latestSession
            tvSessionOverviewSubtitle.text = if (last != null) {
                "Ostatnia: ${last.title}"
            } else {
                "Rozpocznij trening, aby utworzyć pierwszą sesję."
            }
        }
    }

    /** Uruchamia sekundnik aktywnej sesji w bloku statusu (bez auto-stopu). */
    private fun startOverviewTimer() {
        val startedAt = sessionManager.getActiveSessionStartedAt() ?: run {
            tvSessionOverviewSubtitle.text = activeSession?.title ?: "Sesja w toku"
            return
        }
        stopOverviewTimer()
        timerRunnable = object : Runnable {
            override fun run() {
                val elapsedSeconds = ((System.currentTimeMillis() - startedAt) / 1000).coerceAtLeast(0)
                val hours = elapsedSeconds / 3600
                val minutes = (elapsedSeconds % 3600) / 60
                val seconds = elapsedSeconds % 60
                val timeStr = if (hours > 0) {
                    String.format("%d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format("%02d:%02d", minutes, seconds)
                }
                tvSessionOverviewSubtitle.text = "${activeSession?.title ?: "Sesja w toku"}  ·  $timeStr"
                timerHandler.postDelayed(this, 1000L)
            }
        }.also { timerHandler.post(it) }
    }

    private fun stopOverviewTimer() {
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
        timerRunnable = null
    }

}
