package pl.edu.ur.km131467.trainit.ui.workouts

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pl.edu.ur.km131467.trainit.MainActivity
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.ui.profile.ProfileActivity

/**
 * Aktywność ekranu planów treningowych.
 *
 * Wyświetla listę dostępnych planów treningowych w formie przewijalnych
 * kart. Każda karta zawiera:
 * - nazwę planu treningowego,
 * - badge poziomu trudności (kolorystycznie zróżnicowany),
 * - informację o ostatnim wykonaniu,
 * - liczbę ćwiczeń i szacowany czas trwania,
 * - przycisk "Rozpocznij trening".
 *
 * Ekran zawiera również pole wyszukiwania (stub — bez filtrowania),
 * przycisk [FloatingActionButton] do dodawania nowych planów (stub)
 * oraz dolną nawigację ([BottomNavigationView]).
 *
 * Aktualnie wszystkie dane treningów są zakodowane na sztywno.
 *
 * @see MainActivity
 * @see ProfileActivity
 */
class WorkoutsActivity : AppCompatActivity() {

    /** Pole wyszukiwania treningów (aktualnie bez implementacji filtrowania). */
    private lateinit var etSearch: EditText

    /** Kontener [LinearLayout], do którego dynamicznie dodawane są karty treningów. */
    private lateinit var workoutCardsContainer: LinearLayout

    /** Dolna nawigacja (Home / Treningi / Profil). */
    private lateinit var bottomNavigation: BottomNavigationView

    /** Przycisk pływający (FAB) do dodawania nowego planu treningowego. */
    private lateinit var fabAddWorkout: FloatingActionButton

    /**
     * Model danych pojedynczego planu treningowego.
     *
     * @property name nazwa planu, np. "Push Day - Klatka & Triceps".
     * @property difficulty etykieta poziomu trudności, np. "Średni", "Trudny".
     * @property difficultyColor kolor tekstu badge'a trudności jako wartość [Int] (ARGB).
     * @property difficultyBgRes identyfikator zasobu drawable tła badge'a trudności.
     * @property lastPerformed informacja o ostatnim wykonaniu, np. "Ostatnio: 2 dni temu".
     * @property exerciseCount liczba ćwiczeń w planie.
     * @property duration szacowany czas trwania treningu, np. "60-75 min".
     */
    data class WorkoutPlan(
        val name: String,
        val difficulty: String,
        val difficultyColor: Int,
        val difficultyBgRes: Int,
        val lastPerformed: String,
        val exerciseCount: Int,
        val duration: String
    )

    /**
     * Lista planów treningowych zakodowanych na sztywno.
     *
     * Zawiera trzy przykładowe plany o różnych poziomach trudności:
     * - Push Day (średni) — 8 ćwiczeń, 60-75 min,
     * - Pull Day (trudny) — 9 ćwiczeń, 70-85 min,
     * - Leg Day (bardzo trudny) — 10 ćwiczeń, 80-90 min.
     */
    private val workoutPlans = listOf(
        WorkoutPlan(
            name = "Push Day - Klatka & Triceps",
            difficulty = "Średni",
            difficultyColor = Color.parseColor("#FFD600"),
            difficultyBgRes = R.drawable.bg_badge_medium,
            lastPerformed = "Ostatnio: 2 dni temu",
            exerciseCount = 8,
            duration = "60-75 min"
        ),
        WorkoutPlan(
            name = "Pull Day - Plecy & Biceps",
            difficulty = "Trudny",
            difficultyColor = Color.parseColor("#FF6D00"),
            difficultyBgRes = R.drawable.bg_badge_hard,
            lastPerformed = "Ostatnio: 4 dni temu",
            exerciseCount = 9,
            duration = "70-85 min"
        ),
        WorkoutPlan(
            name = "Leg Day - Nogi & Pośladki",
            difficulty = "Bardzo trudny",
            difficultyColor = Color.parseColor("#F44336"),
            difficultyBgRes = R.drawable.bg_badge_very_hard,
            lastPerformed = "Ostatnio: Wczoraj",
            exerciseCount = 10,
            duration = "80-90 min"
        )
    )

