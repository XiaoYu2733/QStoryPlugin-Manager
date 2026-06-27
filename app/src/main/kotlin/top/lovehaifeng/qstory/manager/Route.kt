package top.lovehaifeng.qstory.manager

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation routes for the app.
 */
@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Main : Route

    @Serializable
    data class PluginDetail(val cloudId: String) : Route

    @Serializable
    data object ColorPalette : Route
}
