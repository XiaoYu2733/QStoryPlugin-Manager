package hai.qstory.plugin.manager.repository

import android.content.Context
import hai.qstory.plugin.manager.data.AiReviewRecord
import hai.qstory.plugin.manager.data.PlatformStatistics
import hai.qstory.plugin.manager.data.ScriptDetail
import hai.qstory.plugin.manager.data.ScriptListItem
import hai.qstory.plugin.manager.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

data class CachedState<T>(
    val data: T? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

object PluginRepository {

    private const val REFRESH_INTERVAL_MS = 30_000L
    private const val CLEANUP_INTERVAL_MS = 12 * 60 * 60 * 1000L

    private val service get() = RetrofitClient.pluginService
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var cacheStorage: PluginCacheStorage? = null
    private var refreshJob: Job? = null
    private var cleanupJob: Job? = null

    private val _scriptList = MutableStateFlow(CachedState<List<ScriptListItem>>(isLoading = true))
    val scriptList: StateFlow<CachedState<List<ScriptListItem>>> = _scriptList.asStateFlow()

    private val _statistics = MutableStateFlow(CachedState<PlatformStatistics>(isLoading = true))
    val statistics: StateFlow<CachedState<PlatformStatistics>> = _statistics.asStateFlow()

    private val detailFlows = ConcurrentHashMap<String, MutableStateFlow<CachedState<ScriptDetail>>>()
    private val aiReviewFlows = ConcurrentHashMap<String, MutableStateFlow<CachedState<AiReviewRecord?>>>()

    private val lastFetchTimes = ConcurrentHashMap<String, Long>()

    private val scriptListActive = java.util.concurrent.atomic.AtomicBoolean(false)
    private val statisticsActive = java.util.concurrent.atomic.AtomicBoolean(false)
    private val activeDetailIds = ConcurrentHashMap.newKeySet<String>()
    private val activeAiReviewIds = ConcurrentHashMap.newKeySet<String>()

    fun init(context: Context) {
        if (refreshJob != null) return
        cacheStorage = PluginCacheStorage(context.applicationContext)
        runBlocking(Dispatchers.IO) {
            loadPersistedCache()
        }
        scriptListActive.set(true)
        statisticsActive.set(true)
        scope.launch {
            fetchScriptList(force = false)
            fetchStatistics(force = false)
        }
        refreshJob = scope.launch {
            while (isActive) {
                delay(REFRESH_INTERVAL_MS.milliseconds)
                refreshAll(force = true)
            }
        }
        cleanupJob = scope.launch {
            while (isActive) {
                delay(CLEANUP_INTERVAL_MS.milliseconds)
                clearDetailsAndAiReviewsCache()
            }
        }
    }

    private fun loadPersistedCache() {
        val storage = cacheStorage ?: return
        storage.loadScriptList()?.let { cached ->
            _scriptList.value = CachedState(data = cached, isLoading = false)
            lastFetchTimes[KEY_SCRIPT_LIST] = storage.loadLastFetch(KEY_SCRIPT_LIST)
        }
        storage.loadStatistics()?.let { cached ->
            _statistics.value = CachedState(data = cached, isLoading = false)
            lastFetchTimes[KEY_STATISTICS] = storage.loadLastFetch(KEY_STATISTICS)
        }
    }

    fun ensureScriptListLoaded() {
        scriptListActive.set(true)
        scope.launch {
            val cached = _scriptList.value.data
            if (cached != null) {
                _scriptList.value = _scriptList.value.copy(isLoading = false, error = null)
            } else if (!_scriptList.value.isLoading) {
                _scriptList.value = _scriptList.value.copy(isLoading = true, error = null)
            }
            fetchScriptList(force = cached == null)
        }
    }

    fun ensureStatisticsLoaded() {
        statisticsActive.set(true)
        scope.launch {
            val cached = _statistics.value.data
            if (cached != null) {
                _statistics.value = _statistics.value.copy(isLoading = false, error = null)
            } else if (!_statistics.value.isLoading) {
                _statistics.value = _statistics.value.copy(isLoading = true, error = null)
            }
            fetchStatistics(force = cached == null)
        }
    }

    fun scriptDetailState(cloudId: String): StateFlow<CachedState<ScriptDetail>> {
        val flow = detailFlows.getOrPut(cloudId) {
            val diskCached = cacheStorage?.loadScriptDetail(cloudId)
            val lastFetch = cacheStorage?.loadLastFetch("$KEY_SCRIPT_DETAIL:$cloudId") ?: 0L
            if (lastFetch > 0L) {
                lastFetchTimes["$KEY_SCRIPT_DETAIL:$cloudId"] = lastFetch
            }
            MutableStateFlow(
                if (diskCached != null) CachedState(data = diskCached, isLoading = false)
                else CachedState(isLoading = true),
            )
        }
        return flow.asStateFlow()
    }

    fun aiReviewState(cloudId: String): StateFlow<CachedState<AiReviewRecord?>> {
        val flow = aiReviewFlows.getOrPut(cloudId) {
            val diskCached = cacheStorage?.loadAiReview(cloudId)
            val lastFetch = cacheStorage?.loadLastFetch("$KEY_AI_REVIEW:$cloudId") ?: 0L
            if (lastFetch > 0L) {
                lastFetchTimes["$KEY_AI_REVIEW:$cloudId"] = lastFetch
            }
            MutableStateFlow(
                if (diskCached != null) CachedState(data = diskCached, isLoading = false)
                else CachedState(isLoading = true),
            )
        }
        return flow.asStateFlow()
    }

    fun ensureScriptDetailLoaded(cloudId: String) {
        activeDetailIds.add(cloudId)
        val flow = detailFlows.getOrPut(cloudId) {
            val diskCached = cacheStorage?.loadScriptDetail(cloudId)
            MutableStateFlow(
                if (diskCached != null) CachedState(data = diskCached, isLoading = false)
                else CachedState(isLoading = true),
            )
        }
        scope.launch {
            if (flow.value.data == null) {
                cacheStorage?.loadScriptDetail(cloudId)?.let { diskCached ->
                    flow.value = CachedState(data = diskCached, isLoading = false)
                }
            }
            val cached = flow.value.data
            if (cached != null) {
                flow.value = flow.value.copy(isLoading = false)
            } else if (!flow.value.isLoading) {
                flow.value = flow.value.copy(isLoading = true)
            }
            fetchScriptDetail(cloudId, force = cached == null)
        }
    }

    fun ensureAiReviewLoaded(cloudId: String) {
        activeAiReviewIds.add(cloudId)
        val flow = aiReviewFlows.getOrPut(cloudId) {
            val diskCached = cacheStorage?.loadAiReview(cloudId)
            MutableStateFlow(
                if (diskCached != null) CachedState(data = diskCached, isLoading = false)
                else CachedState(isLoading = true),
            )
        }
        scope.launch {
            if (flow.value.data == null) {
                cacheStorage?.loadAiReview(cloudId)?.let { diskCached ->
                    flow.value = CachedState(data = diskCached, isLoading = false)
                }
            }
            val cached = flow.value.data
            if (cached != null) {
                flow.value = flow.value.copy(isLoading = false)
            } else if (!flow.value.isLoading) {
                flow.value = flow.value.copy(isLoading = true)
            }
            fetchAiReview(cloudId, force = cached == null)
        }
    }

    private suspend fun refreshAll(force: Boolean) {
        if (scriptListActive.get()) fetchScriptList(force = force)
        if (statisticsActive.get()) fetchStatistics(force = force)
        activeDetailIds.forEach { fetchScriptDetail(it, force = force) }
        activeAiReviewIds.forEach { fetchAiReview(it, force = force) }
    }

    private fun clearDetailsAndAiReviewsCache() {
        cacheStorage?.clearDetailsAndAiReviewsCache()
        detailFlows.clear()
        aiReviewFlows.clear()
        lastFetchTimes.keys.removeAll { key ->
            key.startsWith("$KEY_SCRIPT_DETAIL:") || key.startsWith("$KEY_AI_REVIEW:")
        }
    }

    private fun shouldFetch(key: String, force: Boolean): Boolean {
        if (force) return true
        val lastFetch = lastFetchTimes[key] ?: 0L
        return System.currentTimeMillis() - lastFetch >= REFRESH_INTERVAL_MS
    }

    private fun markFetched(key: String) {
        val now = System.currentTimeMillis()
        lastFetchTimes[key] = now
        cacheStorage?.saveLastFetch(key, now)
    }

    private suspend fun fetchScriptList(force: Boolean = false) {
        if (!shouldFetch(KEY_SCRIPT_LIST, force)) return
        try {
            val newData = fetchAllScriptPages()
            markFetched(KEY_SCRIPT_LIST)
            val current = _scriptList.value.data
            if (newData != current) {
                _scriptList.value = CachedState(data = newData, isLoading = false)
                cacheStorage?.saveScriptList(newData)
            } else if (_scriptList.value.isLoading || _scriptList.value.error != null) {
                _scriptList.value = _scriptList.value.copy(isLoading = false, error = null)
            }
        } catch (e: Exception) {
            if (_scriptList.value.data == null) {
                _scriptList.value = CachedState(error = e.message, isLoading = false)
            } else {
                _scriptList.value = _scriptList.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun fetchStatistics(force: Boolean = false) {
        if (!shouldFetch(KEY_STATISTICS, force)) return
        try {
            val response = service.getPlatformStatistics()
            if (!response.isSuccess()) {
                if (_statistics.value.data == null) {
                    _statistics.value = CachedState(error = response.message, isLoading = false)
                }
                return
            }
            val newData = response.data ?: return
            markFetched(KEY_STATISTICS)
            val current = _statistics.value.data
            if (newData != current) {
                _statistics.value = CachedState(data = newData, isLoading = false)
                cacheStorage?.saveStatistics(newData)
            } else if (_statistics.value.isLoading || _statistics.value.error != null) {
                _statistics.value = _statistics.value.copy(isLoading = false, error = null)
            }
        } catch (e: Exception) {
            if (_statistics.value.data == null) {
                _statistics.value = CachedState(error = e.message, isLoading = false)
            } else {
                _statistics.value = _statistics.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun fetchScriptDetail(cloudId: String, force: Boolean = false) {
        val key = "$KEY_SCRIPT_DETAIL:$cloudId"
        if (!shouldFetch(key, force)) return
        val flow = detailFlows.getOrPut(cloudId) { MutableStateFlow(CachedState(isLoading = true)) }
        try {
            val response = service.getPublicScriptDetail(cloudId)
            if (!response.isSuccess()) {
                if (flow.value.data == null) {
                    flow.value = CachedState(error = response.message, isLoading = false)
                }
                return
            }
            val newData = response.data ?: return
            markFetched(key)
            val current = flow.value.data
            if (newData != current) {
                flow.value = CachedState(data = newData, isLoading = false)
                cacheStorage?.saveScriptDetail(cloudId, newData)
            } else if (flow.value.isLoading || flow.value.error != null) {
                flow.value = flow.value.copy(isLoading = false, error = null)
            }
        } catch (e: Exception) {
            if (flow.value.data == null) {
                flow.value = CachedState(error = e.message, isLoading = false)
            } else {
                flow.value = flow.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun fetchAiReview(cloudId: String, force: Boolean = false) {
        val key = "$KEY_AI_REVIEW:$cloudId"
        if (!shouldFetch(key, force)) return
        val flow = aiReviewFlows.getOrPut(cloudId) { MutableStateFlow(CachedState(isLoading = true)) }
        try {
            val response = service.getPublicScriptAiReview(cloudId)
            if (!response.isSuccess()) {
                if (flow.value.data == null) {
                    flow.value = CachedState(error = response.message, isLoading = false)
                }
                return
            }
            val newData = response.data
            markFetched(key)
            val current = flow.value.data
            if (newData != current) {
                flow.value = CachedState(data = newData, isLoading = false)
                cacheStorage?.saveAiReview(cloudId, newData)
            } else if (flow.value.isLoading || flow.value.error != null) {
                flow.value = flow.value.copy(isLoading = false, error = null)
            }
        } catch (e: Exception) {
            if (flow.value.data == null) {
                flow.value = CachedState(error = e.message, isLoading = false)
            } else {
                flow.value = flow.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun fetchAllScriptPages(): List<ScriptListItem> {
        val firstPage = service.getPublicScriptList()
        if (!firstPage.isSuccess()) {
            throw IllegalStateException(firstPage.message)
        }
        val data = firstPage.data ?: throw IllegalStateException("Empty response")
        val allItems = mutableListOf<ScriptListItem>()
        allItems.addAll(data.list)

        if (data.totalPages > 1) {
            coroutineScope {
                val deferred = (2..data.totalPages).map { page ->
                    async { service.getPublicScriptList(page = page) }
                }
                deferred.awaitAll().forEach { response ->
                    if (response.isSuccess()) {
                        response.data?.list?.let { allItems.addAll(it) }
                    }
                }
            }
        }
        return allItems
    }

    private const val KEY_SCRIPT_LIST = "script_list"
    private const val KEY_STATISTICS = "statistics"
    private const val KEY_SCRIPT_DETAIL = "script_detail"
    private const val KEY_AI_REVIEW = "ai_review"
}