    /**
     * Metoda cyklu życia wywoływana przy tworzeniu aktywności.
     *
     * Inicjalizuje layout, wiąże widoki, wypełnia listę kart treningów
     * danymi testowymi, konfiguruje dolną nawigację oraz przycisk FAB.
     *
     * @param savedInstanceState zapisany stan instancji (nieużywany).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workouts)

        initViews()
        populateWorkoutCards()
        setupBottomNavigation()
        setupFab()
    }

    /**
     * Inicjalizuje referencje do wszystkich widoków layoutu [R.layout.activity_workouts].
     */
    private fun initViews() {
        etSearch = findViewById(R.id.etSearch)
        workoutCardsContainer = findViewById(R.id.workoutCardsContainer)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        fabAddWorkout = findViewById(R.id.fabAddWorkout)
    }

    /**
     * Tworzy i dodaje karty treningów do kontenera na podstawie listy [workoutPlans].
     *
     * Dla każdego planu treningowego:
     * 1. Influje layout [R.layout.item_workout_card],
     * 2. Wiąże dane modelu z widokami karty (nazwa, badge trudności, ostatnie
     *    wykonanie, liczba ćwiczeń, czas trwania),
     * 3. Ustawia listener na przycisku "Rozpocznij trening" (stub — wyświetla [Toast]),
     * 4. Dodaje kartę do [workoutCardsContainer].
     */
    private fun populateWorkoutCards() {
        val inflater = LayoutInflater.from(this)

        for (plan in workoutPlans) {
            val cardView = inflater.inflate(R.layout.item_workout_card, workoutCardsContainer, false)

            cardView.findViewById<TextView>(R.id.tvWorkoutName).text = plan.name

            val badgeView = cardView.findViewById<TextView>(R.id.tvDifficultyBadge)
            badgeView.text = plan.difficulty
            badgeView.setTextColor(plan.difficultyColor)
            badgeView.setBackgroundResource(plan.difficultyBgRes)

            cardView.findViewById<TextView>(R.id.tvLastPerformed).text = plan.lastPerformed
            cardView.findViewById<TextView>(R.id.tvExerciseCount).text = "${plan.exerciseCount} ćwiczeń"
            cardView.findViewById<TextView>(R.id.tvDuration).text = plan.duration

            cardView.findViewById<MaterialButton>(R.id.btnStartWorkout).setOnClickListener {
                Toast.makeText(
                    this,
                    "Rozpoczynam: ${plan.name} (stub)",
                    Toast.LENGTH_SHORT
                ).show()
            }

            workoutCardsContainer.addView(cardView)
        }
    }

    /**
     * Konfiguruje dolną nawigację ([BottomNavigationView]).
     *
     * Ustawia zakładkę "Treningi" jako aktywną i definiuje listenery nawigacji:
     * - **Home** — przejście do [MainActivity],
     * - **Treningi** — bieżący ekran (brak akcji),
     * - **Profil** — przejście do [ProfileActivity].
     *
     * Przy przejściu do innej aktywności bieżąca jest zamykana ([finish]),
     * aby uniknąć gromadzenia się aktywności na stosie.
     */
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_workouts

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_workouts -> {
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Konfiguruje przycisk pływający (FAB) do dodawania nowego planu treningowego.
     *
     * Funkcjonalność nie jest jeszcze zaimplementowana — kliknięcie wyświetla
     * jedynie tymczasowy komunikat [Toast].
     */
    private fun setupFab() {
        fabAddWorkout.setOnClickListener {
            Toast.makeText(this, "Dodaj nowy trening (stub)", Toast.LENGTH_SHORT).show()
        }
    }
}
