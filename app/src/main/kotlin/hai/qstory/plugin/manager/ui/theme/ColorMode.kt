package hai.qstory.plugin.manager.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec

enum class ColorMode(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2),
    MONET_SYSTEM(3),
    MONET_LIGHT(4),
    MONET_DARK(5);

    companion object {
        fun fromValue(value: Int) = entries.find { it.value == value } ?: SYSTEM
    }

    val isSystem: Boolean get() = value == 0 || value == 3
    val isDark: Boolean get() = value == 2 || value == 5
    val isMonet: Boolean get() = value >= 3

    fun toNonMonetMode(): Int = when (this) {
        MONET_SYSTEM -> 0
        MONET_LIGHT -> 1
        MONET_DARK -> 2
        else -> value
    }

    fun toMonetMode(): Int = when (this) {
        SYSTEM -> 3
        LIGHT -> 4
        DARK -> 5
        else -> value
    }
}

data class AppSettings(
    val colorMode: ColorMode,
    val keyColor: Int,
    val paletteStyle: PaletteStyle,
    val colorSpec: ColorSpec.SpecVersion,
)

val keyColorOptions = listOf(
    Color(0xFFF44336).toArgb(), // Red
    Color(0xFFE91E63).toArgb(), // Pink
    Color(0xFF9C27B0).toArgb(), // Purple
    Color(0xFF673AB7).toArgb(), // Deep Purple
    Color(0xFF3F51B5).toArgb(), // Indigo
    Color(0xFF2196F3).toArgb(), // Blue
    Color(0xFF00BCD4).toArgb(), // Cyan
    Color(0xFF009688).toArgb(), // Teal
    Color(0xFF4FAF50).toArgb(), // Green
    Color(0xFFFFEB3B).toArgb(), // Yellow
    Color(0xFFFFC107).toArgb(), // Amber
    Color(0xFFFF9800).toArgb(), // Orange
    Color(0xFF795548).toArgb(), // Brown
    Color(0xFF607D8F).toArgb(), // Blue Grey
    Color(0xFFFF9CA8).toArgb(), // Sakura
)
