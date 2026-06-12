package hai.qstory.plugin.manager.data

import com.google.gson.annotations.SerializedName

data class QSResult<T>(
    val status: Int,
    val data: T?,
    val message: String
) {
    fun isSuccess(): Boolean = status == 200
}

data class OnlinePluginInfo(
    @SerializedName("cloudId")
    val cloudId: String,
    @SerializedName("downloadCount")
    val downloadCount: Int,
    @SerializedName("commentCount")
    val commentCount: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("pluginId")
    val pluginId: String,
    @SerializedName("pluginInfo")
    val pluginInfo: PluginInfo
) {
    data class PluginInfo(
        @SerializedName("author")
        val author: String,
        @SerializedName("cloudId")
        val cloudId: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("fileName")
        val fileName: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("pluginId")
        val pluginId: String,
        @SerializedName("tags")
        val tags: List<String>,
        @SerializedName("version")
        val version: String,
        @SerializedName("images")
        val images: Images? = null
    ) {
        data class Images(
            @SerializedName("cloudId")
            val cloudId: String,
            @SerializedName("iconFilename")
            val iconFilename: String?,
            @SerializedName("iconStatus")
            val iconStatus: Int,
            @SerializedName("previewFilename")
            val previewFilename: List<String>?,
            @SerializedName("previewStatus")
            val previewStatus: Int
        )
    }
}

// ── Statistics API models ──

data class PlatformStatistics(
    val scripts: ScriptStats,
    val audits: AuditStats,
    val downloads: DownloadStats,
    val aiReviews: AiReviewStats,
    val comments: CommentStats,
    val users: UserStats,
    val tags: Map<String, Int>,
    val recentActivity: RecentActivity
)

data class ScriptStats(
    val total: Int,
    val online: Int,
    val offline: Int,
    val notPublished: Int
)

data class AuditStats(
    val total: Int,
    val pending: Int,
    val approved: Int,
    val rejected: Int
)

data class DownloadStats(
    val total: Int
)

data class AiReviewStats(
    val total: Int,
    val success: Int,
    val failed: Int,
    val totalTokensUsed: Int,
    val riskDistribution: RiskDistribution
)

data class RiskDistribution(
    val low: Int,
    val medium: Int,
    val high: Int
)

data class CommentStats(
    val total: Int,
    val active: Int
)

data class UserStats(
    val uploaders: Int,
    val commenters: Int,
    val blacklisted: Int
)

data class RecentActivity(
    val todayUploads: Int,
    val weekUploads: Int,
    val monthUploads: Int
)
