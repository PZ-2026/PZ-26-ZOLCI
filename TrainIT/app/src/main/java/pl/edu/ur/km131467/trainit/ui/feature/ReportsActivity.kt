package pl.edu.ur.km131467.trainit.ui.feature

import pl.edu.ur.km131467.trainit.R

/**
 * Ekran modułu raportów.
 *
 * Prezentuje gotowe i oczekujące raporty użytkownika w postaci listy pozycji.
 */
class ReportsActivity : BaseFeatureActivity() {
    /** Konfiguracja modułu dla raportów. */
    override val module: FeatureModule = FeatureModule.REPORTS

    /** Aktywna zakładka dolnej nawigacji dla tego ekranu. */
    override val bottomNavItem: Int = R.id.nav_profile
}
