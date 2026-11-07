package com.example.core.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.*

/**
 * محرك تلوين الصيغ البرمجية (Syntax Highlighter)
 * يدعم لغات: Kotlin, Java, XML, HTML, CSS, JavaScript, JSON
 */
object SyntaxHighlighter {

    private val kotlinKeywords = setOf(
        "package", "import", "class", "interface", "object", "fun", "val", "var",
        "private", "public", "protected", "internal", "override", "open", "abstract",
        "return", "if", "else", "when", "for", "while", "do", "try", "catch", "finally",
        "throw", "is", "in", "as", "by", "lazy", "companion", "data", "sealed", "enum",
        "suspend", "inline", "true", "false", "null", "this", "super", "typealias"
    )

    private val javaKeywords = setOf(
        "package", "import", "public", "private", "protected", "class", "interface",
        "extends", "implements", "static", "final", "void", "int", "boolean", "double",
        "float", "long", "byte", "short", "char", "return", "if", "else", "switch",
        "case", "default", "for", "while", "do", "break", "continue", "try", "catch",
        "finally", "throw", "throws", "new", "this", "super", "null", "true", "false"
    )

    private val jsKeywords = setOf(
        "const", "let", "var", "function", "return", "if", "else", "for", "while",
        "switch", "case", "break", "continue", "try", "catch", "finally", "throw",
        "new", "this", "class", "extends", "import", "export", "default", "from",
        "async", "await", "true", "false", "null", "undefined", "typeof", "document", "window"
    )

    fun highlight(code: String, language: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            when (language.lowercase()) {
                "kotlin" -> applyKeywordsAndTokens(this, code, kotlinKeywords)
                "java" -> applyKeywordsAndTokens(this, code, javaKeywords)
                "javascript", "js" -> applyKeywordsAndTokens(this, code, jsKeywords)
                "xml", "html" -> applyXmlHighlighting(this, code)
                "json" -> applyJsonHighlighting(this, code)
                "css" -> applyCssHighlighting(this, code)
                else -> applyGenericHighlighting(this, code)
            }
        }
    }

    private fun applyKeywordsAndTokens(builder: AnnotatedString.Builder, code: String, keywords: Set<String>) {
        // تلوين السلاسل النصية "..."
        val stringRegex = Regex("\"(\\\\.|[^\"])*\"|'(\\\\.|[^'])*'")
        stringRegex.findAll(code).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFFA5D6FF)),
                match.range.first,
                match.range.last + 1
            )
        }

        // تلوين التعليقات السطرية //... والمكتملة /*...*/
        val commentRegex = Regex("//.*|/\\*[\\s\\S]*?\\*/")
        commentRegex.findAll(code).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFF8B949E), fontWeight = FontWeight.Normal),
                match.range.first,
                match.range.last + 1
            )
        }

        // تلوين التعليقات التوضيحية @Annotation
        val annotationRegex = Regex("@[A-Za-z0-9_]+")
        annotationRegex.findAll(code).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFFFFB300), fontWeight = FontWeight.SemiBold),
                match.range.first,
                match.range.last + 1
            )
        }

        // تلوين الكلمات المفتاحية
        val wordRegex = Regex("\\b[A-Za-z_][A-Za-z0-9_]*\\b")
        wordRegex.findAll(code).forEach { match ->
            val word = match.value
            if (keywords.contains(word)) {
                builder.addStyle(
                    SpanStyle(color = Color(0xFFFF7B72), fontWeight = FontWeight.Bold),
                    match.range.first,
                    match.range.last + 1
                )
            } else if (word.first().isUpperCase()) {
                // أسماء الكلاسات والأنواع (مثل String, Modifier, View)
                builder.addStyle(
                    SpanStyle(color = Color(0xFF7EE787)),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        // تلوين الأرقام
        val numberRegex = Regex("\\b\\d+(\\.\\d+)?(f|L)?\\b")
        numberRegex.findAll(code).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFF79C0FF)),
                match.range.first,
                match.range.last + 1
            )
        }
    }

    private fun applyXmlHighlighting(builder: AnnotatedString.Builder, code: String) {
        // تلوين وسوم XML/HTML: <tag ...> </tag>
        val tagRegex = Regex("<(/?)(\\w+)(:[\\w]+)?")
        tagRegex.findAll(code).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFF7EE787), fontWeight = FontWeight.Bold),
                match.range.first,
                match.range.last + 1
            )
        }

        // تلوين السمات (Attributes مثل android:id أو class=)
        val attrRegex = Regex("\\b(\\w+:\\w+|\\w+)(?=\\s*=)")
        attrRegex.findAll(code).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFF79C0FF)),
                match.range.first,
                match.range.last + 1
            )
        }

        // تلوين القيم داخل علامات التنصيص
        val valueRegex = Regex("\"[^\"]*\"")
        valueRegex.findAll(code).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFFA5D6FF)),
                match.range.first,
                match.range.last + 1
            )
        }

        // تلوين التعليقات <!-- ... -->
        val commentRegex = Regex("<!--[\\s\\S]*?-->")
        commentRegex.findAll(code).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFF8B949E)),
                match.range.first,
                match.range.last + 1
            )
        }
    }

    private fun applyJsonHighlighting(builder: AnnotatedString.Builder, code: String) {
        // مفاتيح JSON
        val keyRegex = Regex("\"[^\"]+\"\\s*:")
        keyRegex.findAll(code).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFF7EE787), fontWeight = FontWeight.Bold),
                match.range.first,
                match.range.last + 1
            )
        }
        // قيم السلاسل النصية
        val stringRegex = Regex(":\\s*\"[^\"]*\"")
        stringRegex.findAll(code).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFFA5D6FF)),
                match.range.first,
                match.range.last + 1
            )
        }
        // الأرقام والقيم البوليانية
        val literalRegex = Regex("\\b(true|false|null|\\d+(\\.\\d+)?)\\b")
        literalRegex.findAll(code).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFFFF7B72)),
                match.range.first,
                match.range.last + 1
            )
        }
    }

    private fun applyCssHighlighting(builder: AnnotatedString.Builder, code: String) {
        // محددات CSS
        val selectorRegex = Regex("[.#]?[a-zA-Z0-9_-]+\\s*(?=\\{)")
        selectorRegex.findAll(code).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFFFF7B72), fontWeight = FontWeight.Bold),
                match.range.first,
                match.range.last + 1
            )
        }
        // خصائص CSS
        val propRegex = Regex("[a-zA-Z-]+(?=\\s*:)")
        propRegex.findAll(code).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFF79C0FF)),
                match.range.first,
                match.range.last + 1
            )
        }
        // قيم الألوان والأرقام
        val valRegex = Regex("(#([0-9a-fA-F]{3,8})|\\d+(px|rem|%|em|vh|vw)?)")
        valRegex.findAll(code).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFFA5D6FF)),
                match.range.first,
                match.range.last + 1
            )
        }
    }

    private fun applyGenericHighlighting(builder: AnnotatedString.Builder, code: String) {
        val stringRegex = Regex("\"(\\\\.|[^\"])*\"|'(\\\\.|[^'])*'")
        stringRegex.findAll(code).forEach { match ->
            builder.addStyle(SpanStyle(color = Color(0xFFA5D6FF)), match.range.first, match.range.last + 1)
        }
    }
}
