package pl.edu.ur.km131467.trainit.ui.feature

import pl.edu.ur.km131467.trainit.R

/**
 * Ekran modułu statystyk.
 *
 * Rozszerza dashboard o listę dodatkowych podsumowań i metryk aktywności.
 */
class StatisticsActivity : BaseFeatureActivity() {
    /** Konfiguracja modułu dla statystyk. */
    override val module: FeatureModule = FeatureModule.STATISTICS

    /** Aktywna zakładka dolnej nawigacji dla tego ekranu. */
    override val bottomNavItem: Int = R.id.nav_home
}
