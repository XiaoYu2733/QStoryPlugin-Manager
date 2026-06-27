package hai.qstory.plugin.manager

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import hai.qstory.plugin.manager.preferences.PreferencesManager
import hai.qstory.plugin.manager.ui.component.AdaptiveCard
import hai.qstory.plugin.manager.ui.component.AdaptiveSmallTitle
import hai.qstory.plugin.manager.ui.component.AdaptiveText
import hai.qstory.plugin.manager.ui.component.adaptiveOnSurfaceVariantSummary
import hai.qstory.plugin.manager.ui.theme.LocalUiMode
import hai.qstory.plugin.manager.ui.theme.UiMode
import hai.qstory.plugin.manager.ui.util.TopBarSurface
import hai.qstory.plugin.manager.ui.util.rememberBlurBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.net.URL

private data class Contributor(
    val login: String,
    val name: String,
    val avatarUrl: String,
    val htmlUrl: String
)

private fun fetchGitHubUser(username: String): Contributor {
    return try {
        val json = URL("https://api.github.com/users/$username").readText()
        val obj = JSONObject(json)
        Contributor(
            login = obj.optString("login", username),
            name = obj.optString("name", "").ifEmpty { username },
            avatarUrl = obj.optString("avatar_url", ""),
            htmlUrl = obj.optString("html_url", "https://github.com/$username")
        )
    } catch (_: Exception) {
        Contributor(
            login = username,
            name = username,
            avatarUrl = "",
            htmlUrl = "https://github.com/$username"
        )
    }
}

@Composable
fun AboutPage() {
    val context = LocalContext.current
    val uiMode = LocalUiMode.current
    val githubUrl = "https://github.com/XiaoYu2733/QStoryPlugin-Manager"

    var developers by remember { mutableStateOf<List<Contributor>>(emptyList()) }
    var thanksUsers by remember { mutableStateOf<List<Contributor>>(emptyList()) }
    var genshinExpert by remember { mutableStateOf<Contributor?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            developers = listOf("XiaoYu2733", "suzhelan").mapNotNull { fetchGitHubUser(it) }
            thanksUsers = listOf("HdShare", "HChenX").mapNotNull { fetchGitHubUser(it) }
            genshinExpert = fetchGitHubUser("DJWSJ").copy(name = "原神高手")
        }
    }

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
        // ── 头部 ──
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (uiMode) {
                    UiMode.Material -> {
                        Image(
                            painter = painterResource(R.mipmap.ic_launcher_round),
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    UiMode.Miuix -> {
                        Image(
                            painter = painterResource(R.mipmap.ic_launcher_round),
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AdaptiveText(
                    text = "QStory Plugin Manager",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(4.dp))

                AdaptiveText(
                    text = "v${BuildConfig.VERSION_NAME}",
                    color = adaptiveOnSurfaceVariantSummary(),
                    fontSize = 14.sp,
                )
            }
        }

        // ── 项目简介 ──
        item { AdaptiveSmallTitle(text = "关于项目") }

        item {
            AdaptiveCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AdaptiveText(
                        text = "QStory脚本管理器，用于浏览、下载和QStory脚本。\n\n" +
                                "使用脚本请自行辨别脚本是否包含危险功能，对所加载的脚本负责。\n\n" +
                                "项目100%开源，仅供学习交流使用。",
                        fontSize = 15.sp,
                    )
                }
            }
        }

        // ── 开发者 ──
        if (developers.isNotEmpty()) {
            item { AdaptiveSmallTitle(text = "开发者") }

            item {
                AdaptiveCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp),
                    onClick = {}
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        developers.forEach { dev ->
                            ContributorRow(dev, context)
                        }
                    }
                }
            }
        }

        // ── 特别鸣谢 ──
        if (thanksUsers.isNotEmpty()) {
            item { AdaptiveSmallTitle(text = "特别鸣谢") }

            item {
                AdaptiveCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp),
                    onClick = {}
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        thanksUsers.forEach { user ->
                            ContributorRow(user, context)
                        }
                    }
                }
            }
        }

        // ── 原神高手 ──
        if (genshinExpert != null) {
            item { AdaptiveSmallTitle(text = "原神高手") }

            item {
                AdaptiveCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp),
                    onClick = {}
                ) {
                    ContributorRow(genshinExpert!!, context)
                }
            }
        }

        // ── 反馈交流 ──
        item { AdaptiveSmallTitle(text = "反馈交流") }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdaptiveCard(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                        context.startActivity(intent)
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (uiMode) {
                            UiMode.Material -> {
                                androidx.compose.material3.Icon(
                                    painter = painterResource(R.drawable.ic_github),
                                    contentDescription = "GitHub",
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            UiMode.Miuix -> {
                                MiuixIcon(
                                    painter = painterResource(R.drawable.ic_github),
                                    contentDescription = "GitHub",
                                    tint = MiuixTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            AdaptiveText(text = "GitHub 仓库", fontWeight = FontWeight.Medium)
                            AdaptiveText(
                                text = githubUrl,
                                color = adaptiveOnSurfaceVariantSummary(),
                                fontSize = 13.sp,
                            )
                        }
                    }
                }

                AdaptiveCard(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/XiaoYu_Chat"))
                        context.startActivity(intent)
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (uiMode) {
                            UiMode.Material -> {
                                androidx.compose.material3.Icon(
                                    painter = painterResource(R.drawable.ic_telegram),
                                    contentDescription = "Telegram",
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            UiMode.Miuix -> {
                                MiuixIcon(
                                    painter = painterResource(R.drawable.ic_telegram),
                                    contentDescription = "Telegram",
                                    tint = MiuixTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            AdaptiveText(text = "Telegram 群组", fontWeight = FontWeight.Medium)
                            AdaptiveText(
                                text = "https://t.me/XiaoYu_Chat",
                                color = adaptiveOnSurfaceVariantSummary(),
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
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
                        text = "QStory Plugin Manager",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContributorRow(contributor: Contributor, context: android.content.Context) {
    val uiMode = LocalUiMode.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(contributor.htmlUrl))
                context.startActivity(intent)
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (contributor.avatarUrl.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(contributor.avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
            )
        } else {
            // Fallback avatar placeholder
            when (uiMode) {
                UiMode.Material -> {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.ic_github),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                UiMode.Miuix -> {
                    MiuixIcon(
                        painter = painterResource(R.drawable.ic_github),
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onBackground,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            AdaptiveText(
                text = contributor.login,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
            )
            if (contributor.name.isNotEmpty() && contributor.name != contributor.login) {
                AdaptiveText(
                    text = contributor.name,
                    color = adaptiveOnSurfaceVariantSummary(),
                    fontSize = 13.sp,
                )
            }
        }
    }
}