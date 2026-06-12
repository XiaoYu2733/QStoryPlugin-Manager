package hai.qstory.plugin.manager

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import hai.qstory.plugin.manager.data.OnlinePluginInfo
import hai.qstory.plugin.manager.manager.PluginDownloadManager
import hai.qstory.plugin.manager.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Settings

sealed class DownloadState {
    data object Idle : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    data object Success : DownloadState()
    data object Failed : DownloadState()
}

val SCRIPT_TAGS = listOf("全部", "群聊辅助", "娱乐功能", "功能扩展", "综合脚本", "官方脚本")

@Composable
fun HomePage(
    navigator: AppNavigator
) {
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
    var selectedStatus by remember { mutableStateOf("全部状态") }
    var searchText by remember { mutableStateOf("") }
    var showTagDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    val statusOptions = listOf("全部状态", "已上架", "已下架", "待审核")

    val filteredPlugins = remember(pluginList, selectedTag, selectedStatus, searchText) {
        pluginList.filter { plugin ->
            val tagMatch = if (selectedTag == "全部") {
                true
            } else {
                val isOfficial = selectedTag == "官方脚本" && plugin.pluginInfo.tags.contains("官方")
                isOfficial || plugin.pluginInfo.tags.contains(selectedTag)
            }

            val statusMatch = when (selectedStatus) {
                "已上架" -> plugin.onlineStatus == 1
                "已下架" -> plugin.onlineStatus == 0
                "待审核" -> plugin.auditStatus == 0
                else -> true
            }

            val searchMatch = if (searchText.isBlank()) {
                true
            } else {
                plugin.pluginInfo.name.contains(searchText, ignoreCase = true) ||
                        plugin.pluginInfo.author.contains(searchText, ignoreCase = true)
            }

            tagMatch && statusMatch && searchMatch
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
                        onClick = { showStatusDialog = !showStatusDialog },
                        modifier = Modifier
                            .height(56.dp)
                            .width(100.dp)
                    ) {
                        Text(selectedStatus)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showTagDialog = !showTagDialog },
                        modifier = Modifier
                            .height(56.dp)
                            .width(100.dp)
                    ) {
                        Text(selectedTag)
                    }
                }

                if (showStatusDialog) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column {
                        statusOptions.forEach { status ->
                            StatusOptionButton(
                                label = status,
                                selectedStatus = selectedStatus,
                                onClick = {
                                    selectedStatus = status
                                    showStatusDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    InfiniteProgressIndicator(
                        color = MiuixTheme.colorScheme.primary
                    )
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
                    navigator = navigator
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
    navigator: AppNavigator
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp)),
        pressFeedbackType = top.yukonga.miuix.kmp.utils.PressFeedbackType.Sink,
        showIndication = true,
        onClick = {
            navigator.push(Route.PluginDetail(plugin.cloudId))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plugin.pluginInfo.name,
                            style = MiuixTheme.textStyles.body1,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "v${plugin.pluginInfo.version}",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plugin.pluginInfo.author,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun PluginDetailPage(
    cloudId: String,
    navigator: AppNavigator
) {
    val context = LocalContext.current
    val downloadManager = remember { PluginDownloadManager(context) }
    val coroutineScope = rememberCoroutineScope()
    var plugin by remember { mutableStateOf<OnlinePluginInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadedFileName by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf(0) }

    LaunchedEffect(cloudId) {
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.pluginService.getOnlinePluginList()
                if (response.isSuccess()) {
                    plugin = response.data?.find { it.cloudId == cloudId }
                }
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(cloudId) {
        val files = downloadManager.getDownloadedFiles()
        val matchingFile = files.find { it.name.contains(cloudId) }
        downloadedFileName = matchingFile?.fileName
    }

    val isDownloaded = remember(downloadedFileName) {
        downloadedFileName != null
    }

    fun startDownload() {
        val currentPlugin = plugin ?: return
        if (isDownloading) return
        isDownloading = true
        downloadState = DownloadState.Downloading(0)

        coroutineScope.launch(Dispatchers.IO) {
            val result = downloadManager.downloadPlugin(
                pluginName = currentPlugin.pluginInfo.name,
                cloudId = currentPlugin.cloudId,
                serverFileName = currentPlugin.pluginInfo.fileName
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    InfiniteProgressIndicator(
                        color = MiuixTheme.colorScheme.primary
                    )
                }
            }
            plugin == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "脚本不存在",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 顶部间距，为返回按钮留出空间
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }

                    // 头部大卡片
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                // 脚本图标和名称
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 显示脚本图标
                                    val images = plugin!!.pluginInfo.images
                                    val iconUrl = images?.let {
                                        if (it.iconStatus == 1 && !it.iconFilename.isNullOrEmpty()) {
                                            "https://plugin.suzhelan.top/api/plugin/images/${plugin!!.cloudId}/${it.iconFilename}"
                                        } else null
                                    }

                                    if (iconUrl != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(iconUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "脚本图标",
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                    }

                                    // 脚本名称和版本
                                    Column {
                                        Text(
                                            text = plugin!!.pluginInfo.name,
                                            style = MiuixTheme.textStyles.body1.copy(fontSize = 24.sp),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "v${plugin!!.pluginInfo.version}",
                                            style = MiuixTheme.textStyles.body2,
                                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // 作者信息
                                Text(
                                    text = "作者: ${plugin!!.pluginInfo.author}",
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // 脚本ID
                                Text(
                                    text = "ID: ${plugin!!.pluginInfo.pluginId}",
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                                )
                            }
                        }
                    }

                    // 简介卡片
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp)
                            ) {
                                Text(
                                    text = "简介",
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                SelectionContainer {
                                    Text(
                                        text = plugin!!.pluginInfo.description,
                                        style = MiuixTheme.textStyles.body1,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }

                    // 标签卡片
                    if (plugin!!.pluginInfo.tags.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp)
                                ) {
                                    androidx.compose.foundation.layout.FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        plugin!!.pluginInfo.tags.forEach { tag ->
                                            TagChip(tag = tag)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 预览图卡片
                    val images = plugin!!.pluginInfo.images
                    if (images?.previewStatus == 1 && !images.previewFilename.isNullOrEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp)
                                ) {
                                    Text(
                                        text = "预览图",
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        images.previewFilename.forEach { filename ->
                                            val previewUrl = "https://plugin.suzhelan.top/api/plugin/images/${plugin!!.cloudId}/$filename"
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(previewUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "预览图",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 底部下载区域
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = when (downloadState) {
                                            is DownloadState.Idle -> if (isDownloaded) "已下载" else "准备下载"
                                            is DownloadState.Downloading -> "下载中..."
                                            is DownloadState.Success -> "下载完成"
                                            is DownloadState.Failed -> "下载失败"
                                        },
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                                    )
                                    when (downloadState) {
                                        is DownloadState.Downloading -> {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${(downloadState as DownloadState.Downloading).progress}%",
                                                style = MiuixTheme.textStyles.body2,
                                                color = MiuixTheme.colorScheme.primary
                                            )
                                        }
                                        else -> {}
                                    }
                                }

                                when (downloadState) {
                                    is DownloadState.Idle -> {
                                        if (isDownloaded) {
                                            Icon(
                                                imageVector = MiuixIcons.Info,
                                                contentDescription = "已下载",
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(28.dp)
                                            )
                                        } else {
                                            Button(
                                                onClick = { startDownload() }
                                            ) {
                                                Text("下载")
                                            }
                                        }
                                    }
                                    is DownloadState.Downloading -> {
                                        Text(
                                            text = "${(downloadState as DownloadState.Downloading).progress}%",
                                            style = MiuixTheme.textStyles.body1,
                                            color = MiuixTheme.colorScheme.primary
                                        )
                                    }
                                    is DownloadState.Success -> {
                                        Icon(
                                            imageVector = MiuixIcons.Info,
                                            contentDescription = "完成",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    is DownloadState.Failed -> {
                                        Button(
                                            onClick = { startDownload() }
                                        ) {
                                            Text("重试")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 底部间距
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }

        // 悬浮返回按钮放在最后，确保在最上层
        IconButton(
            onClick = { navigator.pop() },
            modifier = Modifier
                .padding(top = 50.dp, start = 12.dp)
                .size(44.dp)
        ) {
            Icon(
                imageVector = MiuixIcons.Back,
                contentDescription = "返回",
                tint = MiuixTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun TagChip(tag: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MiuixTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = tag,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSecondaryContainer
        )
    }
}
