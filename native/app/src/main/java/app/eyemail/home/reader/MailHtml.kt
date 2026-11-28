package app.eyemail.home.reader

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

/**
 * Mail arrives as HTML. Printing it raw was wrong, but stripping it to bare text threw
 * away the bold, the links and the paragraphs that make a real mail readable.
 *
 * This walks the HTML once and keeps the parts that matter, in our own type: bold,
 * italic, links, line breaks and list items. Everything else, including style blocks,
 * tables and images, is dropped rather than shown as source.
 */
internal object MailHtml {

    private val blocked = Regex("(?is)<(script|style|head|title)[^>]*>.*?</\\1>")
    private val lineBreaks = Regex("(?i)<br\\s*/?>")
    private val blockEnds = Regex("(?i)</(p|div|tr|h[1-6]|blockquote)>")
    private val listItem = Regex("(?i)<li[^>]*>")
    private val anchor = Regex("(?is)<a\\s+[^>]*href\\s*=\\s*\"([^\"]*)\"[^>]*>(.*?)</a>")
    private val boldOpen = Regex("(?i)<(b|strong)[^>]*>")
    private val boldClose = Regex("(?i)</(b|strong)>")
    private val italicOpen = Regex("(?i)<(i|em)[^>]*>")
    private val italicClose = Regex("(?i)</(i|em)>")
    private val anyTag = Regex("<[^>]+>")
    private val spaces = Regex("[ \t\u00a0]+")
    private val manyNewlines = Regex("\n\\s*\n\\s*\n+")

    fun toText(raw: String, linkColor: Color): AnnotatedString {
        if (raw.isBlank()) return AnnotatedString("")
        if (!raw.contains("<")) return AnnotatedString(decode(raw).trim())

        var html = raw.replace(blocked, " ")

        // Keep links as their visible words, underlined, in the accent colour.
        val links = mutableListOf<Pair<String, String>>()
        html = anchor.replace(html) { match ->
            val url = match.groupValues[1]
            val label = decode(match.groupValues[2].replace(anyTag, "")).trim()
            links.add(label to url)
            "[[link:${links.size - 1}]]"
        }

        html = html.replace(lineBreaks, "\n")
        html = html.replace(blockEnds, "\n")
        html = html.replace(listItem, "\n• ")

        // Remember where the emphasis is before the tags disappear.
        val boldRanges = mutableListOf<IntRange>()
        val italicRanges = mutableListOf<IntRange>()
        val builder = StringBuilder()
        var index = 0
        var boldStart = -1
        var italicStart = -1

        val tokens = Regex("(?i)<(/?)(b|strong|i|em)[^>]*>|<[^>]+>")
        tokens.findAll(html).forEach { match ->
            val plain = html.substring(index, match.range.first)
            builder.append(plain)
            val closing = match.groupValues[1] == "/"
            when (match.groupValues[2].lowercase()) {
                "b", "strong" -> {
                    if (!closing) boldStart = builder.length
                    else if (boldStart >= 0) {
                        boldRanges.add(boldStart until builder.length)
                        boldStart = -1
                    }
                }
                "i", "em" -> {
                    if (!closing) italicStart = builder.length
                    else if (italicStart >= 0) {
                        italicRanges.add(italicStart until builder.length)
                        italicStart = -1
                    }
                }
            }
            index = match.range.last + 1
        }
        builder.append(html.substring(index))

        var text = decode(builder.toString())
        text = spaces.replace(text, " ")
        text = manyNewlines.replace(text, "\n\n")
        text = text.trim()

        // Put the link styling back on the placeholders.
        val annotated = buildAnnotatedString {
            append(text)
            boldRanges.forEach { range ->
                if (range.first < length && range.last < length) {
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), range.first, range.last + 1)
                }
            }
            italicRanges.forEach { range ->
                if (range.first < length && range.last < length) {
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic), range.first, range.last + 1)
                }
            }
            links.forEachIndexed { i, (label, _) ->
                val marker = "[[link:$i]]"
                val at = text.indexOf(marker)
                if (at >= 0 && label.isNotBlank()) {
                    // The marker is replaced by the words the person should read.
                    addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                        at, at + marker.length)
                }
            }
        }

        // Swap each placeholder for its label, keeping the styling above.
        var result = annotated
        links.forEachIndexed { i, (label, _) ->
            val marker = "[[link:$i]]"
            val at = result.text.indexOf(marker)
            if (at >= 0) {
                result = buildAnnotatedString {
                    append(result.subSequence(0, at))
                    pushStringAnnotation("url", links[i].second)
                    withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
                        append(label)
                    }
                    pop()
                    append(result.subSequence(at + marker.length, result.length))
                }
            }
        }
        return result
    }

    /** The few entities that appear in real mail; anything else is left as it is. */
    private fun decode(text: String): String = text
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
        .replace("&mdash;", "—")
        .replace("&ndash;", "–")
}
