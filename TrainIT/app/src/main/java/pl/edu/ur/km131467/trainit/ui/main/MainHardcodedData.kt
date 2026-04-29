package pl.edu.ur.km131467.trainit.ui.main

/**
 * Zestaw danych przykładowych dla dashboardu [pl.edu.ur.km131467.trainit.MainActivity].
 *
 * W przyszłości można zastąpić je danymi z repozytorium lub API bez zmiany układu ekranu.
 *
 * @see pl.edu.ur.km131467.trainit.MainActivity
 */
object MainHardcodedData {

    /** Liczba ukończonych treningów w bieżącym tygodniu (cel tygodniowy). */
    const val weeklyCompleted: Int = 4

    /** Docelowy tygodniowy limit treningów. */
    const val weeklyGoal: Int = 5

    /**
     * Wartości prezentowane w siatce statystyk 2×2 na dashboardzie.
     *
     * @property streak seria dni treningowych z rzędu.
     * @property weekDays liczba dni z treningiem w bieżącym tygodniu.
     * @property totalHours suma godzin treningów (wyświetlana z sufiksem h).
     * @property completedCount łączna liczba ukończonych treningów.
     */
    data class DashboardStats(
        val streak: Int,
        val weekDays: Int,
        val totalHours: Int,
        val completedCount: Int,
    )

    /** Domyślne statystyki zgodne z wartościami startowymi w layoucie `activity_home`. */
    val dashboardStats: DashboardStats = DashboardStats(
        streak = 12,
        weekDays = 4,
        totalHours = 48,
        completedCount = 156,
    )
}
