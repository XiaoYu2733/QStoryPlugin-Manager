package top.lovehaifeng.qstory.manager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.lovehaifeng.qstory.manager.data.PlatformStatistics
import top.lovehaifeng.qstory.manager.preferences.PreferencesManager
import top.lovehaifeng.qstory.manager.repository.PluginRepository
import top.lovehaifeng.qstory.manager.ui.component.AdaptiveCard
import top.lovehaifeng.qstory.manager.ui.component.AdaptiveInfiniteProgressIndicator
import top.lovehaifeng.qstory.manager.ui.component.AdaptiveSmallTitle
import top.lovehaifeng.qstory.manager.ui.component.AdaptiveText
import top.lovehaifeng.qstory.manager.ui.component.adaptiveOnSurfaceVariantSummary
import top.lovehaifeng.qstory.manager.ui.component.adaptivePrimaryColor
import top.lovehaifeng.qstory.manager.ui.util.TopBarSurface
import top.lovehaifeng.qstory.manager.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Text

@Composable
fun StatisticsPage() {
    val statisticsState by PluginRepository.statistics.collectAsState()

    LaunchedEffect(Unit) {
        PluginRepository.ensureStatisticsLoaded()
    }

    val stats = statisticsState.data
    val isLoading = statisticsState.isLoading && stats == null
    val errorMessage = statisticsState.error

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
            item {
                Spacer(modifier = Modifier.height(statusBarHeightDp + 56.dp))
            }

            when {
                    isLoading -> {
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
                    }
                    errorMessage != null -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AdaptiveText(
                                    text = "加载失败: $errorMessage",
                                    color = adaptiveOnSurfaceVariantSummary()
                                )
                            }
                        }
                    }
                    stats != null -> {
                        val data = stats!!

                        item { AdaptiveSmallTitle(text = "平台统计总览") }
                        item { OverviewCards(data) }

                        item { AdaptiveSmallTitle(text = "脚本与审核") }
                        item { ScriptAuditCards(data) }

                        item { AdaptiveSmallTitle(text = "AI 评审统计") }
                        item { AiReviewCards(data) }

                        item { AdaptiveSmallTitle(text = "用户与互动") }
                        item { UserInteractionCards(data) }

                        item { AdaptiveSmallTitle(text = "近期活跃度") }
                        item { RecentActivityCards(data) }

                        item { AdaptiveSmallTitle(text = "AI 风险分布") }
                        item { RiskDistributionCard(data) }

                        item { AdaptiveSmallTitle(text = "标签分布") }
                        item { TagDistributionCard(data) }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
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
                        text = "平台统计",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewCards(data: PlatformStatistics) {
    AdaptiveCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatRow("累计下载", formatNumber(data.downloads.total))
            StatRow("脚本总数", "${data.scripts.total}")
            StatRow("审核总数", "${data.audits.total}")
            StatRow("AI 评审", "${data.aiReviews.total}")
            StatRow("评论总数", "${data.comments.total}")
        }
    }
}

@Composable
private fun ScriptAuditCards(data: PlatformStatistics) {
    AdaptiveCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatRow("在线脚本", "${data.scripts.online}")
            StatRow("已下架脚本", "${data.scripts.offline}")
            StatRow("未上线脚本", "${data.scripts.notPublished}")
            top.yukonga.miuix.kmp.basic.HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
            )
            StatRow("待审核", "${data.audits.pending}")
            StatRow("审核通过", "${data.audits.approved}")
            StatRow("审核拒绝", "${data.audits.rejected}")
        }
    }
}

@Composable
private fun AiReviewCards(data: PlatformStatistics) {
    AdaptiveCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatRow("AI 评审成功", "${data.aiReviews.success}")
            StatRow("AI 评审失败", "${data.aiReviews.failed}")
            StatRow("累计 Token", formatNumber(data.aiReviews.totalTokensUsed))
            StatRow("风险脚本", "${data.aiReviews.riskDistribution.medium + data.aiReviews.riskDistribution.high}")
        }
    }
}

@Composable
private fun UserInteractionCards(data: PlatformStatistics) {
    AdaptiveCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatRow("活跃评论", "${data.comments.active}")
            StatRow("评论总数", "${data.comments.total}")
            StatRow("上传者", "${data.users.uploaders}")
            StatRow("评论用户", "${data.users.commenters}")
            StatRow("黑名单用户", "${data.users.blacklisted}")
        }
    }
}

@Composable
private fun RecentActivityCards(data: PlatformStatistics) {
    AdaptiveCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActivityColumn("今日新增", data.recentActivity.todayUploads)
            ActivityColumn("本周新增", data.recentActivity.weekUploads)
            ActivityColumn("本月新增", data.recentActivity.monthUploads)
        }
    }
}

@Composable
private fun ActivityColumn(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AdaptiveText(
            text = "$count",
            fontWeight = FontWeight.Bold,
            color = adaptivePrimaryColor()
        )
        Spacer(modifier = Modifier.height(4.dp))
        AdaptiveText(
            text = label,
            color = adaptiveOnSurfaceVariantSummary()
        )
    }
}

@Composable
private fun RiskDistributionCard(data: PlatformStatistics) {
    val risk = data.aiReviews.riskDistribution
    val total = (risk.low + risk.medium + risk.high).toFloat()
    AdaptiveCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProgressBarRow("低风险", risk.low, risk.low / total, 0xFF4CAF50.toInt())
            Spacer(modifier = Modifier.height(12.dp))
            ProgressBarRow("中风险", risk.medium, risk.medium / total, 0xFFFF9800.toInt())
            Spacer(modifier = Modifier.height(12.dp))
            ProgressBarRow("高风险", risk.high, risk.high / total, 0xFFF44336.toInt())
        }
    }
}

@Composable
private fun TagDistributionCard(data: PlatformStatistics) {
    val maxCount = data.tags.values.maxOrNull()?.toFloat() ?: 1f
    AdaptiveCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            data.tags.entries.sortedByDescending { it.value }.forEachIndexed { index, (tag, count) ->
                ProgressBarRow(tag, count, count / maxCount, null)
                if (index < data.tags.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ProgressBarRow(label: String, count: Int, progress: Float, colorArgb: Int?) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AdaptiveText(
                text = label,
                fontWeight = FontWeight.Medium
            )
            AdaptiveText(
                text = "$count",
                color = adaptiveOnSurfaceVariantSummary()
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            progress = progress.coerceIn(0f, 1f)
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AdaptiveText(text = label)
        AdaptiveText(
            text = value,
            fontWeight = FontWeight.Medium,
            color = adaptiveOnSurfaceVariantSummary()
        )
    }
}

private fun formatNumber(n: Int): String {
    return when {
        n >= 1_000_000 -> "${n / 1_000_000}.${(n % 1_000_000) / 100_000}M"
        n >= 1_000 -> "${n / 1_000}.${(n % 1_000) / 100}K"
        else -> "$n"
    }
}
