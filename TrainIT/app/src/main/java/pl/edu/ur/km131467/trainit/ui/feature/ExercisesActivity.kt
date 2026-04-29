package pl.edu.ur.km131467.trainit.ui.feature

import pl.edu.ur.km131467.trainit.R

/**
 * Ekran modułu ćwiczeń.
 *
 * Korzysta z bazowego widoku [BaseFeatureActivity] i zaznacza zakładkę treningów
 * jako najbliższą funkcjonalnie sekcję na dolnej nawigacji.
 */
class ExercisesActivity : BaseFeatureActivity() {
    /** Konfiguracja modułu dla listy ćwiczeń. */
    override val module: FeatureModule = FeatureModule.EXERCISES

    /** Aktywna zakładka dolnej nawigacji dla tego ekranu. */
    override val bottomNavItem: Int = R.id.nav_workouts
}
