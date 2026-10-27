package com.example.otakulist.data.remote

import com.example.otakulist.data.model.CatalogPatchDto
import com.example.otakulist.data.model.SyncManifestDto
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class LocalSyncClient(
    private val gson: Gson = Gson()
) {

    private fun normalizeBaseUrl(hostOrUrl: String): String {
        val trimmed = hostOrUrl.trim().removeSuffix("/")
        return if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "http://$trimmed"
        } else {
            trimmed
        }
    }

    suspend fun fetchManifest(host: String): Result<SyncManifestDto> = withContext(Dispatchers.IO) {
        runCatching {
            val baseUrl = normalizeBaseUrl(host)
            val url = URL("$baseUrl/status")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 8000
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                throw IllegalStateException("HTTP $responseCode: $errorMsg")
            }

            connection.inputStream.use { stream ->
                InputStreamReader(stream).use { reader ->
                    gson.fromJson(reader, SyncManifestDto::class.java)
                        ?: throw IllegalStateException("Empty manifest received")
                }
            }
        }
    }

    suspend fun fetchPatch(host: String): Result<CatalogPatchDto> = withContext(Dispatchers.IO) {
        runCatching {
            val baseUrl = normalizeBaseUrl(host)
            val url = URL("$baseUrl/patch")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 15000
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                throw IllegalStateException("HTTP $responseCode: $errorMsg")
            }

            connection.inputStream.use { stream ->
                InputStreamReader(stream).use { reader ->
                    gson.fromJson(reader, CatalogPatchDto::class.java)
                        ?: throw IllegalStateException("Empty patch payload received")
                }
            }
        }
    }

    suspend fun downloadCoversZip(
        host: String,
        destinationFile: File,
        expectedSizeBytes: Long = 0L,
        onProgress: (Float) -> Unit = {}
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val baseUrl = normalizeBaseUrl(host)
            val url = URL("$baseUrl/covers")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 30000
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                throw IllegalStateException("HTTP $responseCode: $errorMsg")
            }

            val totalSize = if (expectedSizeBytes > 0L) {
                expectedSizeBytes
            } else {
                connection.contentLengthLong.coerceAtLeast(1L)
            }

            destinationFile.parentFile?.mkdirs()
            val tempFile = File(destinationFile.parentFile, "${destinationFile.name}.tmp")
            if (tempFile.exists()) tempFile.delete()

            try {
                connection.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var downloaded = 0L
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloaded += bytesRead
                            if (totalSize > 0) {
                                val progress = (downloaded.toFloat() / totalSize.toFloat()).coerceIn(0f, 1f)
                                onProgress(progress)
                            }
                        }
                        output.flush()
                    }
                }

                if (destinationFile.exists()) destinationFile.delete()
                if (!tempFile.renameTo(destinationFile)) {
                    throw IllegalStateException("Failed to rename temporary download file to ${destinationFile.name}")
                }
            } finally {
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }
        }
    }

    suspend fun extractCoversZip(
        zipFile: File,
        targetFilesDir: File,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            if (!zipFile.exists() || zipFile.length() == 0L) {
                return@runCatching 0
            }

            targetFilesDir.mkdirs()
            val canonicalTarget = targetFilesDir.canonicalPath
            var extractedCount = 0

            // First pass: count total entries for progress calculation
            var totalEntries = 0
            ZipInputStream(BufferedInputStream(zipFile.inputStream())).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        totalEntries++
                    }
                    entry = zis.nextEntry
                }
            }

            // Second pass: extract files with Zip Slip validation
            ZipInputStream(BufferedInputStream(zipFile.inputStream())).use { zis ->
                var entry = zis.nextEntry
                val buffer = ByteArray(8192)

                while (entry != null) {
                    val entryName = entry.name.replace('\\', '/')
                    val destinationFile = File(targetFilesDir, entryName)
                    val canonicalDest = destinationFile.canonicalPath

                    // Zip Slip Path Traversal Protection
                    if (!canonicalDest.startsWith(canonicalTarget)) {
                        throw SecurityException("Zip Slip detected in entry: $entryName")
                    }

                    if (entry.isDirectory) {
                        destinationFile.mkdirs()
                    } else {
                        destinationFile.parentFile?.mkdirs()
                        FileOutputStream(destinationFile).use { fos ->
                            var len: Int
                            while (zis.read(buffer).also { len = it } != -1) {
                                fos.write(buffer, 0, len)
                            }
                            fos.flush()
                        }
                        extractedCount++
                        onProgress(extractedCount, totalEntries.coerceAtLeast(1))
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            extractedCount
        }
    }
}
