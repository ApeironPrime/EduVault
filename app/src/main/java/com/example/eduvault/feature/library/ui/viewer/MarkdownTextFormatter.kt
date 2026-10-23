package com.example.eduvault.feature.library.ui.viewer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.ui.appendMathText

fun parseMarkdownToAnnotatedString(markdown: String, baseFontSizeSp: Float): AnnotatedString {
    val lines = markdown.split("\n")
    return buildAnnotatedString {
        lines.forEachIndexed { index, line ->
            var processedLine = line
                .replace("\\Omega", "Ω")
                .replace("\\omega", "ω")
                .replace("\\alpha", "α")
                .replace("\\beta", "β")
                .replace("\\gamma", "γ")
                .replace("\\delta", "δ")
                .replace("\\pi", "π")
                .replace("\\sigma", "σ")
                .replace("\\mu", "μ")
                .replace("\\lambda", "λ")
                .replace("\\theta", "θ")
                .replace("\\phi", "φ")
                .replace("\\epsilon", "ε")
                .replace("\\rho", "ρ")
                .replace("\\Delta", "Δ")
                .replace("\\Sigma", "Σ")
            var isHeader = false
            var headerLevel = 0

            if (line.trimStart().startsWith("#")) {
                val match = Regex("^(#{1,6})\\s+(.*)$").find(line.trim())
                if (match != null) {
                    headerLevel = match.groupValues[1].length
                    processedLine = match.groupValues[2]
                    isHeader = true
                }
            } else {
                // Xử lý bullet list (*, -, +) ở đầu dòng thành dấu • để tránh bị trùng với cú pháp in nghiêng *
                val bulletMatch = Regex("^(\\s*)[*+-]\\s+(.*)$").find(line)
                if (bulletMatch != null) {
                    val spaces = bulletMatch.groupValues[1]
                    val content = bulletMatch.groupValues[2]
                    processedLine = "$spaces• $content"
                }
            }

            if (isHeader) {
                val scale = when (headerLevel) {
                    1 -> 1.4f
                    2 -> 1.3f
                    3 -> 1.2f
                    else -> 1.1f
                }
                val start = length
                append(processedLine)
                addStyle(
                    style = SpanStyle(
                        fontSize = (baseFontSizeSp * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC48B36)
                    ),
                    start = start,
                    end = length
                )
            } else {
                appendInlineFormattedText(processedLine, baseFontSizeSp)
            }

            if (index < lines.lastIndex) {
                append("\n")
            }
        }
    }
}

fun AnnotatedString.Builder.appendInlineFormattedText(text: String, baseFontSizeSp: Float) {
    var i = 0
    val len = text.length
    while (i < len) {
        if (text[i] == '$') {
            val endIdx = text.indexOf("$", i + 1)
            if (endIdx != -1) {
                val mathContent = text.substring(i + 1, endIdx)
                appendMathText(mathContent, baseFontSizeSp)
                i = endIdx + 1
                continue
            }
        }

        if (i < len - 2 && text[i] == '*' && text[i + 1] == '*') {
            val endIdx = text.indexOf("**", i + 2)
            if (endIdx != -1) {
                val boldContent = text.substring(i + 2, endIdx)
                val start = length
                append(boldContent)
                addStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    ),
                    start = start,
                    end = length
                )
                i = endIdx + 2
                continue
            }
        }

        if (text[i] == '*') {
            val endIdx = text.indexOf("*", i + 1)
            if (endIdx != -1) {
                val italicContent = text.substring(i + 1, endIdx)
                val start = length
                append(italicContent)
                addStyle(
                    style = SpanStyle(
                        fontStyle = FontStyle.Italic
                    ),
                    start = start,
                    end = length
                )
                i = endIdx + 1
                continue
            }
        }

        if (text[i] == '`') {
            val endIdx = text.indexOf("`", i + 1)
            if (endIdx != -1) {
                val codeContent = text.substring(i + 1, endIdx)
                val start = length
                append(codeContent)
                addStyle(
                    style = SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        background = Color(0x1A808080)
                    ),
                    start = start,
                    end = length
                )
                i = endIdx + 1
                continue
            }
        }

        append(text[i])
        i++
    }
}
