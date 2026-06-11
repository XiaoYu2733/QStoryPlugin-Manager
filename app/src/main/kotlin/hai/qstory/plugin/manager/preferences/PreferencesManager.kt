package hai.qstory.plugin.manager.preferences

import android.content.Context
import android.content.SharedPreferences
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import hai.qstory.plugin.manager.ui.theme.ColorMode

object PreferencesManager {

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    var colorMode: Int
        get() = prefs.getInt("color_mode", ColorMode.SYSTEM.value)
        set(value) = prefs.edit().putInt("color_mode", value).apply()

    var miuixMonet: Boolean
        get() = prefs.getBoolean("miuix_monet", false)
        set(value) = prefs.edit().putBoolean("miuix_monet", value).apply()

    var keyColor: Int
        get() = prefs.getInt("key_color", 0)
        set(value) = prefs.edit().putInt("key_color", value).apply()

    var colorStyle: String
        get() = prefs.getString("color_style", PaletteStyle.TonalSpot.name) ?: PaletteStyle.TonalSpot.name
        set(value) = prefs.edit().putString("color_style", value).apply()

    var colorSpec: String
        get() = prefs.getString("color_spec", ColorSpec.SpecVersion.Default.name) ?: ColorSpec.SpecVersion.Default.name
        set(value) = prefs.edit().putString("color_spec", value).apply()

    var enableBlur: Boolean
        get() = prefs.getBoolean("enable_blur", false)
        set(value) = prefs.edit().putBoolean("enable_blur", value).apply()

    var enableFloatingBottomBar: Boolean
        get() = prefs.getBoolean("enable_floating_bottom_bar", false)
        set(value) = prefs.edit().putBoolean("enable_floating_bottom_bar", value).apply()

    var enableFloatingBottomBarBlur: Boolean
        get() = prefs.getBoolean("enable_floating_bottom_bar_blur", false)
        set(value) = prefs.edit().putBoolean("enable_floating_bottom_bar_blur", value).apply()

    var enablePredictiveBack: Boolean
        get() = prefs.getBoolean("enable_predictive_back", false)
        set(value) = prefs.edit().putBoolean("enable_predictive_back", value).apply()

    var pageScale: Float
        get() = prefs.getFloat("page_scale", 1.0f)
        set(value) = prefs.edit().putFloat("page_scale", value).apply()
}
