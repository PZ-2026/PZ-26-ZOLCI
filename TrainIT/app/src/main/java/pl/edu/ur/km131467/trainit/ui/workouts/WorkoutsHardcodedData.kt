package pl.edu.ur.km131467.trainit.ui.workouts

import android.graphics.Color
import pl.edu.ur.km131467.trainit.R

/**
 * Dane testowe listy planów treningowych na ekranie [WorkoutsActivity].
 *
 * @see WorkoutsActivity
 */
object WorkoutsHardcodedData {

    /**
     * Model karty planu treningowego.
     *
     * @property name tytuł planu.
     * @property difficulty etykieta trudności.
     * @property difficultyColor kolor tekstu badge'a (ARGB).
     * @property difficultyBgRes drawable tła badge'a.
     * @property lastPerformed tekst ostatniego wykonania.
     * @property exerciseCount liczba \u0107wicze\u0144.
     * @property duration szacowany czas trwania (tekst).
     */
    data class WorkoutPlan(
        val name: String,
        val difficulty: String,
        val difficultyColor: Int,
        val difficultyBgRes: Int,
        val lastPerformed: String,
        val exerciseCount: Int,
        val duration: String,
    )

    /** Trzy przykładowe plany z różnym poziomem trudności. */
    val workoutPlans: List<WorkoutPlan> = listOf(
        WorkoutPlan(
            name = "Push Day - Klatka & Triceps",
            difficulty = "\u015aredni",
            difficultyColor = Color.parseColor("#FFD600"),
            difficultyBgRes = R.drawable.bg_badge_medium,
            lastPerformed = "Ostatnio: 2 dni temu",
            exerciseCount = 8,
            duration = "60-75 min",
        ),
        WorkoutPlan(
            name = "Pull Day - Plecy & Biceps",
            difficulty = "Trudny",
            difficultyColor = Color.parseColor("#FF6D00"),
            difficultyBgRes = R.drawable.bg_badge_hard,
            lastPerformed = "Ostatnio: 4 dni temu",
            exerciseCount = 9,
            duration = "70-85 min",
        ),
        WorkoutPlan(
            name = "Leg Day - Nogi & Pośladki",
            difficulty = "Bardzo trudny",
            difficultyColor = Color.parseColor("#F44336"),
            difficultyBgRes = R.drawable.bg_badge_very_hard,
            lastPerformed = "Ostatnio: Wczoraj",
            exerciseCount = 10,
            duration = "80-90 min",
        ),
    )
}
