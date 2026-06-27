package top.lovehaifeng.qstory.manager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import top.lovehaifeng.qstory.manager.data.AiReviewRecord
import top.lovehaifeng.qstory.manager.data.ComplianceIssue
import top.lovehaifeng.qstory.manager.data.ScriptListItem
import top.lovehaifeng.qstory.manager.manager.PluginDownloadManager
import top.lovehaifeng.qstory.manager.preferences.PreferencesManager
import top.lovehaifeng.qstory.manager.repository.PluginRepository
import top.lovehaifeng.qstory.manager.ui.component.AdaptiveButton
import top.lovehaifeng.qstory.manager.ui.component.AdaptiveCard
import top.lovehaifeng.qstory.manager.ui.component.AdaptiveInfiniteProgressIndicator
import top.lovehaifeng.qstory.manager.ui.component.AdaptiveText
import top.lovehaifeng.qstory.manager.ui.component.AdaptiveTextField
import top.lovehaifeng.qstory.manager.ui.component.adaptiveOnSecondaryContainer
import top.lovehaifeng.qstory.manager.ui.component.adaptiveOnSurfaceSecondary
import top.lovehaifeng.qstory.manager.ui.component.adaptiveOnSurfaceVariantSummary
import top.lovehaifeng.qstory.manager.ui.component.adaptivePrimaryColor
import top.lovehaifeng.qstory.manager.ui.component.adaptiveSecondaryContainer
import top.lovehaifeng.qstory.manager.ui.theme.LocalUiMode
import top.lovehaifeng.qstory.manager.ui.theme.UiMode
import top.lovehaifeng.qstory.manager.ui.util.BlurredBar
import top.lovehaifeng.qstory.manager.ui.util.TopBarSurface
import top.lovehaifeng.qstory.manager.ui.util.rememberBlurBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.menu.OverlayDropdownMenu
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.Text as M3Text

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
    val scriptListState by PluginRepository.scriptList.collectAsState()

    LaunchedEffect(Unit) {
        PluginRepository.ensureScriptListLoaded()
    }

    val pluginList = scriptListState.data ?: emptyList()
    val isLoading = scriptListState.isLoading && pluginList.isEmpty()
    val errorMessage = scriptListState.error

    var selectedTag by remember { mutableStateOf("全部") }
    var selectedStatus by remember { mutableStateOf("全部状态") }
    var searchText by remember { mutableStateOf("") }

    val statusOptions = listOf("全部状态", "待审核", "已通过", "未通过")

    val listState = rememberLazyListState()
    val showTopBar by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 80
        }
    }
    val enableBlur = PreferencesManager.enableBlur
    val backdrop = rememberBlurBackdrop(enableBlur)
    val blurActive = backdrop != null
    val statusBarHeightDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = if (blurActive) Modifier.fillMaxSize().layerBackdrop(backdrop!!) else Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
            // Status bar + top bar spacer
            item {
                Spacer(modifier = Modifier.height(statusBarHeightDp + 56.dp))
            }

            // 搜索和筛选栏
            item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                AdaptiveTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = "搜索脚本",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val statusEntry = remember(selectedStatus) {
                        DropdownEntry(
                            items = statusOptions.map { option ->
                                DropdownItem(
                                    text = option,
                                    selected = option == selectedStatus,
                                    onClick = { selectedStatus = option },
                                )
                            }
                        )
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                    ) {
                        OverlayDropdownMenu(
                            entry = statusEntry,
                            title = selectedStatus,
                        )
                    }
                    val tagEntry = remember(selectedTag) {
                        DropdownEntry(
                            items = SCRIPT_TAGS.map { option ->
                                DropdownItem(
                                    text = option,
                                    selected = option == selectedTag,
                                    onClick = { selectedTag = option },
                                )
                            }
                        )
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                    ) {
                        OverlayDropdownMenu(
                            entry = tagEntry,
                            title = selectedTag,
                        )
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
                    AdaptiveInfiniteProgressIndicator()
                }
            }
        } else if (errorMessage != null) {
            item {
                AdaptiveCard(modifier = Modifier.padding(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AdaptiveText(text = "加载失败")
                        Spacer(modifier = Modifier.height(8.dp))
                        AdaptiveText(
                            text = errorMessage ?: "未知错误",
                            color = adaptiveOnSurfaceSecondary()
                        )
                    }
                }
            }
        } else if (filteredPlugins.isEmpty()) {
            item {
                AdaptiveCard(modifier = Modifier.padding(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AdaptiveText(text = "暂无脚本")
                        Spacer(modifier = Modifier.height(8.dp))
                        AdaptiveText(
                            text = if (searchText.isNotBlank() || selectedTag != "全部" || selectedStatus != "全部状态") {
                                "未找到匹配的脚本"
                            } else {
                                "这里是存放脚本的页面"
                            },
                            color = adaptiveOnSurfaceSecondary()
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

        item { Spacer(modifier = Modifier.height(80.dp)) }
        }
        }

        TopBarSurface(backdrop = backdrop, blurActive = blurActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showTopBar) {
                    Text(
                        text = "QStoryPlugin",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
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
    AdaptiveCard(
        onClick = { navigator.push(Route.PluginDetail(plugin.cloudId)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val (statusText, statusColor) = getStatusLabel(plugin)
        if (LocalUiMode.current == UiMode.Material) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            M3Text(text = plugin.name, style = M3MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = M3MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(8.dp))
                            M3Text(text = "v${plugin.version}", style = M3MaterialTheme.typography.bodySmall, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        M3Text(text = plugin.author, style = M3MaterialTheme.typography.bodyMedium, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    M3Text(text = statusText, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = plugin.name, style = MiuixTheme.textStyles.body1, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "v${plugin.version}", style = MiuixTheme.textStyles.body2, color = adaptiveOnSurfaceSecondary())
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = plugin.author, style = MiuixTheme.textStyles.body2, color = adaptiveOnSurfaceSecondary())
                    }
                    Text(text = statusText, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun PluginDetailPage(
    cloudId: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val downloadManager = remember { PluginDownloadManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val detailState by PluginRepository.scriptDetailState(cloudId).collectAsState()
    val aiReviewState by PluginRepository.aiReviewState(cloudId).collectAsState()
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadedFileName by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf(0) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var downloadDirPath by remember { mutableStateOf("") }

    val plugin = detailState.data
    val isLoading = detailState.isLoading && plugin == null
    val aiReview = aiReviewState.data
    val aiReviewLoading = aiReviewState.isLoading && aiReview == null

    val enableBlur = PreferencesManager.enableBlur
    val backdrop = rememberBlurBackdrop(enableBlur)
    val blurActive = backdrop != null

    LaunchedEffect(cloudId) {
        PluginRepository.ensureScriptDetailLoaded(cloudId)
        PluginRepository.ensureAiReviewLoaded(cloudId)
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

    val tabs = listOf("自述", "AI 评审", "信息")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val statusBarHeightDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerHeight = statusBarHeightDp + 56.dp + 48.dp

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 内容层 — 每个 tab 页面自己负责 layerBackdrop（参照 KSU）
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AdaptiveInfiniteProgressIndicator()
                }
            }
            plugin == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AdaptiveText(
                        text = "脚本不存在",
                        color = adaptiveOnSurfaceSecondary()
                    )
                }
            }
            else -> {
                val currentPlugin = plugin!!

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    val pageModifier = if (blurActive) Modifier.fillMaxSize().layerBackdrop(backdrop!!) else Modifier.fillMaxSize()
                    Box(modifier = pageModifier) {
                        when (page) {
                            0 -> ReadmeTab(plugin = currentPlugin, topPadding = headerHeight)
                            1 -> AiReviewTab(aiReview = aiReview, isLoading = aiReviewLoading, topPadding = headerHeight)
                            2 -> InfoTab(plugin = currentPlugin, topPadding = headerHeight)
                        }
                    }
                }
            }
        }

        // 顶栏 + Tab — 共用模糊（参照 KSU 的 BlurredBar 包裹 TopAppBar + bottomContent TabRow）
        BlurredBar(backdrop = backdrop, blurActive = blurActive) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (!blurActive) Modifier.background(MiuixTheme.colorScheme.surface)
                        else Modifier
                    )
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                            tint = MiuixTheme.colorScheme.onBackground
                        )
                    }

                    if (plugin != null) {
                        AdaptiveText(
                            text = plugin!!.name,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    when {
                        isDownloading -> {
                            AdaptiveText(
                                text = "${downloadProgress}%",
                                color = adaptivePrimaryColor(),
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

                TabRow(
                    tabs = tabs,
                    selectedTabIndex = pagerState.currentPage,
                    onTabSelected = { index ->
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    },
                    height = 48.dp,
                )
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
        AdaptiveCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AdaptiveText(
                    text = "下载成功",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                AdaptiveText(
                    text = "脚本已下载到以下目录，点击可复制",
                    color = adaptiveOnSurfaceSecondary()
                )
                Spacer(modifier = Modifier.height(8.dp))
                AdaptiveText(
                    text = dirPath,
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
                AdaptiveButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AdaptiveText(text = "确定")
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        when {
            isLoading -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AdaptiveInfiniteProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    AdaptiveText(
                        text = "加载中...",
                        color = adaptiveOnSurfaceSecondary()
                    )
                }
            }
            aiReview == null -> {
                AdaptiveText(
                    text = "该脚本尚未进行 AI 评审",
                    color = adaptiveOnSurfaceSecondary()
                )
            }
            aiReview.reviewStatus == 0 -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    AdaptiveText(
                        text = "AI 评审中，请稍后查看...",
                        color = Color(0xFFFF9800)
                    )
                }
            }
            aiReview.reviewStatus == 2 -> {
                AdaptiveText(
                    text = "AI 评审失败",
                    color = Color(0xFFF44336)
                )
                if (!aiReview.errorMessage.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    AdaptiveText(
                        text = aiReview.errorMessage,
                        color = adaptiveOnSurfaceSecondary()
                    )
                }
            }
            aiReview.reviewStatus == 1 -> {
                val result = aiReview.reviewResult
                if (result != null) {
                    RiskSummary(
                        riskLevel = result.riskLevel,
                        summary = result.summary,
                        passed = result.compliance.passed
                    )

                    val issues = result.compliance.issues
                    if (!issues.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        IssuesList(issues = issues)
                    }

                    val suggestions = result.suggestions
                    if (!suggestions.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        SuggestionsList(suggestions = suggestions)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AdaptiveText(
                            text = "模型: ${aiReview.modelUsed ?: "未知"}",
                            color = adaptiveOnSurfaceSecondary()
                        )
                        AdaptiveText(
                            text = "Token: ${aiReview.tokensUsed ?: 0}",
                            color = adaptiveOnSurfaceSecondary()
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
            AdaptiveText(
                text = riskLabel,
                fontWeight = FontWeight.Medium,
                color = riskColor
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        AdaptiveText(
            text = if (passed) "合规通过" else "存在严重问题",
            fontSize = 12.sp,
            color = if (passed) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }

    if (!summary.isNullOrEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        AdaptiveText(
            text = summary,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun IssuesList(issues: List<ComplianceIssue>) {
    Column {
        AdaptiveText(
            text = "发现的问题 (${issues.size})",
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(levelColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                AdaptiveText(
                    text = levelLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = levelColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            AdaptiveText(
                text = issue.category,
                fontSize = 11.sp,
                color = adaptiveOnSurfaceVariantSummary()
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        AdaptiveText(
            text = issue.message,
            fontSize = 14.sp
        )

        if (!issue.location.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            AdaptiveText(
                text = issue.location,
                fontSize = 12.sp,
                color = adaptiveOnSurfaceVariantSummary()
            )
        }

        if (!issue.codeSnippet.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            SelectionContainer {
                AdaptiveText(
                    text = issue.codeSnippet,
                    fontSize = 12.sp,
                    color = adaptiveOnSurfaceVariantSummary(),
                )
            }
        }
    }
}

@Composable
private fun SuggestionsList(suggestions: List<String>) {
    Column {
        AdaptiveText(
            text = "改进建议 (${suggestions.size})",
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        suggestions.forEachIndexed { index, suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                AdaptiveText(
                    text = "${index + 1}.",
                    fontWeight = FontWeight.Medium,
                    color = adaptivePrimaryColor()
                )
                Spacer(modifier = Modifier.width(6.dp))
                AdaptiveText(
                    text = suggestion,
                    fontSize = 14.sp
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
            .background(adaptiveSecondaryContainer().copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        AdaptiveText(
            text = tag,
            color = adaptiveOnSecondaryContainer()
        )
    }
}

// ── 标签页 ──

@Composable
private fun ReadmeTab(
    plugin: top.lovehaifeng.qstory.manager.data.ScriptDetail,
    topPadding: androidx.compose.ui.unit.Dp,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = topPadding + 12.dp,
            bottom = 20.dp,
        ),
    ) {
        item {
            SelectionContainer {
                AdaptiveText(
                    text = plugin.description.ifBlank { "暂无简介" },
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun AiReviewTab(
    aiReview: AiReviewRecord?,
    isLoading: Boolean,
    topPadding: androidx.compose.ui.unit.Dp,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = topPadding + 12.dp,
            bottom = 20.dp,
        ),
    ) {
        item {
            AiReviewCard(aiReview = aiReview, isLoading = isLoading)
        }
    }
}

@Composable
private fun InfoTab(
    plugin: top.lovehaifeng.qstory.manager.data.ScriptDetail,
    topPadding: androidx.compose.ui.unit.Dp,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = topPadding + 12.dp,
            bottom = 20.dp,
        ),
    ) {
        item {
            InfoRow("脚本作者", plugin.author)
            InfoRow("脚本标识", plugin.pluginId)
            InfoRow("脚本版本", plugin.version)
            InfoRow("脚本标签", plugin.tags.joinToString(" · "))
        }

        // 预览图
        val images = plugin.images
        if (images?.previewStatus == 1 && !images.previewFilename.isNullOrEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AdaptiveText(
                    text = "预览图",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(images.previewFilename) { filename ->
                        val previewUrl = "https://plugin.suzhelan.top/api/plugin/images/${plugin.cloudId}/$filename"
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

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
    ) {
        AdaptiveText(
            text = "$label：",
            fontSize = 14.sp,
            color = adaptiveOnSurfaceSecondary(),
        )
        AdaptiveText(
            text = value,
            fontSize = 14.sp,
        )
    }
}

