package pl.edu.ur.km131467.trainit.ui.workouts

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.ui.common.BottomNavHelper

/**
 * Aktywność ekranu planów treningowych.
 *
 * Lista kart jest budowana przez [WorkoutsCardsRenderer] z [WorkoutsHardcodedData].
 * Dolna nawigacja — [BottomNavHelper].
 *
 * @see WorkoutsHardcodedData
 * @see pl.edu.ur.km131467.trainit.MainActivity
 * @see pl.edu.ur.km131467.trainit.ui.profile.ProfileActivity
 */
class WorkoutsActivity : AppCompatActivity() {

    /** Pole wyszukiwania treningów (stub — bez filtrowania). */
    private lateinit var etSearch: EditText

    /** Kontener na karty treningów. */
    private lateinit var workoutCardsContainer: LinearLayout

    /** Dolna nawigacja (Home / Treningi / Profil). */
    private lateinit var bottomNavigation: BottomNavigationView

    /** FAB dodawania planu (stub). */
    private lateinit var fabAddWorkout: FloatingActionButton

    /**
     * Inicjalizuje widoki, listę kart i nawigację.
     *
     * @param savedInstanceState zapisany stan instancji (nieużywany).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workouts)

        initViews()
        WorkoutsCardsRenderer.populate(this, workoutCardsContainer, WorkoutsHardcodedData.workoutPlans)
        BottomNavHelper.setupBottomNav(bottomNavigation, this, R.id.nav_workouts)
        setupFab()
    }

    private fun initViews() {
        etSearch = findViewById(R.id.etSearch)
        workoutCardsContainer = findViewById(R.id.workoutCardsContainer)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        fabAddWorkout = findViewById(R.id.fabAddWorkout)
    }

    private fun setupFab() {
        fabAddWorkout.setOnClickListener {
            Toast.makeText(this, "Dodaj nowy trening (stub)", Toast.LENGTH_SHORT).show()
        }
    }
}
