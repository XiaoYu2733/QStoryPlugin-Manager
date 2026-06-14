package hai.qstory.plugin.manager

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hai.qstory.plugin.manager.preferences.PreferencesManager
import hai.qstory.plugin.manager.ui.component.material.SegmentedColumn
import hai.qstory.plugin.manager.ui.component.material.SegmentedDropdownItem
import hai.qstory.plugin.manager.ui.component.material.SegmentedListItem
import hai.qstory.plugin.manager.ui.theme.LocalUiMode
import hai.qstory.plugin.manager.ui.theme.UiMode

@Composable
fun SettingsPageMaterial(
    navigator: AppNavigator,
) {
    val uiMode = LocalUiMode.current

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "外观",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )

            SegmentedColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                content = listOf(
                    {
                        SegmentedDropdownItem(
                            icon = Icons.Rounded.Dashboard,
                            title = "界面风格",
                            summary = "选择 Miuix 或 Material 界面风格",
                            items = UiMode.entries.map { it.name },
                            selectedIndex = if (uiMode == UiMode.Material) 1 else 0,
                            onItemSelected = { index ->
                                PreferencesManager.uiMode = if (index == 0) UiMode.Miuix.value else UiMode.Material.value
                            }
                        )
                    },
                    {
                        SegmentedListItem(
                            onClick = { navigator.push(Route.ColorPalette) },
                            headlineContent = { Text("主题设置") },
                            leadingContent = {
                                androidx.compose.material3.Icon(
                                    Icons.Rounded.Palette,
                                    "主题设置",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            supportingContent = { Text("调整主题模式、强调色、模糊效果等") },
                        )
                    }
                )
            )
        }
    }
}
