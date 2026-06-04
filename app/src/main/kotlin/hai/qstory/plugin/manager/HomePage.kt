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
import androidx.compose.foundation.layout.width
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
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

sealed class DownloadState {
    data object Idle : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    data object Success : DownloadState()
    data object Failed : DownloadState()
}

val SCRIPT_TAGS = listOf("全部", "群聊辅助", "娱乐功能", "功能扩展", "综合脚本", "官方脚本")

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

    var selectedTag by remember { mutableStateOf("全部") }
    var searchText by remember { mutableStateOf("") }
    var showTagDialog by remember { mutableStateOf(false) }

    val filteredPlugins = remember(pluginList, selectedTag, searchText) {
        pluginList.filter { plugin ->
            val tagMatch = if (selectedTag == "全部") {
                true
            } else {
                val isOfficial = selectedTag == "官方脚本" && plugin.pluginInfo.tags.contains("官方")
                isOfficial || plugin.pluginInfo.tags.contains(selectedTag)
            }

            val searchMatch = if (searchText.isBlank()) {
                true
            } else {
                plugin.pluginInfo.name.contains(searchText, ignoreCase = true) ||
                        plugin.pluginInfo.author.contains(searchText, ignoreCase = true)
            }

            tagMatch && searchMatch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // 搜索和筛选栏
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = "搜索脚本",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showTagDialog = !showTagDialog },
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text(selectedTag)
                    }
                }

                if (showTagDialog) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column {
                        SCRIPT_TAGS.forEach { tag ->
                            TagOptionButton(
                                label = tag,
                                selectedTag = selectedTag,
                                onClick = {
                                    selectedTag = tag
                                    showTagDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        // 脚本列表
        if (isLoading) {
            item {
                Card(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = "加载中...")
                    }
                }
            }
        } else if (errorMessage != null) {
            item {
                Card(
                    modifier = Modifier.padding(16.dp)
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
        } else if (filteredPlugins.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = "暂无脚本")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchText.isNotBlank() || selectedTag != "全部") {
                                "未找到匹配的脚本"
                            } else {
                                "这里是存放脚本的页面"
                            },
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )
                    }
                }
            }
        } else {
            items(filteredPlugins) { plugin ->
                PluginCard(
                    plugin = plugin,
                    downloadManager = downloadManager
                )
            }
        }
    }
}

@Composable
fun TagOptionButton(
    label: String,
    selectedTag: String,
    onClick: () -> Unit
) {
    val isSelected = selectedTag == label
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label)
            if (isSelected) {
                Text("✓")
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
    var isExpanded by remember { mutableStateOf(false) }
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadedFileName by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf(0) }

    LaunchedEffect(plugin.cloudId) {
        val files = downloadManager.getDownloadedFiles()
        val matchingFile = files.find { it.name.contains(plugin.cloudId) }
        downloadedFileName = matchingFile?.fileName
    }

    val isDownloaded = remember(downloadedFileName) {
        downloadedFileName != null
    }

    fun startDownload() {
        if (isDownloading) return
        isDownloading = true
        downloadState = DownloadState.Downloading(0)

        coroutineScope.launch(Dispatchers.IO) {
            val result = downloadManager.downloadPlugin(
                pluginName = plugin.pluginInfo.name,
                cloudId = plugin.cloudId,
                serverFileName = plugin.pluginInfo.fileName
            ) { progress ->
                launch(Dispatchers.Main) {
                    downloadProgress = progress
                    downloadState = DownloadState.Downloading(progress)
                }
            }

            isDownloading = false
            launch(Dispatchers.Main) {
                if (result.isSuccess) {
                    downloadedFileName = result.getOrNull()
                    downloadState = DownloadState.Success
                    Toast.makeText(context, "下载成功", Toast.LENGTH_SHORT).show()
                } else {
                    downloadState = DownloadState.Failed
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plugin.pluginInfo.name,
                        style = MiuixTheme.textStyles.body1,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${plugin.pluginInfo.author} · v${plugin.pluginInfo.version}",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MiuixTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isExpanded) "收起" else "详情",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.primary
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MiuixTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
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
                            style = MiuixTheme.textStyles.body2
                        )
                    }
                }

                if (plugin.pluginInfo.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "标签",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.layout.FlowRow(
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
                                    text = "✓ 已下载",
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
                                text = "✓ 下载完成",
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

                HorizontalDivider()
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
