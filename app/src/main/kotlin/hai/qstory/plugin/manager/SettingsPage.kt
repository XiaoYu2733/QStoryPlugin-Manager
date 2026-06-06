package hai.qstory.plugin.manager

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsPage(
    colorMode: Int = 0,
    onColorModeChange: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val githubUrl = "https://github.com/XiaoYu2733/QStoryPlugin-Manager"

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                        context.startActivity(intent)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_github),
                        contentDescription = "GitHub",
                        tint = MiuixTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "查看源码",
                        style = MiuixTheme.textStyles.body1
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/XiaoYu_Chat"))
                        context.startActivity(intent)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_telegram),
                        contentDescription = "Telegram",
                        tint = MiuixTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Telegram 群组",
                        style = MiuixTheme.textStyles.body1
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
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
