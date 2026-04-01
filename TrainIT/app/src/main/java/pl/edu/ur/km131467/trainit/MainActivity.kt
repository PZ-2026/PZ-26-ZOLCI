package pl.edu.ur.km131467.trainit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import pl.edu.ur.km131467.trainit.ui.login.LoginActivity
import pl.edu.ur.km131467.trainit.ui.profile.ProfileActivity
import pl.edu.ur.km131467.trainit.ui.workouts.WorkoutsActivity

/**
 * Główna aktywność aplikacji TrainIT pełniąca rolę dashboardu.
 *
 * Jest punktem wejścia aplikacji (launcher activity). Przy starcie sprawdza,
 * czy użytkownik jest zalogowany za pomocą [SharedPreferences][android.content.SharedPreferences].
 * Jeśli nie — przekierowuje do [LoginActivity]. Jeśli tak — wyświetla ekran
 * główny zawierający:
 * - nagłówek z logo i nazwą aplikacji,
 * - kartę celu tygodniowego z okrągłym wskaźnikiem postępu ([CircularProgressIndicator]),
 * - siatkę 2×2 kart statystyk (seria dni, aktywność tygodniowa, łączny czas, ukończone treningi),
 * - kartę ostatniej aktywności,
 * - dolną nawigację ([BottomNavigationView]) umożliwiającą przejście do pozostałych ekranów.
 *
 * Aktualnie dane statystyk są zakodowane na sztywno (stub).
 *
 * @see LoginActivity
 * @see WorkoutsActivity
 * @see ProfileActivity
 */
class MainActivity : AppCompatActivity() {

    /** Pole tekstowe z nazwą aplikacji "TrainIT" w nagłówku. */
    private lateinit var tvHeaderAppName: TextView

    /** Okrągły wskaźnik postępu celu tygodniowego. */
    private lateinit var progressWeekly: CircularProgressIndicator

    /** Tekst informujący o postępie celu tygodniowego, np. "4/5 treningów". */
    private lateinit var tvWeeklyGoalProgress: TextView

    /** Podpowiedź pod postępem celu, np. "Jeszcze 1 trening do celu!". */
    private lateinit var tvWeeklyGoalHint: TextView

    /** Wartość statystyki "Seria dni". */
    private lateinit var tvStatStreak: TextView

    /** Wartość statystyki "Ten tydzień". */
    private lateinit var tvStatWeek: TextView

    /** Wartość statystyki "Łącznie" (godziny). */
    private lateinit var tvStatTotalHours: TextView

    /** Wartość statystyki "Ukończone" (treningi). */
    private lateinit var tvStatCompleted: TextView

    /** Odnośnik "Zobacz wszystkie" w sekcji ostatniej aktywności. */
    private lateinit var tvSeeAll: TextView

    /** Dolna nawigacja (Home / Treningi / Profil). */
    private lateinit var bottomNavigation: BottomNavigationView

    /**
     * Metoda cyklu życia wywoływana przy tworzeniu aktywności.
     *
     * Sprawdza stan zalogowania użytkownika. Jeśli użytkownik nie jest
     * zalogowany, przekierowuje do [LoginActivity] i kończy bieżącą aktywność.
     * W przeciwnym razie inicjalizuje widoki dashboardu i konfiguruje
     * wszystkie sekcje ekranu.
     *
     * @param savedInstanceState zapisany stan instancji (nieużywany).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isUserLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_home)

        initViews()
        setupAppNameSpan()
        setupWeeklyProgress()
        setupBottomNavigation()
        setupClickListeners()
    }

    /**
     * Sprawdza, czy użytkownik jest zalogowany.
     *
     * Odczytuje flagę `is_logged_in` z pliku [SharedPreferences][android.content.SharedPreferences]
     * o nazwie `trainit_prefs`.
     *
     * @return `true` jeśli użytkownik jest zalogowany, `false` w przeciwnym razie.
     */
    private fun isUserLoggedIn(): Boolean {
        return getSharedPreferences("trainit_prefs", MODE_PRIVATE)
            .getBoolean("is_logged_in", false)
    }

