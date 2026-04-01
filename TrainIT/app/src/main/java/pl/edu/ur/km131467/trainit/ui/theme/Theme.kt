package pl.edu.ur.km131467.trainit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Schemat kolorów dla motywu ciemnego Compose.
 *
 * Wykorzystuje kolory zdefiniowane w [Color.kt]: [Purple80], [PurpleGrey80], [Pink80].
 */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

/**
 * Schemat kolorów dla motywu jasnego Compose.
 *
 * Wykorzystuje kolory zdefiniowane w [Color.kt]: [Purple40], [PurpleGrey40], [Pink40].
 */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * Główna funkcja kompozycyjna definiująca motyw aplikacji TrainIT dla Jetpack Compose.
 *
 * Plik wygenerowany automatycznie przez Android Studio jako część szablonu
 * projektu Compose. Aktualnie nieużywany — aplikacja korzysta z widoków XML
 * i motywu zdefiniowanego w `res/values/themes.xml` ([Theme.TrainIT]).
 *
 * Obsługuje:
 * - **Dynamic Color** (Android 12+) — automatyczne dopasowanie kolorów
 *   do tapety użytkownika, gdy parametr [dynamicColor] jest ustawiony na `true`.
 * - **Motyw ciemny/jasny** — wybierany na podstawie ustawień systemowych
 *   lub jawnie przez parametr [darkTheme].
 *
 * @param darkTheme czy użyć motywu ciemnego. Domyślnie bazuje na ustawieniach systemu.
 * @param dynamicColor czy włączyć dynamiczne kolory (dostępne na Android 12+). Domyślnie `true`.
 * @param content treść kompozycyjna opakowywana motywem.
 */
@Composable
fun TrainITTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
