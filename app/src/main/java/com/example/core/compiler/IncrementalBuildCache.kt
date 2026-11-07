package com.example.core.compiler

import java.io.File
import java.security.MessageDigest

/**
 * ذاكرة التخزين المؤقت للبناء التزايدي (Incremental Build Cache)
 * تحفظ بصمات وتواريخ تعديل الملفات لتخطي المهام التي لم تتغير (UP-TO-DATE)
 */
class IncrementalBuildCache(private val cacheDir: File) {

    private val cacheFile = File(cacheDir, "incremental_state.json")
    private val fileHashes = mutableMapOf<String, String>()

    init {
        loadCache()
    }

    private fun loadCache() {
        if (cacheFile.exists()) {
            try {
                cacheFile.readLines().forEach { line ->
                    val parts = line.split(":::")
                    if (parts.size == 2) {
                        fileHashes[parts[0]] = parts[1]
                    }
                }
            } catch (e: Exception) {
                fileHashes.clear()
            }
        }
    }

    fun saveCache() {
        try {
            if (!cacheDir.exists()) cacheDir.mkdirs()
            cacheFile.bufferedWriter().use { writer ->
                fileHashes.forEach { (path, hash) ->
                    writer.write("$path:::$hash\n")
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * هل تم تعديل هذا الملف أو الملفات التابعة له منذ آخر عملية بناء؟
     */
    fun hasFileChanged(filePath: String, content: String): Boolean {
        val currentHash = calculateHash(content)
        val oldHash = fileHashes[filePath]
        val changed = oldHash == null || oldHash != currentHash
        if (changed) {
            fileHashes[filePath] = currentHash
        }
        return changed
    }

    fun markFileUnchanged(filePath: String, content: String) {
        fileHashes[filePath] = calculateHash(content)
    }

    fun clearCache() {
        fileHashes.clear()
        if (cacheFile.exists()) cacheFile.delete()
    }

    private fun calculateHash(content: String): String {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(content.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
