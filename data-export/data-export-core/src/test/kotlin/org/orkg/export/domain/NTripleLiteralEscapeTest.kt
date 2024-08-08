package org.orkg.export.domain

import io.kotest.matchers.shouldBe
import java.util.stream.Stream
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class NTripleLiteralEscapeTest {
    @ParameterizedTest
    @MethodSource("literals")
    fun `Given a literal string, it is escaped correctly`(string: String, expected: String) {
        escapeLiteral(string) shouldBe expected
    }

    companion object {
        @JvmStatic
        fun literals(): Stream<Arguments> = Stream.of(
            Arguments.of("abc", "abc"),
            Arguments.of("string with echars \"\n\r\t\u000C\b\\", """string with echars \"\n\r\t\f\b\"""),
            Arguments.of("string with illegal bmp codepoint \uFFFF", """string with illegal bmp codepoint \uFFFF"""),
            Arguments.of("string with illegal non-bmp codepoint \uDC00\uDC00", """string with illegal non-bmp codepoint \uDC00\uDC00"""),
            Arguments.of("string with legal bmp codepoint does not get escaped \uF600", "string with legal bmp codepoint does not get escaped \uF600"),
            Arguments.of("string with legal non-bmp codepoint does not get escaped \uD83D\uDE00", "string with legal non-bmp codepoint does not get escaped \uD83D\uDE00"),
            Arguments.of("string with all cases \n \uFFFF \uDC00\uDC00 \uF600 \uD83D\uDE00", "string with all cases \\n \\uFFFF \\uDC00\\uDC00 \uF600 \uD83D\uDE00"),
        )
    }
}
