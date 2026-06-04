package hai.qstory.plugin.manager.manager

import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PluginDownloadManager(private val context: Context) {

    private val okHttpClient = OkHttpClient()

    private val pluginDir by lazy {
        val downloadDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        val qstoryPluginDir = File(downloadDir, "QStoryPlugin")
        if (!qstoryPluginDir.exists()) {
            val created = qstoryPluginDir.mkdirs()
            Log.d("PluginDownloadManager", "Directory created: $created, path: ${qstoryPluginDir.absolutePath}")
        }
        qstoryPluginDir
    }

    suspend fun downloadPlugin(
        pluginName: String,
        downloadUrl: String,
        fileName: String,
        onProgress: (Int) -> Unit = {}
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("PluginDownloadManager", "Starting download: $downloadUrl")
            Log.d("PluginDownloadManager", "Target dir: ${pluginDir.absolutePath}")
            Log.d("PluginDownloadManager", "Target file: $fileName")

            // 确保目录存在
            if (!pluginDir.exists()) {
                pluginDir.mkdirs()
            }

            val file = File(pluginDir, fileName)
            if (file.exists()) {
                file.delete()
            }

            val request = Request.Builder()
                .url(downloadUrl)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("PluginDownloadManager", "Download failed with code: ${response.code}")
                return@withContext Result.failure(IOException("Download failed with code: ${response.code}"))
            }

            val body = response.body
            if (body == null) {
                Log.e("PluginDownloadManager", "Response body is null")
                return@withContext Result.failure(IOException("Response body is null"))
            }

            val contentLength = body.contentLength()
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(file)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                if (contentLength > 0) {
                    val progress = ((totalBytesRead * 100) / contentLength).toInt()
                    onProgress(progress)
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            Log.d("PluginDownloadManager", "Download completed: ${file.absolutePath}, size: ${file.length()}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PluginDownloadManager", "Download error", e)
            Result.failure(e)
        }
    }

    suspend fun getDownloadedFiles(): List<DownloadedFile> = withContext(Dispatchers.IO) {
        if (!pluginDir.exists()) {
            return@withContext emptyList()
        }

        pluginDir.listFiles()?.filter { it.isFile && it.extension == "zip" }?.map { file ->
            DownloadedFile(
                name = file.nameWithoutExtension,
                fileName = file.name,
                size = file.length(),
                path = file.absolutePath
            )
        } ?: emptyList()
    }

    fun deleteDownloadedFile(fileName: String): Boolean {
        val file = File(pluginDir, fileName)
        return file.exists() && file.delete()
    }

    fun getPluginFile(fileName: String): File? {
        val file = File(pluginDir, fileName)
        return if (file.exists()) file else null
    }
}

data class DownloadedFile(
    val name: String,
    val fileName: String,
    val size: Long,
    val path: String
)
