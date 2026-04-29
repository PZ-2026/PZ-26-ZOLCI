package pl.edu.ur.km131467.trainit.ui.feature

import pl.edu.ur.km131467.trainit.R

/**
 * Ekran skrótów panelu roli użytkownika.
 *
 * Lista pozycji jest dopasowana do roli zapisanej w [pl.edu.ur.km131467.trainit.data.local.SessionManager].
 */
class RolePanelActivity : BaseFeatureActivity() {
    /** Konfiguracja modułu panelu roli. */
    override val module: FeatureModule = FeatureModule.ROLE_PANEL

    /** Aktywna zakładka dolnej nawigacji dla tego ekranu. */
    override val bottomNavItem: Int = R.id.nav_profile
}
