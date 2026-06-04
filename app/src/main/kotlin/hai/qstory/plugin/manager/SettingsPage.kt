package hai.qstory.plugin.manager

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.RadioButtonPreference

@Composable
fun SettingsPage(
    colorMode: Int = 0,
    onColorModeChange: (Int) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SmallTitle(text = "主题设置")
            Card(
                modifier = Modifier.padding(12.dp)
            ) {
                Column {
                    RadioButtonPreference(
                        title = "跟随系统",
                        selected = colorMode == 0,
                        onClick = { onColorModeChange(0) }
                    )
                    RadioButtonPreference(
                        title = "浅色模式",
                        selected = colorMode == 1,
                        onClick = { onColorModeChange(1) }
                    )
                    RadioButtonPreference(
                        title = "深色模式",
                        selected = colorMode == 2,
                        onClick = { onColorModeChange(2) }
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
