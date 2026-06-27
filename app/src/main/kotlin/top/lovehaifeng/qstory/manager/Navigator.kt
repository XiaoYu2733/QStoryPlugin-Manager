package top.lovehaifeng.qstory.manager

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

/**
 * Simple navigation interface.
 */
interface AppNavigator {
    val backStack: SnapshotStateList<NavKey>
    fun push(route: NavKey)
    /** Returns to the previous screen in the navigation stack. */
    fun pop()
    fun current(): NavKey
}
