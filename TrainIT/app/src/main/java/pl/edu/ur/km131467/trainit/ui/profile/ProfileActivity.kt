package pl.edu.ur.km131467.trainit.ui.profile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import pl.edu.ur.km131467.trainit.MainActivity
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.ui.login.LoginActivity
import pl.edu.ur.km131467.trainit.ui.workouts.WorkoutsActivity

/**
 * Aktywność ekranu profilu użytkownika.
 *
 * Wyświetla stronę profilu zawierającą następujące sekcje (wszystkie
 * z danymi zakodowanymi na sztywno):
 * - **Nagłówek** — avatar, imię użytkownika, data dołączenia,
 * - **Statystyki** — trzy pill-kafelki (treningów, łącznie godzin, seria dni),
 * - **Aktywność tygodniowa** — wykres słupkowy (Pn–Nd) budowany programowo,
 * - **Rekordy osobiste** — karty z najlepszymi wynikami ćwiczeń,
 * - **Osiągnięcia** — siatka 3×2 badge'ów (odblokowane i zablokowane),
 * - **Podsumowanie** — lista kluczowych statystyk (najczęstsze ćwiczenie,
 *   średni czas, najdłuższa seria, trend miesiąca),
 * - **Przycisk wylogowania** — czyści sesję i nawiguje do [LoginActivity].
 *
 * Dolna nawigacja ([BottomNavigationView]) umożliwia przejście do
 * [MainActivity] (Home) i [WorkoutsActivity] (Treningi).
 *
 * @see MainActivity
 * @see WorkoutsActivity
 * @see LoginActivity
 */
class ProfileActivity : AppCompatActivity() {

    /** Dolna nawigacja (Home / Treningi / Profil). */
    private lateinit var bottomNavigation: BottomNavigationView

    /** Przycisk "Wyloguj się". */
    private lateinit var btnLogout: MaterialButton

    /** Kontener na słupki wykresu aktywności tygodniowej. */
    private lateinit var chartBarsContainer: LinearLayout

    /** Kontener na etykiety dni tygodnia pod wykresem (Pn, Wt, ..., Nd). */
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
     * Dane testowe wykresu aktywności tygodniowej.
     *
     * Każda wartość odpowiada liczbie godzin treningu w danym dniu tygodnia
     * (Pn–Nd). Wartość `0f` oznacza brak treningu.
     */
    private val weeklyData = listOf(0f, 1f, 1f, 1f, 1f, 0f, 0f)

    /** Etykiety dni tygodnia wyświetlane pod wykresem słupkowym. */
    private val dayLabels = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

    /**
     * Model danych rekordu osobistego.
     *
     * @property exercise nazwa ćwiczenia, np. "Wyciskanie sztangi".
     * @property weight osiągnięty ciężar, np. "100 kg".
     * @property date data ustanowienia rekordu w formacie dd.MM.yyyy.
     * @property reps informacja o liczbie powtórzeń, np. "5 powtórzeń".
     */
    data class PersonalRecord(
        val exercise: String,
        val weight: String,
        val date: String,
        val reps: String
    )

    /** Lista rekordów osobistych zakodowanych na sztywno. */
    private val records = listOf(
        PersonalRecord("Wyciskanie sztangi", "100 kg", "14.03.2026", "5 powtórzeń"),
        PersonalRecord("Przysiad ze sztangą", "140 kg", "10.03.2026", "8 powtórzeń")
    )

    /**
     * Model danych pojedynczego osiągnięcia.
     *
     * @property icon identyfikator zasobu drawable ikony osiągnięcia.
     * @property label opis osiągnięcia wyświetlany pod ikoną (może zawierać `\n`).
     * @property unlocked `true` jeśli osiągnięcie zostało odblokowane, `false` w przeciwnym razie.
     */
    data class Achievement(
        val icon: Int,
        val label: String,
        val unlocked: Boolean
    )

    /**
     * Lista osiągnięć zakodowanych na sztywno.
     *
     * Pierwsze trzy osiągnięcia są odblokowane (wyświetlane w 1. rzędzie),
     * pozostałe trzy są zablokowane (wyświetlane w 2. rzędzie z wyszarzeniem).
     */
    private val achievements = listOf(
        Achievement(R.drawable.ic_fire, "Seria 7 dni", true),
        Achievement(R.drawable.ic_muscle, "50\ntreningów", true),
        Achievement(R.drawable.ic_trophy, "Mistrz Push\nDay", true),
        Achievement(R.drawable.ic_target, "100\ntreningów", false),
        Achievement(R.drawable.ic_star, "Seria 30 dni", false),
        Achievement(R.drawable.ic_muscle, "Wszystkie\nmięśnie", false)
    )

    /**
     * Model danych wiersza podsumowania.
     *
     * @property icon identyfikator zasobu drawable ikony wiersza.
     * @property label opis statystyki (lewa kolumna).
     * @property value wartość statystyki (prawa kolumna, pogrubiona).
     */
    data class SummaryItem(
        val icon: Int,
        val label: String,
        val value: String
    )

