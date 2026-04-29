package pl.edu.ur.km131467.trainit.ui.workouts

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.repository.FeatureRepository
import pl.edu.ur.km131467.trainit.ui.common.BottomNavHelper
import pl.edu.ur.km131467.trainit.ui.feature.FeatureModule
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
    }

    /** Obsługuje akcję FAB tworząc/usuwając plan przez backend i odświeżając listę. */
    private fun setupFab() {
        fabAddWorkout.setOnClickListener {
            lifecycleScope.launch {
                runCatching { featureRepository.runPrimaryAction(FeatureModule.ROLE_PANEL, sessionManager) }
                    .onSuccess {
                        Toast.makeText(this@WorkoutsActivity, it, Toast.LENGTH_SHORT).show()
                        loadWorkouts()
                    }
                    .onFailure {
                        Toast.makeText(this@WorkoutsActivity, it.message ?: "Błąd akcji planu", Toast.LENGTH_LONG)
                            .show()
                    }
            }
        }
    }

    /**
     * Podpina akcję pola wyszukiwania.
     *
     * Enter odświeża listę danych z backendu.
     */
    private fun setupSearchAction() {
        etSearch.setOnEditorActionListener { _, _, _ ->
            loadWorkouts()
            true
        }
        etSearch.setOnClickListener {
            Toast.makeText(this, "Wyszukiwanie lokalne będzie dodane w kolejnym etapie", Toast.LENGTH_SHORT).show()
        }
    }

    /** Ładuje listę planów z backendu i renderuje karty. */
    private fun loadWorkouts() {
        lifecycleScope.launch {
            runCatching { featureRepository.getItems(FeatureModule.ROLE_PANEL, sessionManager) }
                .onSuccess { items ->
                    WorkoutsCardsRenderer.populate(this@WorkoutsActivity, workoutCardsContainer, items) {
                        Toast.makeText(
                            this@WorkoutsActivity,
                            "Uruchamianie sesji przeniesione do modułu Sesje",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
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
}
