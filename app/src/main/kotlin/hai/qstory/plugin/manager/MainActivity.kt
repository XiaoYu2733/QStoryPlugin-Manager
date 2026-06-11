package hai.qstory.plugin.manager

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import hai.qstory.plugin.manager.preferences.PreferencesManager
import hai.qstory.plugin.manager.ui.theme.AppSettings
import hai.qstory.plugin.manager.ui.theme.ColorMode

val LocalColorMode = staticCompositionLocalOf { 0 }
val LocalEnableBlur = staticCompositionLocalOf { false }
val LocalEnableFloatingBottomBar = staticCompositionLocalOf { false }
val LocalEnableFloatingBottomBarBlur = staticCompositionLocalOf { false }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PreferencesManager.init(applicationContext)

        setContent {
            var colorModeValue by remember { mutableIntStateOf(PreferencesManager.colorMode) }
            var miuixMonet by remember { mutableStateOf(PreferencesManager.miuixMonet) }
            var keyColor by remember { mutableIntStateOf(PreferencesManager.keyColor) }
            var colorStyleStr by remember { mutableStateOf(PreferencesManager.colorStyle) }
            var colorSpecStr by remember { mutableStateOf(PreferencesManager.colorSpec) }
            var enableBlur by remember { mutableStateOf(PreferencesManager.enableBlur) }
            var enableFloatingBottomBar by remember { mutableStateOf(PreferencesManager.enableFloatingBottomBar) }
            var enableFloatingBottomBarBlur by remember { mutableStateOf(PreferencesManager.enableFloatingBottomBarBlur) }
            var enablePredictiveBack by remember { mutableStateOf(PreferencesManager.enablePredictiveBack) }
            var pageScale by remember { mutableFloatStateOf(PreferencesManager.pageScale) }

            val colorMode = ColorMode.fromValue(colorModeValue)
            val paletteStyle = try {
                PaletteStyle.valueOf(colorStyleStr)
            } catch (_: Exception) {
                PaletteStyle.TonalSpot
            }
            val colorSpec = try {
                ColorSpec.SpecVersion.valueOf(colorSpecStr)
            } catch (_: Exception) {
                ColorSpec.SpecVersion.Default
            }

            val appSettings = AppSettings(colorMode, keyColor, paletteStyle, colorSpec)

            val darkMode = colorMode.isDark || (colorMode.isSystem && isSystemInDarkTheme())

            DisposableEffect(darkMode) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkMode },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkMode },
                )
                window.isNavigationBarContrastEnforced = false
                onDispose {}
            }

            val systemDensity = LocalDensity.current
            val density = remember(systemDensity, pageScale) {
                Density(systemDensity.density * pageScale, systemDensity.fontScale)
            }

            // Refresh state from prefs when returning from ColorPaletteScreen
            val refreshSettings: () -> Unit = {
                colorModeValue = PreferencesManager.colorMode
                miuixMonet = PreferencesManager.miuixMonet
                keyColor = PreferencesManager.keyColor
                colorStyleStr = PreferencesManager.colorStyle
                colorSpecStr = PreferencesManager.colorSpec
                enableBlur = PreferencesManager.enableBlur
                enableFloatingBottomBar = PreferencesManager.enableFloatingBottomBar
                enableFloatingBottomBarBlur = PreferencesManager.enableFloatingBottomBarBlur
                enablePredictiveBack = PreferencesManager.enablePredictiveBack
                pageScale = PreferencesManager.pageScale
            }

            CompositionLocalProvider(
                LocalDensity provides density,
                LocalColorMode provides colorModeValue,
                LocalEnableBlur provides enableBlur,
                LocalEnableFloatingBottomBar provides enableFloatingBottomBar,
                LocalEnableFloatingBottomBarBlur provides enableFloatingBottomBarBlur,
            ) {
                App(
                    appSettings = appSettings,
                    enableBlur = enableBlur,
                    enableFloatingBottomBar = enableFloatingBottomBar,
                    enableFloatingBottomBarBlur = enableFloatingBottomBarBlur,
                    pageScale = pageScale,
                    onSettingsChanged = refreshSettings,
                )
            }
        }
    }
}
