package hai.qstory.plugin.manager

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import hai.qstory.plugin.manager.ui.component.FloatingBottomBar
import hai.qstory.plugin.manager.ui.component.FloatingBottomBarItem
import hai.qstory.plugin.manager.ui.screen.ColorPaletteScreen
import hai.qstory.plugin.manager.ui.theme.AppSettings
import hai.qstory.plugin.manager.ui.theme.AppTheme
import hai.qstory.plugin.manager.ui.theme.LocalUiMode
import hai.qstory.plugin.manager.ui.theme.UiMode
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.NavigationBar as M3NavigationBar
import androidx.compose.material3.NavigationBarItem as M3NavigationBarItem

private fun NavKey.toSaveString(): String = when (this) {
    is Route.Main -> "main"
    is Route.ColorPalette -> "color_palette"
    is Route.PluginDetail -> "plugin_detail:${cloudId}"
    else -> "main"
}

private fun String.toNavRoute(): Route = when {
    this == "color_palette" -> Route.ColorPalette
    this.startsWith("plugin_detail:") -> Route.PluginDetail(this.removePrefix("plugin_detail:"))
    else -> Route.Main
}

private fun String.toNavKey(): NavKey = toNavRoute()

private fun routeForward(from: String, to: String): Boolean {
    val fromDepth = when (from.toNavRoute()) {
        is Route.Main -> 0
        else -> 1
    }
    val toDepth = when (to.toNavRoute()) {
        is Route.Main -> 0
        else -> 1
    }
    return toDepth > fromDepth
}

@Composable
fun App(
    appSettings: AppSettings,
    enableBlur: Boolean = false,
    enableFloatingBottomBar: Boolean = false,
    enableFloatingBottomBarBlur: Boolean = false,
    pageScale: Float = 1.0f,
    uiMode: UiMode = UiMode.Miuix,
) {
    CompositionLocalProvider(LocalUiMode provides uiMode) {
        val coroutineScope = rememberCoroutineScope()

        // Navigation state is ABOVE AppTheme so it survives theme switches (Bug: UI style change)
        // rememberSaveable so it survives Activity.recreate() (Bug: predictive back toggle)
        var currentRouteStr by rememberSaveable { mutableStateOf("main") }
        var navStackStr by rememberSaveable { mutableStateOf(listOf("main")) }

        val navigator = remember {
            object : AppNavigator {
                override val backStack: SnapshotStateList<NavKey>
                    get() = mutableStateListOf<NavKey>().also {
                        it.addAll(navStackStr.map { s -> s.toNavKey() })
                    }

                override fun push(route: NavKey) {
                    navStackStr = navStackStr + route.toSaveString()
                    currentRouteStr = route.toSaveString()
                }

                override fun pop() {
                    if (navStackStr.size > 1) {
                        navStackStr = navStackStr.dropLast(1)
                        currentRouteStr = navStackStr.last()
                    }
                }

                override fun current(): NavKey = currentRouteStr.toNavKey()
            }
        }

        val pagerState = rememberPagerState(pageCount = { 4 })
        val context = LocalContext.current
        var lastBackPressTime by remember { mutableLongStateOf(0L) }

        var previousRouteStr by remember { mutableStateOf(currentRouteStr) }
        LaunchedEffect(currentRouteStr) {
            if (previousRouteStr.startsWith("plugin_detail:") && currentRouteStr == "main") {
                if (pagerState.currentPage != 0) {
                    pagerState.animateScrollToPage(0)
                }
            } else if (currentRouteStr != "main" && !currentRouteStr.startsWith("plugin_detail:")) {
                lastBackPressTime = 0L
            }
            previousRouteStr = currentRouteStr
        }

        val performOptimizedBack: () -> Unit = {
            val atMainHome = navigator.current() is Route.Main && pagerState.currentPage == 0
            val now = System.currentTimeMillis()

            if (!atMainHome) {
                when (navigator.current()) {
                    is Route.PluginDetail -> navigator.pop()
                    is Route.Main -> {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                    else -> Unit
                }
                lastBackPressTime = now
            } else if (now - lastBackPressTime < 2_000L) {
                (context as? Activity)?.moveTaskToBack(true)
                lastBackPressTime = 0L
            } else {
                lastBackPressTime = now
                Toast.makeText(context, "再按一次退出应用", Toast.LENGTH_SHORT).show()
            }
        }

        BackHandler(
            enabled = navigator.current() is Route.PluginDetail
                || navigator.current() is Route.Main,
        ) {
            performOptimizedBack()
        }

        BackHandler(enabled = navigator.current() is Route.ColorPalette) {
            navigator.pop()
        }

        val navigationItems = remember {
            listOf(
                NavigationItem("主页", Icons.Rounded.Cottage),
                NavigationItem("统计", Icons.Rounded.BarChart),
                NavigationItem("关于", Icons.Rounded.Extension),
                NavigationItem("设置", Icons.Rounded.Settings),
            )
        }

        AppTheme(appSettings = appSettings, uiMode = uiMode) {
            val surfaceColor = MiuixTheme.colorScheme.surface
            val backdrop = rememberLayerBackdrop {
                drawRect(surfaceColor)
                drawContent()
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (navigator.current() == Route.Main) {
                        if (uiMode == UiMode.Material) {
                            M3NavigationBar {
                                navigationItems.forEachIndexed { index, item ->
                                    M3NavigationBarItem(
                                        selected = pagerState.currentPage == index,
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        },
                                        icon = {
                                            androidx.compose.material3.Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.label,
                                            )
                                        },
                                        label = {
                                            androidx.compose.material3.Text(
                                                text = item.label,
                                            )
                                        },
                                    )
                                }
                            }
                        } else if (enableFloatingBottomBar) {
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
                AnimatedContent(
                    targetState = currentRouteStr,
                    transitionSpec = {
                        if (routeForward(initialState, targetState)) {
                            slideInHorizontally(tween(300)) { fullWidth -> fullWidth } togetherWith
                                slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth / 3 }
                        } else {
                            slideInHorizontally(tween(300)) { fullWidth -> -fullWidth / 3 } togetherWith
                                slideOutHorizontally(tween(300)) { fullWidth -> fullWidth }
                        }
                    },
                    label = "app_route",
                    modifier = Modifier.fillMaxSize(),
                ) { routeStr ->
                    when (val route = routeStr.toNavRoute()) {
                        is Route.Main -> {
                            val bottomPadding = if (enableFloatingBottomBar) 0.dp
                                else innerPadding.calculateBottomPadding()
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .padding(bottom = bottomPadding)
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
                                cloudId = route.cloudId,
                                onBack = performOptimizedBack,
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
    }
}
