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

/**
 * This method escapes a given string following the rules of the
 * [W3C RDF N-Triple specification](https://www.w3.org/TR/rdf12-n-triples/#canonical-ntriples),
 * where the returned string represents a valid STRING_LITERAL_QUOTE.
 */
internal fun escapeLiteral(literal: String): String {
    val echarEscaped = literal
        .replace("\\", """\\""")
        .replace("\t", """\t""")
        .replace("\u0008", """\b""")
        .replace("\n", """\n""")
        .replace("\r", """\r""")
        .replace("\u000C", """\f""")
        .replace("\"", """\"""")
    val ucharEscaped = buildString {
        echarEscaped.codePoints().forEach { codePoint ->
            val isIllegal = illegalCharRanges.any { codePoint in it }
            if (isIllegal) {
                if (Character.isBmpCodePoint(codePoint)) {
                    append("""\u%04X""".format(codePoint))
                } else {
                    append("""\u%04X\u%04X""".format(Character.highSurrogate(codePoint).code, Character.lowSurrogate(codePoint).code))
                }
            } else {
                appendCodePoint(codePoint)
            }
        }
    }
    return ucharEscaped
}
