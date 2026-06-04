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
        val version: String
    )
}
