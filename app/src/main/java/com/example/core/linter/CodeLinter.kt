package com.example.core.linter

enum class DiagnosticSeverity {
    ERROR,
    WARNING,
    INFO
}

data class CodeDiagnostic(
    val line: Int,
    val message: String,
    val severity: DiagnosticSeverity,
    val ruleId: String
)

/**
 * محلل الأكواد الفوري (Real-Time Code Inspection & Diagnostics)
 * يحاكي أداة Lint وفحص الأخطاء النحوية في Android Studio
 */
object CodeLinter {

    fun analyze(code: String, language: String): List<CodeDiagnostic> {
        val diagnostics = mutableListOf<CodeDiagnostic>()
        val lines = code.lines()

        // 1. فحص توازن الأقواس { } و ( ) و [ ]
        var braceCount = 0
        var parenCount = 0
        var bracketCount = 0

        lines.forEachIndexed { index, line ->
            val lineNumber = index + 1
            val trimmed = line.trim()

            // تخطي التعليقات
            if (trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                return@forEachIndexed
            }

            // فحص إغلاق السلاسل النصية الفردية
            val quoteCount = trimmed.count { it == '"' }
            if (quoteCount % 2 != 0 && !trimmed.contains("\"\"\"")) {
                diagnostics.add(
                    CodeDiagnostic(
                        line = lineNumber,
                        message = "سلسلة نصية غير مغلقة (Unterminated String Literal)",
                        severity = DiagnosticSeverity.ERROR,
                        ruleId = "SyntaxError:StringLiteral"
                    )
                )
            }

            // فحص الفاصلة المنقوطة في Java
            if (language.equals("java", ignoreCase = true)) {
                if (trimmed.isNotEmpty() &&
                    !trimmed.endsWith(";") &&
                    !trimmed.endsWith("{") &&
                    !trimmed.endsWith("}") &&
                    !trimmed.startsWith("@") &&
                    !trimmed.startsWith("package") &&
                    !trimmed.startsWith("import")
                ) {
                    if (trimmed.startsWith("return") || trimmed.startsWith("int") || trimmed.startsWith("String") || trimmed.contains("=")) {
                        diagnostics.add(
                            CodeDiagnostic(
                                line = lineNumber,
                                message = "فاصلة منقوطة مفقودة ';' في نهاية السطر",
                                severity = DiagnosticSeverity.WARNING,
                                ruleId = "Java:MissingSemicolon"
                            )
                        )
                    }
                }
            }

            // فحص وسوم XML غير المغلقة
            if (language.equals("xml", ignoreCase = true) || language.equals("html", ignoreCase = true)) {
                if (trimmed.startsWith("<") && !trimmed.startsWith("<!--") && !trimmed.startsWith("<?")) {
                    if (!trimmed.endsWith(">") && !trimmed.endsWith("/>")) {
                        diagnostics.add(
                            CodeDiagnostic(
                                line = lineNumber,
                                message = "وسم XML غير مغلق بإحكام '>'",
                                severity = DiagnosticSeverity.ERROR,
                                ruleId = "Xml:UnclosedTag"
                            )
                        )
                    }
                }
            }

            // عد الأقواس
            line.forEach { ch ->
                when (ch) {
                    '{' -> braceCount++
                    '}' -> braceCount--
                    '(' -> parenCount++
                    ')' -> parenCount--
                    '[' -> bracketCount++
                    ']' -> bracketCount--
                }
            }
        }

        if (braceCount != 0) {
            diagnostics.add(
                CodeDiagnostic(
                    line = lines.size,
                    message = if (braceCount > 0) "قوس معقوف مفتوح غير مغلق '}'" else "قوس معقوف زائد '}'",
                    severity = DiagnosticSeverity.ERROR,
                    ruleId = "SyntaxError:UnbalancedBraces"
                )
            )
        }

        if (parenCount != 0) {
            diagnostics.add(
                CodeDiagnostic(
                    line = lines.size,
                    message = if (parenCount > 0) "قوس هلالي مفتوح غير مغلق ')'" else "قوس هلالي زائد ')'",
                    severity = DiagnosticSeverity.ERROR,
                    ruleId = "SyntaxError:UnbalancedParens"
                )
            )
        }

        return diagnostics
    }
}
