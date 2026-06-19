package hai.qstory.plugin.manager.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hai.qstory.plugin.manager.data.AiReviewRecord
import hai.qstory.plugin.manager.data.PlatformStatistics
import hai.qstory.plugin.manager.data.ScriptDetail
import hai.qstory.plugin.manager.data.ScriptListItem
import java.io.File

internal class PluginCacheStorage(context: Context) {

    private val gson = Gson()
    private val cacheDir = File(context.filesDir, "plugin_cache").also { it.mkdirs() }
    private val detailsDir = File(cacheDir, "details").also { it.mkdirs() }
    private val aiReviewsDir = File(cacheDir, "ai_reviews").also { it.mkdirs() }

    fun loadScriptList(): List<ScriptListItem>? =
        readJson(File(cacheDir, FILE_SCRIPT_LIST), scriptListType)

    fun saveScriptList(data: List<ScriptListItem>) {
        writeJson(File(cacheDir, FILE_SCRIPT_LIST), data)
    }

    fun loadStatistics(): PlatformStatistics? =
        readJson(File(cacheDir, FILE_STATISTICS), statisticsType)

    fun saveStatistics(data: PlatformStatistics) {
        writeJson(File(cacheDir, FILE_STATISTICS), data)
    }

    fun loadScriptDetail(cloudId: String): ScriptDetail? =
        readJson(File(detailsDir, "$cloudId.json"), scriptDetailType)

    fun saveScriptDetail(cloudId: String, data: ScriptDetail) {
        writeJson(File(detailsDir, "$cloudId.json"), data)
    }

    fun loadAiReview(cloudId: String): AiReviewRecord? =
        readJson(File(aiReviewsDir, "$cloudId.json"), aiReviewType)

    fun saveAiReview(cloudId: String, data: AiReviewRecord?) {
        val file = File(aiReviewsDir, "$cloudId.json")
        if (data == null) {
            file.delete()
        } else {
            writeJson(file, data)
        }
    }

    fun loadLastFetch(key: String): Long {
        val file = File(cacheDir, "fetch_times.json")
        if (!file.exists()) return 0L
        return runCatching {
            val map: Map<String, Long> = gson.fromJson(
                file.readText(),
                object : TypeToken<Map<String, Long>>() {}.type,
            )
            map[key] ?: 0L
        }.getOrDefault(0L)
    }

    fun saveLastFetch(key: String, timestamp: Long) {
        val file = File(cacheDir, "fetch_times.json")
        val map = runCatching {
            gson.fromJson<Map<String, Long>>(
                file.takeIf { it.exists() }?.readText(),
                object : TypeToken<Map<String, Long>>() {}.type,
            )
        }.getOrNull()?.toMutableMap() ?: mutableMapOf()
        map[key] = timestamp
        file.writeText(gson.toJson(map))
    }

    fun clearDetailsAndAiReviewsCache() {
        detailsDir.listFiles()?.forEach { it.delete() }
        aiReviewsDir.listFiles()?.forEach { it.delete() }
        cleanFetchTimes()
    }

    private fun cleanFetchTimes() {
        val file = File(cacheDir, "fetch_times.json")
        if (!file.exists()) return
        runCatching {
            val map: Map<String, Long> = gson.fromJson(
                file.readText(),
                object : TypeToken<Map<String, Long>>() {}.type,
            )
            val cleaned = map.filterKeys { key ->
                !key.startsWith("script_detail:") && !key.startsWith("ai_review:")
            }
            file.writeText(gson.toJson(cleaned))
        }
    }

    private inline fun <reified T> readJson(file: File, type: java.lang.reflect.Type): T? {
        if (!file.exists()) return null
        return runCatching { gson.fromJson<T>(file.readText(), type) }.getOrNull()
    }

    private fun writeJson(file: File, data: Any) {
        runCatching { file.writeText(gson.toJson(data)) }
    }

    companion object {
        private const val FILE_SCRIPT_LIST = "script_list.json"
        private const val FILE_STATISTICS = "statistics.json"

        private val scriptListType = object : TypeToken<List<ScriptListItem>>() {}.type
        private val statisticsType = object : TypeToken<PlatformStatistics>() {}.type
        private val scriptDetailType = object : TypeToken<ScriptDetail>() {}.type
        private val aiReviewType = object : TypeToken<AiReviewRecord>() {}.type
    }
}
