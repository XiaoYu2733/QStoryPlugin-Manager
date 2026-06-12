package hai.qstory.plugin.manager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import hai.qstory.plugin.manager.data.PlatformStatistics
import hai.qstory.plugin.manager.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun StatisticsPage() {
    var stats by remember { mutableStateOf<PlatformStatistics?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.pluginService.getPlatformStatistics()
                if (response.isSuccess()) {
                    stats = response.data
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
            }
            errorMessage != null -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "加载失败: $errorMessage",
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }
            }
            stats != null -> {
                val data = stats!!

                // ── 平台统计总览 ──
                item { SmallTitle(text = "平台统计总览") }
                item {
                    OverviewCards(data)
                }

                // ── 脚本与审核 ──
                item { SmallTitle(text = "脚本与审核") }
                item {
                    ScriptAuditCards(data)
                }

                // ── AI 评审统计 ──
                item { SmallTitle(text = "AI 评审统计") }
                item {
                    AiReviewCards(data)
                }

                // ── 用户与互动 ──
                item { SmallTitle(text = "用户与互动") }
                item {
                    UserInteractionCards(data)
                }

                // ── 近期活跃度 ──
                item { SmallTitle(text = "近期活跃度") }
                item {
                    RecentActivityCards(data)
                }

                // ── AI 风险分布 ──
                item { SmallTitle(text = "AI 风险分布") }
                item {
                    RiskDistributionCard(data)
                }

                // ── 标签分布 ──
                item { SmallTitle(text = "标签分布") }
                item {
                    TagDistributionCard(data)
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }
}

// ── Overview cards (horizontal scroll) ──

@Composable
private fun OverviewCards(data: PlatformStatistics) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { StatItemCard("累计下载", formatNumber(data.downloads.total), MiuixTheme.colorScheme.primary) }
        item { StatItemCard("脚本总数", "${data.scripts.total}", MiuixTheme.colorScheme.primary) }
        item { StatItemCard("审核总数", "${data.audits.total}", MiuixTheme.colorScheme.primary) }
        item { StatItemCard("AI 评审", "${data.aiReviews.total}", MiuixTheme.colorScheme.primary) }
        item { StatItemCard("评论总数", "${data.comments.total}", MiuixTheme.colorScheme.primary) }
    }
}

@Composable
private fun StatItemCard(label: String, value: String, accentColor: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.width(130.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MiuixTheme.textStyles.body1,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Script & Audit section ──

@Composable
private fun ScriptAuditCards(data: PlatformStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
    ) {
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

// ── AI Review section ──

@Composable
private fun AiReviewCards(data: PlatformStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatRow("AI 评审成功", "${data.aiReviews.success}")
            StatRow("AI 评审失败", "${data.aiReviews.failed}")
            StatRow("累计 Token", formatNumber(data.aiReviews.totalTokensUsed))
            StatRow("风险脚本", "${data.aiReviews.riskDistribution.medium + data.aiReviews.riskDistribution.high}")
        }
    }
}

// ── User & Interaction section ──

@Composable
private fun UserInteractionCards(data: PlatformStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatRow("活跃评论", "${data.comments.active}")
            StatRow("评论总数", "${data.comments.total}")
            StatRow("上传者", "${data.users.uploaders}")
            StatRow("评论用户", "${data.users.commenters}")
            StatRow("黑名单用户", "${data.users.blacklisted}")
        }
    }
}

// ── Recent Activity section ──

@Composable
private fun RecentActivityCards(data: PlatformStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
    ) {
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
        Text(
            text = "$count",
            style = MiuixTheme.textStyles.body1,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
        )
    }
}

// ── Risk Distribution with progress bars ──

@Composable
private fun RiskDistributionCard(data: PlatformStatistics) {
    val risk = data.aiReviews.riskDistribution
    val total = (risk.low + risk.medium + risk.high).toFloat()
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProgressBarRow("低风险", risk.low, risk.low / total, 0xFF4CAF50.toInt())
            Spacer(modifier = Modifier.height(12.dp))
            ProgressBarRow("中风险", risk.medium, risk.medium / total, 0xFFFF9800.toInt())
            Spacer(modifier = Modifier.height(12.dp))
            ProgressBarRow("高风险", risk.high, risk.high / total, 0xFFF44336.toInt())
        }
    }
}

// ── Tag Distribution with progress bars ──

@Composable
private fun TagDistributionCard(data: PlatformStatistics) {
    val maxCount = data.tags.values.maxOrNull()?.toFloat() ?: 1f
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
    ) {
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
            Text(
                text = label,
                style = MiuixTheme.textStyles.body2,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            progress = progress.coerceIn(0f, 1f)
        )
    }
}

// ── Helpers ──

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.body2
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.body2,
            fontWeight = FontWeight.Medium,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
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
