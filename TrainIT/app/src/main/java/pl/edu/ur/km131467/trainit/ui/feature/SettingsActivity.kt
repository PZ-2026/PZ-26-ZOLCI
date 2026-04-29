package pl.edu.ur.km131467.trainit.ui.feature

import pl.edu.ur.km131467.trainit.R

/**
 * Ekran ustawień użytkownika.
 *
 * Zawiera sekcje związane z profilem, jednostkami i prywatnością.
 */
class SettingsActivity : BaseFeatureActivity() {
    /** Konfiguracja modułu ustawień. */
    override val module: FeatureModule = FeatureModule.SETTINGS

    /** Aktywna zakładka dolnej nawigacji dla tego ekranu. */
    override val bottomNavItem: Int = R.id.nav_profile
}
