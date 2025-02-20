package org.orkg.contenttypes.domain.identifiers

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class ISBNTest {
    @ParameterizedTest
    @MethodSource("validISBNs")
    @Suppress("UNUSED_PARAMETER")
    fun `Given a valid isbn, it gets parsed correctly`(isbn: String, uri: String) {
        ISBN.of(isbn).value shouldBe isbn
    }

    @ParameterizedTest
    @MethodSource("validISBNs")
    fun `Given a valid isbn uri, it gets parsed correctly`(isbn: String, uri: String) {
        ISBN.of(uri).value shouldBe isbn
    }

    @ParameterizedTest
    @MethodSource("validISBNs")
    fun `Given a valid isbn, it returns the correct uri`(isbn: String, uri: String) {
        ISBN.of(isbn).uri shouldBe uri
    }

    @ParameterizedTest
    @MethodSource("invalidISBNs")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid isbn, it throws an exception`(isbn: String, uri: String) {
        assertThrows<IllegalArgumentException> { ISBN.of(isbn) }
    }

    @ParameterizedTest
    @MethodSource("invalidISBNs")
    @Suppress("UNUSED_PARAMETER")
    fun `Given an invalid isbn uri, it throws an exception`(isbn: String, uri: String) {
        assertThrows<IllegalArgumentException> { ISBN.of(uri) }
    }

    companion object {
        @JvmStatic
        fun validISBNs(): Stream<Arguments> = Stream.of(
            Arguments.of("99921-58-10-7", "urn:ISBN:99921-58-10-7"),
            Arguments.of("9971-5-0210-0", "urn:ISBN:9971-5-0210-0"),
            Arguments.of("80-902734-1-6", "urn:ISBN:80-902734-1-6"),
            Arguments.of("85-359-0277-5", "urn:ISBN:85-359-0277-5"),
            Arguments.of("1-84356-028-3", "urn:ISBN:1-84356-028-3"),
            Arguments.of("0-684-84328-5", "urn:ISBN:0-684-84328-5"),
            Arguments.of("0-8044-2957-X", "urn:ISBN:0-8044-2957-X"),
            Arguments.of("0-85131-041-9", "urn:ISBN:0-85131-041-9"),
            Arguments.of("93-86954-21-4", "urn:ISBN:93-86954-21-4"),
            Arguments.of("0-943396-04-2", "urn:ISBN:0-943396-04-2"),
            Arguments.of("0-9752298-0-X", "urn:ISBN:0-9752298-0-X"),
            Arguments.of("978-3-598-21504-9", "urn:ISBN:978-3-598-21504-9"),
            Arguments.of("978-303096956-1", "urn:ISBN:978-303096956-1"),
            Arguments.of("978-1-4860-1097-4", "urn:ISBN:978-1-4860-1097-4"),
            Arguments.of("1-4405-9334-5", "urn:ISBN:1-4405-9334-5"),
            Arguments.of("978-0-522-86467-0", "urn:ISBN:978-0-522-86467-0"),
            Arguments.of("3598215088", "urn:ISBN:3598215088"),
            Arguments.of("9780522864670", "urn:ISBN:9780522864670"),
        )

        @JvmStatic
        fun invalidISBNs(): Stream<Arguments> = Stream.of(
            // Illegal start of 13-digit ISBN
            Arguments.of("960-303096956-1", "urn:ISBN:960-303096956-1"),
            // Letters used
            Arguments.of("960-ABC-059-0", "urn:ISBN:960-ABC-059-0"),
            // More than 13 digits
            Arguments.of("960-425-059-0-1", "urn:ISBN:960-425-059-0-1"),
            // Less than 10 digits
            Arguments.of("960-425-059", "urn:ISBN:960-425-059"),
            // 11 digits used
            Arguments.of("960-425-0591-0", "urn:ISBN:960-425-0591-0"),
            // 12 digits used
            Arguments.of("960-4125-0591-0", "urn:ISBN:960-4125-0591-0"),
            // empty
            Arguments.of("", "urn:ISBN:")
        )
    }
}
