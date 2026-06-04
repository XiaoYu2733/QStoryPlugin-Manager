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
import java.text.Normalizer
import java.util.regex.Pattern

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

    private val INVALID_CHARS = Pattern.compile("[^a-zA-Z0-9\\u4e00-\\u9fa5._-]")

    private fun sanitizeFileName(name: String): String {
        val normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
        val cleaned = INVALID_CHARS.matcher(normalized).replaceAll("")
        return cleaned.trim()
    }

    private fun generateFileName(pluginName: String, cloudId: String): String {
        val sanitizedName = sanitizeFileName(pluginName)
        return "${sanitizedName}_$cloudId.zip"
    }

    suspend fun downloadPlugin(
        pluginName: String,
        cloudId: String,
        serverFileName: String,
        onProgress: (Int) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val downloadUrl = "https://plugin.suzhelan.top/api/plugin/plugins/files/$cloudId"
            val fileName = generateFileName(pluginName, cloudId)

            Log.d("PluginDownloadManager", "Starting download: $downloadUrl")
            Log.d("PluginDownloadManager", "Plugin name: $pluginName")
            Log.d("PluginDownloadManager", "Generated file name: $fileName")
            Log.d("PluginDownloadManager", "Target dir: ${pluginDir.absolutePath}")

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
            Result.success(fileName)
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
