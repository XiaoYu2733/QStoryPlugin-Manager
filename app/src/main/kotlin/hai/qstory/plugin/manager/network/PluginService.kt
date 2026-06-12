package hai.qstory.plugin.manager.network

import hai.qstory.plugin.manager.data.OnlinePluginInfo
import hai.qstory.plugin.manager.data.PlatformStatistics
import hai.qstory.plugin.manager.data.QSResult
import retrofit2.http.GET
import retrofit2.http.Query

interface PluginService {

    @GET("plugins")
    suspend fun getOnlinePluginList(
        @Query("uin") uin: String = "",
        @Query("sort") sort: String = "time",
        @Query("tag") tag: String = "全部"
    ): QSResult<List<OnlinePluginInfo>>

    @GET("public/statistics")
    suspend fun getPlatformStatistics(): QSResult<PlatformStatistics>
}