    /**
     * Inicjalizuje referencje do wszystkich widoków layoutu [R.layout.activity_home].
     *
     * Wywoływana jednorazowo w [onCreate] po ustawieniu content view.
     */
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
     * Konfiguruje nazwę aplikacji "TrainIT" z kolorowym fragmentem "IT".
     *
     * Tworzy [SpannableString] i nakłada [ForegroundColorSpan] w kolorze
     * żółtym (#FFD600) na znaki od indeksu 5 do 7 ("IT"), dzięki czemu
     * wyróżniają się wizualnie w nagłówku.
     */
    private fun setupAppNameSpan() {
        val text = "TrainIT"
        val spannable = SpannableString(text)
        val yellowColor = Color.parseColor("#FFD600")
        spannable.setSpan(
            ForegroundColorSpan(yellowColor),
            5, 7,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvHeaderAppName.text = spannable
    }

    /**
     * Konfiguruje sekcję celu tygodniowego z danymi testowymi.
     *
     * Ustawia wartość wskaźnika postępu ([CircularProgressIndicator]),
     * tekst z liczbą ukończonych treningów względem celu oraz podpowiedź
     * informującą, ile treningów pozostało do osiągnięcia celu.
     *
     * Aktualnie wykorzystuje dane zakodowane na sztywno: 4 z 5 treningów (80%).
     */
    private fun setupWeeklyProgress() {
        val completed = 4
        val goal = 5
        val percent = (completed * 100) / goal

        progressWeekly.progress = percent
        tvWeeklyGoalProgress.text = "$completed/$goal treningów"

        val remaining = goal - completed
        tvWeeklyGoalHint.text = if (remaining > 0) {
            "Jeszcze $remaining trening do celu!"
        } else {
            "Cel tygodniowy osiągnięty!"
        }
    }

    /**
     * Konfiguruje dolną nawigację ([BottomNavigationView]).
     *
     * Ustawia zakładkę "Home" jako aktywną i definiuje listenery nawigacji:
     * - **Home** — bieżący ekran (brak akcji),
     * - **Treningi** — przejście do [WorkoutsActivity],
     * - **Profil** — przejście do [ProfileActivity].
     *
     * Przy przejściu do innej aktywności bieżąca jest zamykana ([finish]),
     * aby uniknąć gromadzenia się aktywności na stosie.
     */
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_workouts -> {
                    startActivity(Intent(this, WorkoutsActivity::class.java))
                    finish()
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
     * Konfiguruje listenery kliknięć dla elementów interaktywnych dashboardu.
     *
     * Obsługuje:
     * - kliknięcie "Zobacz wszystkie" — stub nawigacji do pełnej historii aktywności,
     * - kliknięcie karty ostatniej aktywności — stub nawigacji do szczegółów treningu.
     *
     * Obie akcje wyświetlają tymczasowy komunikat [Toast].
     */
    private fun setupClickListeners() {
        tvSeeAll.setOnClickListener {
            Toast.makeText(this, "Zobacz wszystkie (stub)", Toast.LENGTH_SHORT).show()
        }

        findViewById<android.view.View>(R.id.cardRecentActivity).setOnClickListener {
            Toast.makeText(this, "Szczegóły treningu (stub)", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Aktualizuje wartości kart statystyk na dashboardzie.
     *
     * Metoda publiczna przewidziana do wywołania po załadowaniu danych
     * z backendu. Aktualnie nieużywana — wartości domyślne pochodzą
     * bezpośrednio z layoutu XML.
     *
     * @param streak aktualna seria dni treningowych z rzędu.
     * @param weekDays liczba dni treningowych w bieżącym tygodniu.
     * @param totalHours łączna liczba godzin treningowych.
     * @param completedCount całkowita liczba ukończonych treningów.
     */
    fun updateStats(streak: Int, weekDays: Int, totalHours: Int, completedCount: Int) {
        tvStatStreak.text = streak.toString()
        tvStatWeek.text = "$weekDays dni"
        tvStatTotalHours.text = "${totalHours}h"
        tvStatCompleted.text = completedCount.toString()
    }
}