    /** Lista wierszy podsumowania zakodowanych na sztywno. */
    private val summaryItems = listOf(
        SummaryItem(R.drawable.ic_dumbbell, "Najczęstsze\nćwiczenie", "Wyciskanie\nsztangi"),
        SummaryItem(R.drawable.ic_clock, "Średni czas treningu", "72 minuty"),
        SummaryItem(R.drawable.ic_fire, "Najdłuższa seria", "21 dni"),
        SummaryItem(R.drawable.ic_trending_up, "Trend tego miesiąca", "+15% więcej")
    )

    /**
     * Metoda cyklu życia wywoływana przy tworzeniu aktywności.
     *
     * Inicjalizuje layout, wiąże widoki i konfiguruje wszystkie sekcje
     * ekranu profilu: wykres, rekordy, osiągnięcia, podsumowanie,
     * dolną nawigację oraz przycisk wylogowania.
     *
     * @param savedInstanceState zapisany stan instancji (nieużywany).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        setupChart()
        setupRecords()
        setupAchievements()
        setupSummary()
        setupBottomNavigation()
        setupLogout()
    }

    /**
     * Inicjalizuje referencje do wszystkich widoków layoutu [R.layout.activity_profile].
     */
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
     * Buduje wykres słupkowy aktywności tygodniowej programowo.
     *
     * Dla każdego dnia tygodnia (na podstawie [weeklyData]) tworzy słupek
     * o wysokości proporcjonalnej do wartości (maksimum osi Y = 2 godziny).
     * Słupki z wartością > 0 otrzymują żółte tło ([R.drawable.bg_bar_chart_bar]),
     * a słupki z wartością 0 — ciemne tło ([R.drawable.bg_bar_chart_empty])
     * o minimalnej wysokości 2dp.
     *
     * Pod wykresem dodawane są etykiety dni tygodnia z listy [dayLabels].
     */
    private fun setupChart() {
        val maxValue = 2f
        val barMaxHeightDp = 120

        for (i in weeklyData.indices) {
            val value = weeklyData[i]

            val barWrapper = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            }

            val ratio = (value / maxValue).coerceIn(0f, 1f)
            val barHeightDp = (barMaxHeightDp * ratio).toInt()
            val barHeightPx = dpToPx(barHeightDp)

            val bar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(18), if (barHeightPx > 0) barHeightPx else dpToPx(2))
                setBackgroundResource(
                    if (value > 0) R.drawable.bg_bar_chart_bar else R.drawable.bg_bar_chart_empty
                )
            }

            barWrapper.addView(bar)
            chartBarsContainer.addView(barWrapper)
        }

        for (label in dayLabels) {
            val tv = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = label
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.text_secondary))
                gravity = Gravity.CENTER
            }
            chartDaysContainer.addView(tv)
        }
    }

    /**
     * Buduje karty rekordów osobistych programowo na podstawie listy [records].
     *
     * Każda karta zawiera dwa wiersze:
     * - **górny** — nazwa ćwiczenia (lewa strona) i ciężar (prawa strona),
     * - **dolny** — data rekordu (lewa strona) i liczba powtórzeń (prawa strona).
     *
     * Karty używają tła [R.drawable.bg_card_dark] i są dodawane
     * do [recordsContainer] z marginesem dolnym.
     */
    private fun setupRecords() {
        for (record in records) {
            val card = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dpToPx(10) }
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_card_dark)
                setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
            }

            val topRow = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvExercise = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = record.exercise
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColor(Color.WHITE)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val tvWeight = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = record.weight
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(Color.WHITE)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            topRow.addView(tvExercise)
            topRow.addView(tvWeight)

            val bottomRow = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dpToPx(4) }
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvDate = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = record.date
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.text_secondary))
            }

            val tvReps = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = record.reps
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.text_secondary))
            }

            bottomRow.addView(tvDate)
            bottomRow.addView(tvReps)

            card.addView(topRow)
            card.addView(bottomRow)
            recordsContainer.addView(card)
        }
    }

    /**
     * Buduje siatkę osiągnięć w dwóch rzędach.
     *
     * Filtruje listę [achievements] na odblokowane i zablokowane, a następnie
     * tworzy badge'e za pomocą [createAchievementBadge] i dodaje je odpowiednio
     * do [achievementsRow1] (odblokowane) i [achievementsRow2] (zablokowane).
     */
    private fun setupAchievements() {
        val unlockedAchievements = achievements.filter { it.unlocked }
        val lockedAchievements = achievements.filter { !it.unlocked }

        for (achievement in unlockedAchievements) {
            achievementsRow1.addView(createAchievementBadge(achievement))
        }

        for (achievement in lockedAchievements) {
            achievementsRow2.addView(createAchievementBadge(achievement))
        }
    }

    /**
     * Tworzy pojedynczy badge osiągnięcia jako [LinearLayout].
     *
     * Badge ma stałą wysokość 100dp, aby zapewnić jednolity rozmiar w siatce.
     * Zawiera ikonę (28×28dp) oraz etykietę (2 linie, wycentrowana).
     *
     * Dla osiągnięć odblokowanych:
     * - tło [R.drawable.bg_achievement_unlocked] (półprzezroczysty żółty),
     * - ikona w pełnym kolorze,
     * - biały tekst etykiety.
     *
     * Dla osiągnięć zablokowanych:
     * - tło [R.drawable.bg_achievement_locked] (ciemne),
     * - ikona z obniżoną przezroczystością (40%) i szarym filtrem kolorów,
     * - szary tekst etykiety.
     *
     * @param achievement dane osiągnięcia do wyświetlenia.
     * @return skonfigurowany [LinearLayout] reprezentujący badge osiągnięcia.
     */
    private fun createAchievementBadge(achievement: Achievement): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(100), 1f).apply {
                marginEnd = dpToPx(8)
            }
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundResource(
                if (achievement.unlocked) R.drawable.bg_achievement_unlocked
                else R.drawable.bg_achievement_locked
            )
            setPadding(dpToPx(8), dpToPx(14), dpToPx(8), dpToPx(14))

            val icon = ImageView(this@ProfileActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(28), dpToPx(28))
                setImageResource(achievement.icon)
                if (!achievement.unlocked) {
                    alpha = 0.4f
                    setColorFilter(
                        ContextCompat.getColor(this@ProfileActivity, R.color.achievement_locked_icon),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
            }
            addView(icon)

            val label = TextView(this@ProfileActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dpToPx(6) }
                text = achievement.label
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                minLines = 2
                maxLines = 2
                setTextColor(
                    if (achievement.unlocked) Color.WHITE
                    else ContextCompat.getColor(this@ProfileActivity, R.color.text_secondary)
                )
                gravity = Gravity.CENTER
            }
            addView(label)
        }
    }

    /**
     * Buduje sekcję podsumowania na podstawie listy [summaryItems].
     *
     * Każdy wiersz podsumowania zawiera:
     * - ikonę (20×20dp) po lewej stronie,
     * - etykietę opisową (szary tekst, wypełnia dostępną przestrzeń),
     * - wartość statystyki (biały pogrubiony tekst, wyrównany do prawej).
     *
     * Wiersze są dodawane do [summaryContainer] z marginesem dolnym 14dp.
     */
    private fun setupSummary() {
        for (item in summaryItems) {
            val row = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dpToPx(14) }
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val icon = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(20), dpToPx(20))
                setImageResource(item.icon)
            }

            val label = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = dpToPx(12)
                }
                text = item.label
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.text_secondary))
            }

            val value = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = item.value
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(Color.WHITE)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                gravity = Gravity.END
            }

            row.addView(icon)
            row.addView(label)
            row.addView(value)
            summaryContainer.addView(row)
        }
    }

    /**
     * Konfiguruje dolną nawigację ([BottomNavigationView]).
     *
     * Ustawia zakładkę "Profil" jako aktywną i definiuje listenery nawigacji:
     * - **Home** — przejście do [MainActivity],
     * - **Treningi** — przejście do [WorkoutsActivity],
     * - **Profil** — bieżący ekran (brak akcji).
     *
     * Przy przejściu do innej aktywności bieżąca jest zamykana ([finish]),
     * aby uniknąć gromadzenia się aktywności na stosie.
     */
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_profile

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_workouts -> {
                    startActivity(Intent(this, WorkoutsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Konfiguruje przycisk wylogowania.
     *
     * Po kliknięciu:
     * 1. Ustawia flagę `is_logged_in = false` w [SharedPreferences][android.content.SharedPreferences],
     * 2. Uruchamia [LoginActivity] z flagami [Intent.FLAG_ACTIVITY_NEW_TASK]
     *    i [Intent.FLAG_ACTIVITY_CLEAR_TASK], co czyści cały stos aktywności,
     * 3. Kończy bieżącą aktywność.
     *
     * Dzięki czyszczeniu stosu użytkownik nie może wrócić przyciskiem "wstecz"
     * do ekranów wymagających zalogowania.
     */
    private fun setupLogout() {
        btnLogout.setOnClickListener {
            getSharedPreferences("trainit_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("is_logged_in", false)
                .apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    /**
     * Konwertuje wartość z jednostek dp (density-independent pixels) na piksele (px).
     *
     * Wykorzystuje [TypedValue.applyDimension] z metrykami wyświetlacza
     * bieżącego urządzenia do przeliczenia wartości.
     *
     * @param dp wartość w dp do przeliczenia.
     * @return odpowiadająca wartość w pikselach.
     */
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
