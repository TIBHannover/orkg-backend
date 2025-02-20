package org.orkg.export.domain

private val illegalCharRanges = arrayOf(
    IntRange(0x00000000, 0x00000007), // range not allowed by N-Triples
    IntRange(0x0000000B, 0x0000000B), // VT, not allowed by N-Triples
    IntRange(0x0000000E, 0x0000001F), // range not allowed by N-Triples
    IntRange(0x0000007F, 0x0000007F), // DEL, not allowed by N-Triples
    IntRange(0x0000D800, 0x0000DFFF), // range not allowed by XML
    IntRange(0x0000FFFF, 0x0000FFFF), // range not allowed by XML
    IntRange(0x00110000, 0x7FFFFFFF), // range not allowed by XML
)

private const val BACKSLASH_CODEPOINT = '\\'.code
private const val HORIZONTAL_TAB_CODEPOINT = '\t'.code
private const val BACKSPACE_CODEPOINT = '\u0008'.code
private const val NEW_LINE_CODEPOINT = '\n'.code
private const val CARRIAGE_RETURN_CODEPOINT = '\r'.code
private const val FORM_FEED_CODEPOINT = '\u000C'.code
private const val DOUBLE_QUOTES_CODEPOINT = '\"'.code

/**
 * This method escapes a given string following the rules of the
 * [W3C RDF N-Triple specification](https://www.w3.org/TR/rdf12-n-triples/#canonical-ntriples),
 * where the returned string represents a valid STRING_LITERAL_QUOTE.
 */
internal fun escapeLiteral(literal: String): String = buildString(literal.length) {
    literal.codePoints().forEach { codePoint ->
        when (codePoint) {
            BACKSLASH_CODEPOINT -> append("""\\""")
            HORIZONTAL_TAB_CODEPOINT -> append("""\t""")
            BACKSPACE_CODEPOINT -> append("""\b""")
            NEW_LINE_CODEPOINT -> append("""\n""")
            CARRIAGE_RETURN_CODEPOINT -> append("""\r""")
            FORM_FEED_CODEPOINT -> append("""\f""")
            DOUBLE_QUOTES_CODEPOINT -> append("""\"""")
            else -> {
                val isIllegal = illegalCharRanges.any { codePoint in it }
                if (isIllegal) {
                    if (Character.isBmpCodePoint(codePoint)) {
                        append("""\u%04X""".format(codePoint))
                    } else {
                        val high = Character.highSurrogate(codePoint).code
                        val low = Character.lowSurrogate(codePoint).code
                        append("""\u%04X\u%04X""".format(high, low))
                    }
                } else {
                    appendCodePoint(codePoint)
                }
            }
        }
    }
}
