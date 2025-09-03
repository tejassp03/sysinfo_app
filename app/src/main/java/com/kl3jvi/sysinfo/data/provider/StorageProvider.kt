package com.kl3jvi.sysinfo.data.provider

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.util.Log
import org.koin.core.component.KoinComponent
import java.io.File

class StorageProvider(
    private val context: Context
) : KoinComponent {

    private fun getSystemStorageUsage(): Pair<Long, Long> {
        val root = "/"
        val stat = StatFs(root)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        val totalStorage = blockSize * totalBlocks
        val availableStorage = blockSize * availableBlocks
        return Pair(totalStorage, totalStorage - availableStorage)
    }

    private fun getInternalStorageUsage(): Pair<Long, Long> {
        val internalStorage = context.filesDir.absolutePath
        val internalStat = StatFs(internalStorage)
        val internalBlockSize = internalStat.blockSizeLong
        val internalTotalBlocks = internalStat.blockCountLong
        val internalAvailableBlocks = internalStat.availableBlocksLong
        val totalInternalStorage = internalBlockSize * internalTotalBlocks
        val availableInternalStorage = internalBlockSize * internalAvailableBlocks
        return Pair(totalInternalStorage, totalInternalStorage - availableInternalStorage)
    }

    private fun getExternalStorageUsage(): Pair<Long, Long> {
        val externalStorage = Environment.getExternalStorageDirectory().absolutePath
        val externalStat = StatFs(externalStorage)
        val externalBlockSize = externalStat.blockSizeLong
        val externalTotalBlocks = externalStat.blockCountLong
        val externalAvailableBlocks = externalStat.availableBlocksLong
        val totalExternalStorage = externalBlockSize * externalTotalBlocks
        val availableExternalStorage = externalBlockSize * externalAvailableBlocks
        return Pair(totalExternalStorage, totalExternalStorage - availableExternalStorage)
    }

    fun getFileTypesAndSizes(): List<Pair<String, Long>> {
        val result = mutableMapOf<String, Long>()

        // Getting the internal storage path
        val internalStoragePath = Environment.getExternalStorageDirectory().absolutePath

        val internalStorage = File(internalStoragePath)

        // Recursive function to process files and directories
        fun processFile(file: File) {
            if (file.isDirectory) {
                file.listFiles()?.forEach { child ->
                    processFile(child)
                }
            } else if (file.isFile) {
                val extension = file.extension
                val fileSize = file.length()
                val currentSize = result[extension] ?: 0L // Custom logic to replace getOrDefault
                result[extension] = currentSize + fileSize
            }
        }

        processFile(internalStorage)

        return result.map {
            Log.e("_____${it.key}", it.value.toString())
            Pair(it.key, it.value)
        }
    }

    fun calculateSystemPercentage(): Int {
        val (total, used) = getSystemStorageUsage()
        return (used.toDouble() / total * 100).toInt()
    }

    fun calculateInternalPercentage(): Int {
        val (total, used) = getInternalStorageUsage()
        return (used.toDouble() / total * 100).toInt()
    }

    fun calculateExternalPercentage(): Int {
        val (total, used) = getExternalStorageUsage()
        return (used.toDouble() / total * 100).toInt()
    }

    fun getInternalStorageInfo(): Triple<Long, Long, Long> {
        val (total, used) = getInternalStorageUsage()
        return Triple(total, used, total - used)
    }

    fun getExternalStorageInfo(): Triple<Long, Long, Long> {
        val (total, used) = getExternalStorageUsage()
        return Triple(total, used, total - used)
    }

    private fun formatBytes(bytes: Long): String {
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return String.format("%.1f GB", gb)
    }

    fun getFormattedInternalStorage(): Triple<String, String, String> {
        val (total, used, available) = getInternalStorageInfo()
        return Triple(formatBytes(total), formatBytes(used), formatBytes(available))
    }

    fun getFormattedExternalStorage(): Triple<String, String, String> {
        val (total, used, available) = getExternalStorageInfo()
        return Triple(formatBytes(total), formatBytes(used), formatBytes(available))
    }
}
