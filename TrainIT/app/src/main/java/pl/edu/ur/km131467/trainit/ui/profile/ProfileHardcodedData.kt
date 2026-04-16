package pl.edu.ur.km131467.trainit.ui.profile

import pl.edu.ur.km131467.trainit.R

/**
 * Wszystkie dane testowe (stub) ekranu profilu.
 *
 * Ułatwia późniejszą podmianę na dane z repozytorium bez przeszukiwania wielu klas.
 *
 * @see ProfileActivity
 */
object ProfileHardcodedData {

    /** Imię i nazwisko wyświetlane w nagłówku profilu. */
    val profileDisplayName: String = "Jan Kowalski"

    /** Tekst „członkostwa” pod nazwiskiem. */
    val memberSinceText: String = "Członek od marca 2024"

    /** Wartość liczbowa w kafelku „Treningów”. */
    val statWorkouts: String = "156"

    /** Wartość w kafelku sumy czasu (np. z sufiksem h). */
    val statHours: String = "48h"

    /** Wartość w kafelku serii dni. */
    val statStreak: String = "12"

    /**
     * Wartości wykresu słupkowego (godziny treningu) dla dni Pn–Nd.
     *
     * Wartość `0f` oznacza brak treningu w danym dniu. Maksimum skali osi Y w rendererze to 2 godziny.
     */
    val weeklyChartValues: List<Float> = listOf(0f, 1f, 1f, 1f, 1f, 0f, 0f)

    /** Etykiety dni pod wykresem. */
    val dayLabels: List<String> = listOf("Pn", "Wt", "\u015ar", "Cz", "Pt", "So", "Nd")

    /**
     * Rekord osobisty w pojedynczym \u0107wiczeniu.
     *
     * @property exercise nazwa \u0107wiczenia.
     * @property weight osiągnięty ciężar (tekst z jednostką).
     * @property date data ustanowienia rekordu.
     * @property reps opis powtórzeń.
     */
    data class PersonalRecord(
        val exercise: String,
        val weight: String,
        val date: String,
        val reps: String,
    )

    /** Lista rekordów wyświetlanych w sekcji profilu. */
    val personalRecords: List<PersonalRecord> = listOf(
        PersonalRecord("Wyciskanie sztangi", "100 kg", "14.03.2026", "5 powtórzeń"),
        PersonalRecord("Przysiad ze sztangą", "140 kg", "10.03.2026", "8 powtórzeń"),
    )

    /**
     * Pojedyncze osiągnięcie w siatce 3×2.
     *
     * @property icon zasób drawable ikony.
     * @property label tekst pod ikoną (może zawierać znaki nowej linii).
     * @property unlocked `true` jeśli odblokowane.
     */
    data class Achievement(
        val icon: Int,
        val label: String,
        val unlocked: Boolean,
    )

    /** Sześć osiągnięć: pierwsze trzy odblokowane, kolejne trzy zablokowane. */
    val achievements: List<Achievement> = listOf(
        Achievement(R.drawable.ic_fire, "Seria 7 dni", true),
        Achievement(R.drawable.ic_muscle, "50\ntreningów", true),
        Achievement(R.drawable.ic_trophy, "Mistrz Push\nDay", true),
        Achievement(R.drawable.ic_target, "100\ntreningów", false),
        Achievement(R.drawable.ic_star, "Seria 30 dni", false),
        Achievement(R.drawable.ic_muscle, "Wszystkie\nmięśnie", false),
    )

    /**
     * Wiersz sekcji podsumowania profilu.
     *
     * @property icon zasób drawable ikony.
     * @property label opis (lewa kolumna).
     * @property value wartość (prawa kolumna).
     */
    data class SummaryItem(
        val icon: Int,
        val label: String,
        val value: String,
    )

    /** Wiersze podsumowania na dole ekranu profilu. */
    val summaryItems: List<SummaryItem> = listOf(
        SummaryItem(R.drawable.ic_dumbbell, "Najczęstsze\nćwiczenie", "Wyciskanie\nsztangi"),
        SummaryItem(R.drawable.ic_clock, "\u015aredni czas treningu", "72 minuty"),
        SummaryItem(R.drawable.ic_fire, "Najdłuższa seria", "21 dni"),
        SummaryItem(R.drawable.ic_trending_up, "Trend tego miesiąca", "+15% więcej"),
    )
}
