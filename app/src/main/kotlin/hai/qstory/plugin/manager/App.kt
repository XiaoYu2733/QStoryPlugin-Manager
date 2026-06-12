package hai.qstory.plugin.manager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hai.qstory.plugin.manager.ui.component.FloatingBottomBar
import hai.qstory.plugin.manager.ui.component.FloatingBottomBarItem
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import hai.qstory.plugin.manager.ui.screen.ColorPaletteScreen
import hai.qstory.plugin.manager.ui.theme.AppSettings
import hai.qstory.plugin.manager.ui.theme.AppTheme

@Composable
fun App(
    appSettings: AppSettings,
    enableBlur: Boolean = false,
    enableFloatingBottomBar: Boolean = false,
    enableFloatingBottomBarBlur: Boolean = false,
    pageScale: Float = 1.0f,
) {
    AppTheme(appSettings = appSettings) {
        val coroutineScope = rememberCoroutineScope()

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

        val pagerState = rememberPagerState(pageCount = { 4 })

        val navigationItems = remember {
            listOf(
                NavigationItem("主页", Icons.Rounded.Cottage),
                NavigationItem("统计", Icons.Rounded.BarChart),
                NavigationItem("关于", Icons.Rounded.Extension),
                NavigationItem("设置", Icons.Rounded.Settings),
            )
        }

        val surfaceColor = MiuixTheme.colorScheme.surface
        val backdrop = rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (navigator.current() == Route.Main) {
                    if (enableFloatingBottomBar) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FloatingBottomBar(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {},
                                    )
                                    .padding(bottom = 12.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
                                selectedIndex = { pagerState.currentPage },
                                onSelected = { index ->
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                backdrop = backdrop,
                                tabsCount = 4,
                                isBlurEnabled = enableFloatingBottomBarBlur,
                            ) {
                                navigationItems.forEachIndexed { index, item ->
                                    FloatingBottomBarItem(
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            tint = MiuixTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = item.label,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp,
                                            color = MiuixTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Visible
                                        )
                                    }
                                }
                            }
                        }
                    } else {
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
            }
        ) { innerPadding ->
            when (val route = navigator.current()) {
                is Route.Main -> {
                    // When floating bar is on, content extends behind it so the
                    // backdrop sees real content instead of Scaffold surface color.
                    val bottomPadding = if (enableFloatingBottomBar) 0.dp
                        else innerPadding.calculateBottomPadding()
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .padding(top = innerPadding.calculateTopPadding(), bottom = bottomPadding)
                            .then(if (enableFloatingBottomBar && enableFloatingBottomBarBlur) Modifier.layerBackdrop(backdrop) else Modifier),
                    ) { page ->
                        when (page) {
                            0 -> HomePage(
                                navigator = navigator
                            )
                            1 -> StatisticsPage()
                            2 -> AboutPage()
                            3 -> SettingsPage(
                                navigator = navigator,
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
                is Route.ColorPalette -> {
                    ColorPaletteScreen(
                        onBack = { navigator.pop() }
                    )
                }
            }
        }
    }
}
