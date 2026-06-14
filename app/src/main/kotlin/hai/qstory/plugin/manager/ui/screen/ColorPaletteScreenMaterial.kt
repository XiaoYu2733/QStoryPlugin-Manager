package hai.qstory.plugin.manager.ui.screen

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.filled.Brightness1
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.CallToAction
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DesignServices
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import hai.qstory.plugin.manager.preferences.PreferencesManager
import hai.qstory.plugin.manager.ui.component.ScaleDialog
import hai.qstory.plugin.manager.ui.component.material.ColorCircleButton
import hai.qstory.plugin.manager.ui.component.material.SegmentedColumn
import hai.qstory.plugin.manager.ui.component.material.SegmentedDropdownItem
import hai.qstory.plugin.manager.ui.component.material.SegmentedSwitchItem
import hai.qstory.plugin.manager.ui.component.material.TonalCard
import hai.qstory.plugin.manager.ui.theme.ColorMode
import hai.qstory.plugin.manager.ui.theme.keyColorOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPaletteScreenMaterial(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val pref = PreferencesManager
    val haptic = LocalHapticFeedback.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    var themeMode by remember { mutableStateOf(pref.colorMode) }
    var miuixMonet by remember { mutableStateOf(pref.miuixMonet) }
    var keyColor by remember { mutableStateOf(pref.keyColor) }
    var colorStyle by remember { mutableStateOf(pref.colorStyle) }
    var colorSpec by remember { mutableStateOf(pref.colorSpec) }
    var enableBlur by remember { mutableStateOf(pref.enableBlur) }
    var enableFloatingBottomBar by remember { mutableStateOf(pref.enableFloatingBottomBar) }
    var enableFloatingBottomBarBlur by remember { mutableStateOf(pref.enableFloatingBottomBarBlur) }
    var enablePredictiveBack by remember { mutableStateOf(pref.enablePredictiveBack) }
    var pageScale by remember { mutableStateOf(pref.pageScale) }

    fun saveAndRefresh() {
        pref.colorMode = themeMode
        pref.miuixMonet = miuixMonet
        pref.keyColor = keyColor
        pref.colorStyle = colorStyle
        pref.colorSpec = colorSpec
        pref.enableBlur = enableBlur
        pref.enableFloatingBottomBar = enableFloatingBottomBar
        pref.enableFloatingBottomBarBlur = enableFloatingBottomBarBlur
        pref.enablePredictiveBack = enablePredictiveBack
        pref.pageScale = pageScale
    }

    LaunchedEffect(Unit) {
        scrollBehavior.state.heightOffset = scrollBehavior.state.heightOffsetLimit
    }

    val currentColorMode = ColorMode.fromValue(themeMode)
    val isDark = currentColorMode.isDark || currentColorMode.isSystem && isSystemInDarkTheme()
    val currentPaletteStyle = try {
        PaletteStyle.valueOf(colorStyle)
    } catch (_: Exception) {
        PaletteStyle.TonalSpot
    }
    val currentColorSpec = try {
        ColorSpec.SpecVersion.valueOf(colorSpec)
    } catch (_: Exception) {
        ColorSpec.SpecVersion.Default
    }

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                title = { Text("主题设置") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { paddingValues ->
        val navBars = WindowInsets.navigationBars.asPaddingValues()
        val captionBar = WindowInsets.captionBar.asPaddingValues()
        val showScaleDialog = rememberSaveable { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemePreviewCardMaterial(
                keyColor = keyColor,
                isDark = isDark,
                paletteStyle = currentPaletteStyle,
                colorSpec = currentColorSpec,
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    ColorCircleButton(
                        color = Color(0),
                        isSelected = keyColor == 0,
                        isDark = isDark,
                        paletteStyle = currentPaletteStyle,
                        colorSpec = currentColorSpec,
                        onClick = {
                            keyColor = 0
                            saveAndRefresh()
                        }
                    )
                }

                items(keyColorOptions) { color ->
                    ColorCircleButton(
                        color = Color(color),
                        isSelected = keyColor == color,
                        isDark = isDark,
                        paletteStyle = currentPaletteStyle,
                        colorSpec = currentColorSpec,
                        onClick = {
                            keyColor = color
                            saveAndRefresh()
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val options = listOf(
                    ColorMode.SYSTEM,
                    ColorMode.LIGHT,
                    ColorMode.DARK,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    options.forEachIndexed { index, mode ->
                        val isSelected = (if (themeMode >= 3) themeMode - 3 else themeMode) == mode.value
                        ToggleButton(
                            checked = isSelected,
                            onCheckedChange = {
                                if (it) {
                                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                    themeMode = if (miuixMonet) mode.value + 3 else mode.value
                                    saveAndRefresh()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                2 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                        ) {
                            Icon(
                                imageVector = when (mode) {
                                    ColorMode.SYSTEM -> Icons.Filled.Brightness4
                                    ColorMode.LIGHT -> Icons.Filled.Brightness7
                                    ColorMode.DARK -> Icons.Filled.Brightness3
                                    else -> Icons.Filled.Brightness4
                                },
                                contentDescription = null
                            )
                        }
                    }
                }

                SegmentedColumn(
                    modifier = Modifier.padding(top = 4.dp),
                    content = listOf(
                        {
                            SegmentedSwitchItem(
                                icon = Icons.Rounded.Check,
                                title = "Monet",
                                checked = miuixMonet,
                                onCheckedChange = {
                                    miuixMonet = it
                                    val cm = ColorMode.fromValue(themeMode)
                                    themeMode = if (it) {
                                        if (!cm.isMonet) cm.toMonetMode() else themeMode
                                    } else {
                                        if (cm.isMonet) cm.toNonMonetMode() else themeMode
                                    }
                                    saveAndRefresh()
                                }
                            )
                        },
                        {
                            val colorItems = listOf(
                                "默认", "红色", "粉色", "紫色", "深紫色", "靛蓝",
                                "蓝色", "青色", "蓝绿色", "绿色", "黄色", "琥珀色",
                                "橙色", "棕色", "蓝灰色", "樱花粉"
                            )
                            val colorValues = listOf(0) + keyColorOptions
                            SegmentedDropdownItem(
                                icon = Icons.Rounded.Check,
                                title = "强调色",
                                items = colorItems,
                                selectedIndex = colorValues.indexOf(keyColor).takeIf { it >= 0 } ?: 0,
                                onItemSelected = { index ->
                                    keyColor = colorValues[index]
                                    saveAndRefresh()
                                }
                            )
                        },
                        {
                            val styles = PaletteStyle.entries
                            SegmentedDropdownItem(
                                icon = Icons.Rounded.Style,
                                title = "色彩风格",
                                items = styles.map { it.name },
                                selectedIndex = styles.indexOfFirst { it.name == colorStyle }.coerceAtLeast(0),
                                onItemSelected = { index ->
                                    colorStyle = styles[index].name
                                    saveAndRefresh()
                                }
                            )
                        },
                        {
                            val specs = ColorSpec.SpecVersion.entries
                            SegmentedDropdownItem(
                                icon = Icons.Rounded.DesignServices,
                                title = "色彩标准",
                                items = specs.map { it.name },
                                selectedIndex = specs.indexOfFirst { it.name == colorSpec }.coerceAtLeast(0),
                                onItemSelected = { index ->
                                    colorSpec = specs[index].name
                                    saveAndRefresh()
                                }
                            )
                        }
                    )
                )

                SegmentedColumn(
                    modifier = Modifier.padding(top = 4.dp),
                    content = listOf(
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                SegmentedSwitchItem(
                                    icon = Icons.Rounded.BlurOn,
                                    title = "模糊效果",
                                    summary = "启用后将在界面中应用模糊效果",
                                    checked = enableBlur,
                                    onCheckedChange = {
                                        enableBlur = it
                                        saveAndRefresh()
                                    }
                                )
                            }
                        },
                        {
                            SegmentedSwitchItem(
                                icon = Icons.Rounded.CallToAction,
                                title = "悬浮底栏",
                                summary = "将底栏变为 iOS 风格的悬浮样式",
                                checked = enableFloatingBottomBar,
                                onCheckedChange = {
                                    enableFloatingBottomBar = it
                                    saveAndRefresh()
                                }
                            )
                        },
                        {
                            AnimatedVisibility(visible = enableFloatingBottomBar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                SegmentedSwitchItem(
                                    icon = Icons.Rounded.WaterDrop,
                                    title = "液体玻璃",
                                    summary = "为悬浮底栏添加液体玻璃效果",
                                    checked = enableFloatingBottomBarBlur,
                                    onCheckedChange = {
                                        enableFloatingBottomBarBlur = it
                                        saveAndRefresh()
                                    }
                                )
                            }
                        }
                    )
                )

                SegmentedColumn(
                    modifier = Modifier.padding(top = 4.dp),
                    content = listOf(
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                SegmentedSwitchItem(
                                    icon = Icons.AutoMirrored.Rounded.MenuOpen,
                                    title = "预测性返回手势",
                                    summary = "需要 Android 14+",
                                    checked = enablePredictiveBack,
                                    onCheckedChange = {
                                        enablePredictiveBack = it
                                        saveAndRefresh()
                                        (context as? android.app.Activity)?.recreate()
                                    }
                                )
                            }
                        }
                    )
                )

                TonalCard(modifier = Modifier.padding(top = 4.dp)) {
                    var sliderValue by remember(pageScale) { mutableFloatStateOf(pageScale) }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.AspectRatio,
                                contentDescription = "界面缩放",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "界面缩放",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "调整整体界面的大小",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Text(
                                text = "${(sliderValue * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            onValueChangeFinished = {
                                pageScale = sliderValue
                                saveAndRefresh()
                            },
                            valueRange = 0.8f..1.1f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp + navBars.calculateBottomPadding() + captionBar.calculateBottomPadding()))
        }
    }
}

@Composable
private fun ThemePreviewCardMaterial(
    keyColor: Int,
    isDark: Boolean,
    paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    colorSpec: ColorSpec.SpecVersion = ColorSpec.SpecVersion.SPEC_2021,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()
    val screenRatio = screenWidth / screenHeight

    val colorScheme = if (keyColor == 0) {
        rememberDynamicColorScheme(
            seedColor = Color.Unspecified,
            isDark = isDark,
            style = paletteStyle,
            specVersion = colorSpec,
        )
    } else {
        rememberDynamicColorScheme(
            seedColor = Color(keyColor),
            isDark = isDark,
            style = paletteStyle,
            specVersion = colorSpec,
        )
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .aspectRatio(screenRatio),
            color = colorScheme.background,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 12.dp, top = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Manager",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurface
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopStart
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TonalCard(
                            containerColor = colorScheme.secondaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            content = { }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TonalCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                shape = RoundedCornerShape(12.dp),
                                content = { }
                            )
                            TonalCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                shape = RoundedCornerShape(12.dp),
                                content = { }
                            )
                        }
                        TonalCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(96.dp),
                            shape = RoundedCornerShape(12.dp),
                            content = { }
                        )
                    }
                }

                Surface(
                    color = colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(colorScheme.primary, RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}
