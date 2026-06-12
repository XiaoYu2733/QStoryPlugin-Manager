package hai.qstory.plugin.manager.network

import hai.qstory.plugin.manager.data.OnlinePluginInfo
import hai.qstory.plugin.manager.data.PaginatedScripts
import hai.qstory.plugin.manager.data.PlatformStatistics
import hai.qstory.plugin.manager.data.QSResult
import hai.qstory.plugin.manager.data.ScriptDetail
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PluginService {

    @GET("plugins")
    suspend fun getOnlinePluginList(
        @Query("uin") uin: String = "",
        @Query("sort") sort: String = "time",
        @Query("tag") tag: String = "全部"
    ): QSResult<List<OnlinePluginInfo>>

    @GET("public/scripts")
    suspend fun getPublicScriptList(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 100,
        @Query("sort") sort: String = "time",
        @Query("status") status: Int = -1
    ): QSResult<PaginatedScripts>

    @GET("public/scripts/{cloudId}")
    suspend fun getPublicScriptDetail(
        @Path("cloudId") cloudId: String
    ): QSResult<ScriptDetail>

    @GET("public/statistics")
    suspend fun getPlatformStatistics(): QSResult<PlatformStatistics>
}
