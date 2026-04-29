package pl.edu.ur.km131467.trainit.ui.feature

import pl.edu.ur.km131467.trainit.R

/**
 * Ekran modułu sesji treningowych.
 *
 * Wyświetla listę sesji i działania kontekstowe związane z rozpoczęciem treningu.
 */
class SessionsActivity : BaseFeatureActivity() {
    /** Konfiguracja modułu dla sesji treningowych. */
    override val module: FeatureModule = FeatureModule.SESSIONS

    /** Aktywna zakładka dolnej nawigacji dla tego ekranu. */
    override val bottomNavItem: Int = R.id.nav_workouts
}
