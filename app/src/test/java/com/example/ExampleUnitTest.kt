package com.example

import com.example.core.compiler.IncrementalBuildCache
import com.example.core.editor.SyntaxHighlighter
import com.example.core.linter.CodeLinter
import com.example.core.linter.DiagnosticSeverity
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * اختبارات الوحدة لمحرك فحص الأكواد وتلوين الصيغ والبناء التزايدي في MobileForge IDE
 */
class ExampleUnitTest {
    @Test
    fun testCodeLinterDetectsUnclosedBraces() {
        val brokenCode = """
            fun main() {
                println("Hello world")
        """.trimIndent()

        val diagnostics = CodeLinter.analyze(brokenCode, "kotlin")
        assertTrue(diagnostics.any { it.severity == DiagnosticSeverity.ERROR })
    }

    @Test
    fun testCodeLinterCleanCode() {
        val cleanCode = """
            fun main() {
                println("Hello world")
            }
        """.trimIndent()

        val diagnostics = CodeLinter.analyze(cleanCode, "kotlin")
        assertTrue(diagnostics.isEmpty())
    }

    @Test
    fun testSyntaxHighlighterHighlightNotEmpty() {
        val code = "val x = 10"
        val highlighted = SyntaxHighlighter.highlight(code, "kotlin")
        assertEquals(code, highlighted.text)
    }

    @Test
    fun testIncrementalBuildCacheDetectsChanges() {
        val tempDir = File.createTempFile("test_cache", "").also { it.delete(); it.mkdirs() }
        val cache = IncrementalBuildCache(tempDir)

        val filePath = "app/src/main/java/MainActivity.kt"
        val initialContent = "class MainActivity"

        // المرة الأولى: تغير مؤكد
        assertTrue(cache.hasFileChanged(filePath, initialContent))

        // المرة الثانية بنفس المحتوى: لم يتغير
        assertFalse(cache.hasFileChanged(filePath, initialContent))

        // تعديل المحتوى: كشف التغيير
        assertTrue(cache.hasFileChanged(filePath, "class MainActivity : Activity"))

        tempDir.deleteRecursively()
    }
}
