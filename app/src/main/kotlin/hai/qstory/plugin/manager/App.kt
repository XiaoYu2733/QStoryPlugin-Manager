package hai.qstory.plugin.manager

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

@Composable
fun App(
    colorMode: Int = 0,
    onColorModeChange: (Int) -> Unit = {}
) {
    val themeController = remember(colorMode) {
        when (colorMode) {
            1 -> ThemeController(ColorSchemeMode.Light)
            2 -> ThemeController(ColorSchemeMode.Dark)
            else -> ThemeController(ColorSchemeMode.System)
        }
    }

    MiuixTheme(controller = themeController) {
        val coroutineScope = rememberCoroutineScope()

        // Navigation stack
        val navStack = remember { mutableStateListOf<NavKey>(Route.Main) }
        var currentRoute by remember { mutableStateOf<NavKey>(Route.Main) }

        val navigator = remember {
            object : AppNavigator {
                override val backStack: SnapshotStateList<NavKey> = navStack
                override fun push(route: NavKey) {
                    navStack.add(route)
                    currentRoute = route
                }
                override fun pop() {
                    if (navStack.size > 1) {
                        navStack.removeLast()
                        currentRoute = navStack.last()
                    }
                }
                override fun current() = currentRoute
            }
        }

        val pagerState = rememberPagerState(pageCount = { 3 })

        val navigationItems = remember {
            listOf(
                NavigationItem("主页", MiuixIcons.Info),
                NavigationItem("关于", MiuixIcons.Info),
                NavigationItem("设置", MiuixIcons.Settings),
            )
        }

        // Main navigation handling
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { null },
            bottomBar = {
                if (navigator.current() == Route.Main) {
                    NavigationBar {
                        navigationItems.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                icon = item.icon,
                                label = item.label,
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            when (val route = navigator.current()) {
                is Route.Main -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.padding(innerPadding),
                    ) { page ->
                        when (page) {
                            0 -> HomePage(
                                navigator = navigator
                            )
                            1 -> AboutPage()
                            2 -> SettingsPage(
                                colorMode = colorMode,
                                onColorModeChange = onColorModeChange
                            )
                        }
                    }
                }
                is Route.PluginDetail -> {
                    PluginDetailPage(
                        cloudId = (route as Route.PluginDetail).cloudId,
                        navigator = navigator
                    )
                }
            }
        }
    }
}
