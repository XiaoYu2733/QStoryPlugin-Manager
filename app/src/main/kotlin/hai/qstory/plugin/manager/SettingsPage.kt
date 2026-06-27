package hai.qstory.plugin.manager

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hai.qstory.plugin.manager.preferences.PreferencesManager
import hai.qstory.plugin.manager.ui.theme.LocalUiMode
import hai.qstory.plugin.manager.ui.theme.UiMode
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsPage(
    navigator: AppNavigator,
) {
    when (LocalUiMode.current) {
        UiMode.Material -> SettingsPageMaterial(navigator = navigator)
        UiMode.Miuix -> SettingsPageMiuix(navigator = navigator)
    }
}

@Composable
fun SettingsPageMiuix(
    navigator: AppNavigator,
) {
    val uiMode = LocalUiMode.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        item {
            SmallTitle(text = "外观")
        }
        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
            ) {
                OverlayDropdownPreference(
                    title = "界面风格",
                    summary = "选择 Miuix 或 Material 界面风格",
                    items = UiMode.entries.map { it.name },
                    startAction = {
                        Icon(
                            Icons.Rounded.Dashboard,
                            modifier = Modifier.padding(end = 6.dp),
                            contentDescription = "界面风格",
                            tint = MiuixTheme.colorScheme.onBackground
                        )
                    },
                    selectedIndex = if (uiMode == UiMode.Material) 1 else 0,
                    onSelectedIndexChange = { index ->
                        PreferencesManager.uiMode = if (index == 0) UiMode.Miuix.value else UiMode.Material.value
                    }
                )
            }
        }
        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
            ) {
                ArrowPreference(
                    title = "主题设置",
                    summary = "调整主题模式、强调色、模糊效果等",
                    startAction = {
                        Icon(
                            Icons.Rounded.Palette,
                            modifier = Modifier.padding(end = 6.dp),
                            contentDescription = "主题设置",
                            tint = MiuixTheme.colorScheme.onBackground
                        )
                    },
                    onClick = { navigator.push(Route.ColorPalette) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
