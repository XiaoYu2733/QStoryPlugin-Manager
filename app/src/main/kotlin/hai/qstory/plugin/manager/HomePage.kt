package hai.qstory.plugin.manager

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hai.qstory.plugin.manager.data.OnlinePluginInfo
import hai.qstory.plugin.manager.manager.PluginDownloadManager
import hai.qstory.plugin.manager.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

sealed class DownloadState {
    data object Idle : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    data object Success : DownloadState()
    data object Failed : DownloadState()
}

@Composable
fun HomePage() {
    val context = LocalContext.current
    val downloadManager = remember { PluginDownloadManager(context) }
    var pluginList by remember { mutableStateOf<List<OnlinePluginInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.pluginService.getOnlinePluginList()
                if (response.isSuccess()) {
                    pluginList = response.data ?: emptyList()
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            isLoading -> {
                item {
                    Card(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(text = "加载中...")
                        }
                    }
                }
            }
            errorMessage != null -> {
                item {
                    Card(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(text = "加载失败")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage ?: "未知错误",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceSecondary
                            )
                        }
                    }
                }
            }
            pluginList.isEmpty() -> {
                item {
                    Card(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(text = "暂无脚本")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "这里是存放脚本的页面",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceSecondary
                            )
                        }
                    }
                }
            }
            else -> {
                items(pluginList) { plugin ->
                    PluginCard(
                        plugin = plugin,
                        downloadManager = downloadManager
                    )
                }
            }
        }
    }
}

@Composable
fun PluginCard(
    plugin: OnlinePluginInfo,
    downloadManager: PluginDownloadManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }

    val isDownloaded = remember(plugin.pluginInfo.fileName) {
        downloadManager.getPluginFile(plugin.pluginInfo.fileName) != null
    }

    fun startDownload() {
        if (isDownloading) return
        isDownloading = true
        downloadState = DownloadState.Downloading(0)

        coroutineScope.launch(Dispatchers.IO) {
            val url = "https://plugin.suzhelan.top/api/plugin/downloadPlugin?id=${plugin.cloudId}"
            val result = downloadManager.downloadPlugin(
                pluginName = plugin.pluginInfo.name,
                downloadUrl = url,
                fileName = plugin.pluginInfo.fileName
            ) { progress ->
                downloadProgress = progress
                downloadState = DownloadState.Downloading(progress)
            }

            isDownloading = false
            if (result.isSuccess) {
                downloadState = DownloadState.Success
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "下载成功", Toast.LENGTH_SHORT).show()
                }
            } else {
                downloadState = DownloadState.Failed
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = plugin.pluginInfo.name,
                style = MiuixTheme.textStyles.body1,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "作者: ${plugin.pluginInfo.author}",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                )
                Text(
                    text = "v${plugin.pluginInfo.version}",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MiuixTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                    .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                    .padding(12.dp)
            ) {
                Text(
                    text = "简介",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                SelectionContainer {
                    Text(
                        text = plugin.pluginInfo.description,
                        style = MiuixTheme.textStyles.body2,
                        maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 2
                    )
                }
            }

            if (plugin.pluginInfo.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    plugin.pluginInfo.tags.forEach { tag ->
                        TagChip(tag = tag)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                when (downloadState) {
                    is DownloadState.Idle -> {
                        if (isDownloaded) {
                            Text(
                                text = "已下载",
                                style = MiuixTheme.textStyles.body2,
                                color = Color(0xFF4CAF50)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MiuixTheme.colorScheme.primary)
                                    .clickable { startDownload() }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "下载",
                                    color = Color.White
                                )
                            }
                        }
                    }
                    is DownloadState.Downloading -> {
                        Text(
                            text = "下载中 ${(downloadState as DownloadState.Downloading).progress}%",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )
                    }
                    is DownloadState.Success -> {
                        Text(
                            text = "下载完成",
                            style = MiuixTheme.textStyles.body2,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    is DownloadState.Failed -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF44336))
                                .clickable { startDownload() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "重试",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TagChip(tag: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = MiuixTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = tag,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.primary
        )
    }
}
