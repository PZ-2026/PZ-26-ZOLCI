package pl.edu.ur.km131467.trainit.ui.feature

import pl.edu.ur.km131467.trainit.R

/**
 * Ekran modułu ćwiczeń.
 *
 * Korzysta z bazowego widoku [BaseFeatureActivity]; dolna nawigacja: zakładka Ćwiczenia.
 */
class ExercisesActivity : BaseFeatureActivity() {
    /** Konfiguracja modułu dla listy ćwiczeń. */
    override val module: FeatureModule = FeatureModule.EXERCISES

    /** Aktywna zakładka dolnej nawigacji dla tego ekranu. */
    override val bottomNavItem: Int = R.id.nav_exercises

    /** WF-14/15: wyszukiwanie i filtrowanie listy ćwiczeń. */
    override val enableListSearch: Boolean = true
    override val enableMuscleGroupFilter: Boolean = true
}
