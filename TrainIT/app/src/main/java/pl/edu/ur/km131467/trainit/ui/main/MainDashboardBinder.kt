package pl.edu.ur.km131467.trainit.ui.main

import android.widget.TextView
import com.google.android.material.progressindicator.CircularProgressIndicator

/**
 * Wiąże widoki dashboardu (layout `activity_home`) z wartościami z [MainHardcodedData].
 *
 * @see MainHardcodedData
 */

/**
 * Ustawia okrągły wskaźnik postępu celu tygodniowego oraz powiązane teksty podpowiedzi.
 *
 * @param progressWeekly wskaźnik postępu (procent 0–100).
 * @param tvWeeklyGoalProgress tekst typu „x/y treningów”.
 * @param tvWeeklyGoalHint tekst zachęty lub potwierdzenia osiągnięcia celu.
 * @param completed liczba ukończonych treningów.
 * @param goal wartość docelowa (musi być > 0).
 */
fun bindWeeklyProgress(
    progressWeekly: CircularProgressIndicator,
    tvWeeklyGoalProgress: TextView,
    tvWeeklyGoalHint: TextView,
    completed: Int,
    goal: Int,
) {
    val safeGoal = goal.coerceAtLeast(1)
    val percent = (completed * 100) / safeGoal
    progressWeekly.progress = percent
    tvWeeklyGoalProgress.text = "$completed/$safeGoal treningów"
    val remaining = safeGoal - completed
    tvWeeklyGoalHint.text = if (remaining > 0) {
        "Jeszcze $remaining trening do celu!"
    } else {
        "Cel tygodniowy osiągnięty!"
    }
}

/**
 * Wypełnia wartości liczbowe w kafelkach statystyk dashboardu.
 *
 * @param tvStatStreak pole „Seria dni”.
 * @param tvStatWeek pole „Ten tydzień” (format „{n} dni”).
 * @param tvStatTotalHours pole statystyki sumy czasu (format liczby godzin z sufiksem h).
 * @param tvStatCompleted pole „Ukończone”.
 * @param stats paczka wartości statystyk.
 */
fun bindDashboardStats(
    tvStatStreak: TextView,
    tvStatWeek: TextView,
    tvStatTotalHours: TextView,
    tvStatCompleted: TextView,
    stats: MainHardcodedData.DashboardStats,
) {
    tvStatStreak.text = stats.streak.toString()
    tvStatWeek.text = "${stats.weekDays} dni"
    tvStatTotalHours.text = "${stats.totalHours}h"
    tvStatCompleted.text = stats.completedCount.toString()
}
