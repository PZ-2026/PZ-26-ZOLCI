package pl.edu.ur.km131467.trainit.ui.feature

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<View>(R.id.btnPrimaryAction)?.visibility = View.GONE
        requestNotificationPermissionIfNeeded()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1101)
    }
}
