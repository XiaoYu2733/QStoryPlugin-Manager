package hai.qstory.plugin.manager

import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import hai.qstory.plugin.manager.data.AiReviewRecord
import hai.qstory.plugin.manager.data.ComplianceIssue
import hai.qstory.plugin.manager.data.ScriptDetail
import hai.qstory.plugin.manager.data.ScriptListItem
import hai.qstory.plugin.manager.manager.PluginDownloadManager
import hai.qstory.plugin.manager.network.RetrofitClient
import hai.qstory.plugin.manager.preferences.PreferencesManager
import hai.qstory.plugin.manager.ui.util.BlurredBar
import hai.qstory.plugin.manager.ui.util.rememberBlurBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Download
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
    var pluginList by remember { mutableStateOf<List<ScriptListItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val firstPage = RetrofitClient.pluginService.getPublicScriptList()
                if (!firstPage.isSuccess()) {
                    errorMessage = firstPage.message
                    return@withContext
                }
                val data = firstPage.data ?: return@withContext
                val allItems = mutableListOf<ScriptListItem>()
                allItems.addAll(data.list)

                if (data.totalPages > 1) {
                    coroutineScope {
                        val deferred = (2..data.totalPages).map { page ->
                            async {
                                RetrofitClient.pluginService.getPublicScriptList(page = page)
                            }
                        }
                        deferred.awaitAll().forEach { response ->
                            if (response.isSuccess()) {
                                response.data?.list?.let { allItems.addAll(it) }
                            }
                        }
                    }
                }
                pluginList = allItems
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

    val statusOptions = listOf("全部状态", "待审核", "已通过", "未通过")

    val filteredPlugins = remember(pluginList, selectedTag, selectedStatus, searchText) {
        pluginList.filter { plugin ->
            val tagMatch = if (selectedTag == "全部") {
                true
            } else {
                val isOfficial = selectedTag == "官方脚本" && plugin.tags.contains("官方")
                isOfficial || plugin.tags.contains(selectedTag)
            }

            val statusMatch = when (selectedStatus) {
                "待审核" -> plugin.auditStatus == 0
                "已通过" -> plugin.onlineStatus == 1
                "未通过" -> plugin.onlineStatus == 0 || plugin.onlineStatus == -1
                else -> true
            }

            val searchMatch = if (searchText.isBlank()) {
                true
            } else {
                plugin.name.contains(searchText, ignoreCase = true) ||
                        plugin.author.contains(searchText, ignoreCase = true)
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
                            text = if (searchText.isNotBlank() || selectedTag != "全部" || selectedStatus != "全部状态") {
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
fun StatusOptionButton(
    label: String,
    selectedStatus: String,
    onClick: () -> Unit
) {
    val isSelected = selectedStatus == label
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

private fun getStatusLabel(plugin: ScriptListItem): Pair<String, Color> {
    return when {
        plugin.auditStatus == 0 -> "待审核" to Color(0xFFFF9800)
        plugin.auditStatus == 2 -> "未通过" to Color(0xFFF44336)
        plugin.onlineStatus == 1 -> "已通过" to Color(0xFF4CAF50)
        plugin.onlineStatus == 0 -> "未通过" to Color(0xFFF44336)
        plugin.onlineStatus == -1 -> "未通过" to Color(0xFF9E9E9E)
        else -> "未通过" to Color(0xFF9E9E9E)
    }
}

@Composable
fun PluginCard(
    plugin: ScriptListItem,
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
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plugin.name,
                            style = MiuixTheme.textStyles.body1,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "v${plugin.version}",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plugin.author,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                }
                val (statusText, statusColor) = getStatusLabel(plugin)
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
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
    var plugin by remember { mutableStateOf<ScriptDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadedFileName by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf(0) }
    var aiReview by remember { mutableStateOf<AiReviewRecord?>(null) }
    var aiReviewLoading by remember { mutableStateOf(true) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var downloadDirPath by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val showTopBar by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 80
        }
    }

    val enableBlur = PreferencesManager.enableBlur
    val backdrop = rememberBlurBackdrop(enableBlur)
    val blurActive = backdrop != null

    LaunchedEffect(cloudId) {
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.pluginService.getPublicScriptDetail(cloudId)
                if (response.isSuccess()) {
                    plugin = response.data
                }
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(cloudId) {
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.pluginService.getPublicScriptAiReview(cloudId)
                if (response.isSuccess()) {
                    aiReview = response.data
                }
            } finally {
                aiReviewLoading = false
            }
        }
    }

    LaunchedEffect(cloudId) {
        val files = downloadManager.getDownloadedFiles()
        val matchingFile = files.find { it.name.contains(cloudId) }
        downloadedFileName = matchingFile?.fileName
    }

    fun startDownload() {
        val currentPlugin = plugin ?: return
        if (isDownloading) return
        isDownloading = true
        downloadProgress = 0
        downloadState = DownloadState.Downloading(0)

        coroutineScope.launch(Dispatchers.IO) {
            val result = downloadManager.downloadPlugin(
                pluginName = currentPlugin.name,
                cloudId = currentPlugin.cloudId,
                serverFileName = currentPlugin.fileName
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
                    downloadDirPath = downloadManager.pluginDir.absolutePath
                    showSuccessDialog = true
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
        Box(
            modifier = if (blurActive) Modifier.fillMaxSize().layerBackdrop(backdrop!!) else Modifier.fillMaxSize()
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
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                    item {
                        Spacer(modifier = Modifier.height(88.dp))
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val images = plugin!!.images
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

                                    Column {
                                        Text(
                                            text = plugin!!.name,
                                            style = MiuixTheme.textStyles.body1.copy(fontSize = 24.sp),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "v${plugin!!.version}",
                                            style = MiuixTheme.textStyles.body2,
                                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "作者: ${plugin!!.author}",
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "ID: ${plugin!!.pluginId}",
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                                )

                                if (plugin!!.tags.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "标签：${plugin!!.tags.joinToString(" ")}",
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                                    )
                                }
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
                                        text = plugin!!.description,
                                        style = MiuixTheme.textStyles.body1,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }

                    // AI 评审卡片
                    item {
                        AiReviewCard(aiReview = aiReview, isLoading = aiReviewLoading)
                    }

                    // 预览图卡片
                    val images = plugin!!.images
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "预览图",
                                            style = MiuixTheme.textStyles.body2,
                                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "右滑显示更多",
                                            fontSize = 11.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(images.previewFilename) { filename ->
                                            val previewUrl = "https://plugin.suzhelan.top/api/plugin/images/${plugin!!.cloudId}/$filename"
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(previewUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "预览图",
                                                modifier = Modifier
                                                    .width(260.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }

        }

        // 顶栏 — 始终显示，下滑时出现模糊/背景
        BlurredBar(backdrop = backdrop) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (!blurActive) Modifier.background(MiuixTheme.colorScheme.surface)
                        else Modifier
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                IconButton(
                    onClick = { navigator.pop() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = MiuixIcons.Back,
                        contentDescription = "返回",
                        tint = MiuixTheme.colorScheme.onBackground
                    )
                }

                if (showTopBar && plugin != null) {
                    Text(
                        text = plugin!!.name,
                        style = MiuixTheme.textStyles.body1,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                when {
                    isDownloading -> {
                        Text(
                            text = "${downloadProgress}%",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                    else -> {
                        val iconTint = when (downloadState) {
                            is DownloadState.Success -> Color(0xFF4CAF50)
                            is DownloadState.Failed -> Color(0xFFF44336)
                            else -> MiuixTheme.colorScheme.onBackground
                        }
                        IconButton(
                            onClick = { startDownload() },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Download,
                                contentDescription = "下载",
                                tint = iconTint
                            )
                        }
                    }
                }
            }
            }
        }

        if (showSuccessDialog) {
            DownloadSuccessDialog(
                dirPath = downloadDirPath,
                onDismiss = { showSuccessDialog = false }
            )
        }
    }
}

@Composable
fun DownloadSuccessDialog(
    dirPath: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = { /* 只能点击确定关闭 */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "下载成功",
                    style = MiuixTheme.textStyles.body1,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "脚本已下载到以下目录，点击可复制",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dirPath,
                    style = MiuixTheme.textStyles.body2,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("download_path", dirPath)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                        }
                        .padding(12.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("确定")
                }
            }
        }
    }
}

@Composable
fun AiReviewCard(
    aiReview: AiReviewRecord?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "AI 评审",
                style = MiuixTheme.textStyles.body1,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InfiniteProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MiuixTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "加载中...",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )
                    }
                }
                aiReview == null -> {
                    Text(
                        text = "该脚本尚未进行 AI 评审",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                }
                aiReview.reviewStatus == 0 -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InfiniteProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "AI 评审中，请稍后查看...",
                            style = MiuixTheme.textStyles.body2,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
                aiReview.reviewStatus == 2 -> {
                    Text(
                        text = "AI 评审失败",
                        style = MiuixTheme.textStyles.body2,
                        color = Color(0xFFF44336)
                    )
                    if (!aiReview.errorMessage.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = aiReview.errorMessage,
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )
                    }
                }
                aiReview.reviewStatus == 1 -> {
                    val result = aiReview.reviewResult ?: return@Card

                    // 风险等级摘要
                    RiskSummary(
                        riskLevel = result.riskLevel,
                        summary = result.summary,
                        passed = result.compliance.passed
                    )

                    // 发现的问题
                    val issues = result.compliance.issues
                    if (!issues.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        IssuesList(issues = issues)
                    }

                    // 改进建议
                    val suggestions = result.suggestions
                    if (!suggestions.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        SuggestionsList(suggestions = suggestions)
                    }

                    // Token 与模型
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "模型: ${aiReview.modelUsed ?: "未知"}",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )
                        Text(
                            text = "Token: ${aiReview.tokensUsed ?: 0}",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RiskSummary(
    riskLevel: String,
    summary: String?,
    passed: Boolean
) {
    val riskColor = when (riskLevel) {
        "high" -> Color(0xFFF44336)
        "medium" -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }
    val riskLabel = when (riskLevel) {
        "high" -> "高风险"
        "medium" -> "中风险"
        else -> "低风险"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(riskColor.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = riskLabel,
                style = MiuixTheme.textStyles.body2,
                fontWeight = FontWeight.Medium,
                color = riskColor
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (passed) "合规通过" else "存在严重问题",
            fontSize = 12.sp,
            color = if (passed) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }

    if (!summary.isNullOrEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = summary,
            style = MiuixTheme.textStyles.body2,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun IssuesList(issues: List<ComplianceIssue>) {
    Column {
        Text(
            text = "发现的问题 (${issues.size})",
            style = MiuixTheme.textStyles.body2,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        issues.forEach { issue ->
            IssueItem(issue = issue)
        }
    }
}

@Composable
private fun IssueItem(issue: ComplianceIssue) {
    val levelColor = when (issue.level) {
        "error" -> Color(0xFFF44336)
        "warning" -> Color(0xFFFF9800)
        else -> Color(0xFF2196F3)
    }
    val levelLabel = when (issue.level) {
        "error" -> "严重"
        "warning" -> "警告"
        else -> "提示"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 级别 + 分类
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(levelColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = levelLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = levelColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = issue.category,
                    fontSize = 11.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 问题描述
            Text(
                text = issue.message,
                style = MiuixTheme.textStyles.body2,
                lineHeight = 18.sp
            )

            // 位置信息
            if (!issue.location.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = issue.location,
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }

            // 代码片段
            if (!issue.codeSnippet.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 4.dp
                ) {
                    SelectionContainer {
                        Text(
                            text = issue.codeSnippet,
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionsList(suggestions: List<String>) {
    Column {
        Text(
            text = "改进建议 (${suggestions.size})",
            style = MiuixTheme.textStyles.body2,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        suggestions.forEachIndexed { index, suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "${index + 1}.",
                    style = MiuixTheme.textStyles.body2,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = suggestion,
                    style = MiuixTheme.textStyles.body2,
                    lineHeight = 18.sp
                )
            }
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
