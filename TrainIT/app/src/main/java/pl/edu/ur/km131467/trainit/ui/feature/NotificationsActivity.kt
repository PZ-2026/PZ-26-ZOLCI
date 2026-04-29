package pl.edu.ur.km131467.trainit.ui.feature

import pl.edu.ur.km131467.trainit.R

/**
 * Ekran ustawień i podglądu powiadomień.
 *
 * Udostępnia podsumowanie preferencji przypomnień treningowych.
 */
class NotificationsActivity : BaseFeatureActivity() {
    /** Konfiguracja modułu powiadomień. */
    override val module: FeatureModule = FeatureModule.NOTIFICATIONS

    /** Aktywna zakładka dolnej nawigacji dla tego ekranu. */
    override val bottomNavItem: Int = R.id.nav_profile
}
