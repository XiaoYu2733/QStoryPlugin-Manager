package hai.qstory.plugin.manager

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.ArrowPreference

@Composable
fun SettingsPage(
    navigator: AppNavigator,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
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
                Column {
                    ArrowPreference(
                        title = "界面风格",
                        summary = "调整主题模式、强调色、模糊效果等",
                        onClick = { navigator.push(Route.ColorPalette) }
                    )
                }
            }
        }
    }
}
