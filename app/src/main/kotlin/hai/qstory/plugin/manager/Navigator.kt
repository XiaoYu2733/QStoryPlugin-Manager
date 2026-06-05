package hai.qstory.plugin.manager

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

/**
 * Simple navigation interface.
 */
interface AppNavigator {
    val backStack: SnapshotStateList<NavKey>
    fun push(route: NavKey)
    fun pop()
    fun current(): NavKey
}
