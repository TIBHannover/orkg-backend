package org.orkg.dataimport.domain.internal

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.orkg.dataimport.domain.BlankCSVHeaderValue
import org.orkg.dataimport.domain.DuplicateCSVHeaders
import org.orkg.dataimport.domain.EmptyCSVHeader
import org.orkg.dataimport.domain.InconsistentCSVColumnCount
import org.orkg.dataimport.domain.InvalidCSVValue
import org.orkg.dataimport.domain.TypedValue
import org.orkg.dataimport.domain.UnexpectedCSVValueType
import org.orkg.dataimport.domain.UnknownCSVNamespace
import org.orkg.dataimport.domain.UnknownCSVNamespaceValue
import org.orkg.dataimport.domain.UnknownCSVValueType
import org.orkg.dataimport.domain.csv.CSVHeader
import org.orkg.dataimport.domain.testing.fixtures.createCSVHeaders
import org.orkg.dataimport.domain.testing.fixtures.createCSVSchema
import org.orkg.graph.domain.Classes

internal class SchemaBasedCSVRecordParserTest {
    private val schemaBasedCSVRecordParser = SchemaBasedCSVRecordParser(createCSVSchema())
    private val headers = createCSVHeaders()

    @Test
    fun `Given a list of header values, it parses the header correctly`() {
        val values = listOf("abc", "closed-namespace:the only possible value", "open-namespace:a", "open-namespace:b")
        val expected = headers

        schemaBasedCSVRecordParser.parseHeader(values) shouldBe expected
    }

    @Test
    fun `Given a list of header values, when it contains duplicate header values of no namespace, it returns success`() {
        val values = listOf("a", "a")
        val expected = listOf(
            CSVHeader(
                column = 1,
                name = "a",
                namespace = null,
                columnType = null,
            ),
            CSVHeader(
                column = 2,
                name = "a",
                namespace = null,
                columnType = null,
            )
        )

        schemaBasedCSVRecordParser.parseHeader(values) shouldBe expected
    }

    @Test
    fun `Given a list of header values, when it contains duplicate header values of an open namespace, it returns success`() {
        val values = listOf("open-namespace:a", "open-namespace:a")
        val expected = listOf(
            CSVHeader(
                column = 1,
                name = "a",
                namespace = "open-namespace",
                columnType = Classes.string,
            ),
            CSVHeader(
                column = 2,
                name = "a",
                namespace = "open-namespace",
                columnType = Classes.string,
            )
        )

        schemaBasedCSVRecordParser.parseHeader(values) shouldBe expected
    }

    @Test
    fun `Given a list of header values, when it contains duplicate header values of a closed namespace, it throws an exception`() {
        val values = listOf("closed-namespace:the only possible value", "closed-namespace:the only possible value")

        shouldThrow<DuplicateCSVHeaders> {
            schemaBasedCSVRecordParser.parseHeader(values)
        }
    }

    @Test
    fun `Given a list of header values, when empty, it throws an exception`() {
        val values = emptyList<String>()

        shouldThrow<EmptyCSVHeader> {
            schemaBasedCSVRecordParser.parseHeader(values)
        }
    }

    @Test
    fun `Given a list of header values, when it contains an empty value, it throws an exception`() {
        val values = listOf("")

        shouldThrowNestedException<BlankCSVHeaderValue> {
            schemaBasedCSVRecordParser.parseHeader(values)
        }
    }

    @Test
    fun `Given a list of header values, when it contains an unknown namespace, it throws an exception`() {
        val values = listOf("missing:namespace")

        shouldThrowNestedException<UnknownCSVNamespace> {
            schemaBasedCSVRecordParser.parseHeader(values)
        }
    }

    @Test
    fun `Given a list of header values, when it contains an unknown value of a closed namespace, it throws an exception`() {
        val values = listOf("closed-namespace:an unknown value?")

        shouldThrowNestedException<UnknownCSVNamespaceValue> {
            schemaBasedCSVRecordParser.parseHeader(values)
        }
    }

    @Test
    fun `Given a list of header values, when it contains a value of a closed namespace and its type definition does not match the namespace type definition, it throws an exception`() {
        val values = listOf("closed-namespace:the only possible value<text>")

        shouldThrowNestedException<UnexpectedCSVValueType> {
            schemaBasedCSVRecordParser.parseHeader(values)
        }
    }

    @Test
    fun `Given a list of header values, when it contains a malformed value, it throws an exception`() {
        val values = listOf("open-namespace:-")

        shouldThrowNestedException<InvalidCSVValue> {
            schemaBasedCSVRecordParser.parseHeader(values)
        }
    }

    @Test
    fun `Given a list of header values, when it contains a value with an unknown type, it throws an exception`() {
        val values = listOf("abc<string>")

        shouldThrowNestedException<UnknownCSVValueType> {
            schemaBasedCSVRecordParser.parseHeader(values)
        }
    }

    @Test
    fun `Given a list of record values, it parses the record correctly`() {
        val values = listOf("156541<decimal>", "true", "some string value", "closed-value-namespace:option2")
        val expected = listOf(
            TypedValue(
                namespace = null,
                value = "156541",
                type = Classes.decimal,
            ),
            TypedValue(
                namespace = null,
                value = "true",
                type = Classes.boolean,
            ),
            TypedValue(
                namespace = null,
                value = "some string value",
                type = Classes.string,
            ),
            TypedValue(
                namespace = "closed-value-namespace",
                value = "option2",
                type = Classes.string,
            )
        )

        schemaBasedCSVRecordParser.parseRecord(values, 1, headers) shouldBe expected
    }

    @Test
    fun `Given a list of record values, when value count is less than header column count, it throws an exception`() {
        val values = listOf("1")

        shouldThrow<InconsistentCSVColumnCount> {
            schemaBasedCSVRecordParser.parseRecord(values, 1, headers)
        }
    }

    @Test
    fun `Given a list of record values, when value count is greater than header column count, it throws an exception`() {
        val values = listOf("1", "2", "3", "4", "5")

        shouldThrow<InconsistentCSVColumnCount> {
            schemaBasedCSVRecordParser.parseRecord(values, 1, headers)
        }
    }

    @Test
    fun `Given a list of record values, when it contains an unknown value of a closed namespace, it throws an exception`() {
        val values = listOf("156541<decimal>", "true", "some string value", "closed-value-namespace:does not exist")

        shouldThrowNestedException<UnknownCSVNamespaceValue> {
            schemaBasedCSVRecordParser.parseRecord(values, 1, headers)
        }.asClue {
            it.message shouldBe """Unknown value "does not exist" for closed namespace "closed-value-namespace" in row 1, column 4."""
        }
    }

    @Test
    fun `Given a list of record values, when it contains a value of a closed namespace and its type definition does not match the namespace type definition, it throws an exception`() {
        val values = listOf("156541<decimal>", "true", "some string value", "closed-value-namespace:option2<boolean>")

        shouldThrowNestedException<UnexpectedCSVValueType> {
            schemaBasedCSVRecordParser.parseRecord(values, 1, headers)
        }.asClue {
            it.message shouldBe """Invalid type "Boolean" for value in row 1, column 4. Expected type "String"."""
        }
    }

    @Test
    fun `Given a list of record values, when it contains a value that declares a different type than declared in the value namespace, it throws an exception`() {
        val values = listOf("156541<decimal>", "true", "open-value-namespace:not a boolean<text>", "closed-value-namespace:option2")

        shouldThrowNestedException<UnexpectedCSVValueType> {
            schemaBasedCSVRecordParser.parseRecord(values, 1, headers)
        }.asClue {
            it.message shouldBe """Invalid type "String" for value in row 1, column 3. Expected type "Boolean"."""
        }
    }

    @Test
    fun `Given a list of record values, when it contains a malformed value that does not follow the value namespace constraint, it throws an exception`() {
        val values = listOf("156541<decimal>", "open-value-namespace:1", "some string value", "closed-value-namespace:option2")

        shouldThrowNestedException<InvalidCSVValue> {
            schemaBasedCSVRecordParser.parseRecord(values, 1, headers)
        }.asClue {
            it.message shouldBe """Invalid value "1" in row 1, column 2. Reason: Value "1" does not match pattern "[A-Za-z ]+"."""
        }
    }

    @Test
    fun `Given a list of record values, when it contains a value that does not match the column value constraint of the header namespace, it throws an exception`() {
        val values = listOf("156541<decimal>", "true", "-", "closed-value-namespace:option2")

        shouldThrowNestedException<InvalidCSVValue> {
            schemaBasedCSVRecordParser.parseRecord(values, 1, headers)
        }.asClue {
            it.message shouldBe """Invalid value "-" in row 1, column 3. Reason: Value "-" does not match pattern "[\w ]+"."""
        }
    }

    @Test
    fun `Given a list of record values, when it contains a value that declares a different type than declared in the closed header namespace, it throws an exception`() {
        val values = listOf("156541<decimal>", "true<text>", "some string value", "closed-value-namespace:option2")

        shouldThrowNestedException<UnexpectedCSVValueType> {
            schemaBasedCSVRecordParser.parseRecord(values, 1, headers)
        }.asClue {
            it.message shouldBe """Invalid type "String" for value in row 1, column 2. Expected type "Boolean"."""
        }
    }

    @Test
    fun `Given a list of record values, when it contains a value that declares a different type than declared in the open header namespace, it does not throw an exception`() {
        val values = listOf("156541<decimal>", "true", "not a string<text>", "closed-value-namespace:option2")

        shouldNotThrow<UnexpectedCSVValueType> {
            schemaBasedCSVRecordParser.parseRecord(values, 1, headers)
        }
    }

    @Test
    fun `Given a list of record values, when it contains a malformed value that does not follow the value constraint of the closed header namespace, it throws an exception`() {
        val values = listOf("156541<decimal>", "1", "some string value", "closed-value-namespace:option2")

        shouldThrowNestedException<InvalidCSVValue> {
            schemaBasedCSVRecordParser.parseRecord(values, 1, headers)
        }.asClue {
            it.message shouldBe """Invalid value "1" in row 1, column 2. Reason: Value "1" does not match pattern "[A-Za-z ]+"."""
        }
    }

    @Test
    fun `Given a list of record values, when it contains a malformed value, it throws an exception`() {
        val values = listOf("156541.0<int>", "true", "some string value", "closed-value-namespace:option2")

        shouldThrowNestedException<InvalidCSVValue> {
            schemaBasedCSVRecordParser.parseRecord(values, 1, headers)
        }.asClue {
            it.message shouldBe """Invalid value "156541.0" in row 1, column 1. Reason: Value cannot be parsed as type "Integer"."""
        }
    }

    @Test
    fun `Given a list of record values, when it contains an empty value, it parses the value as null`() {
        val values = listOf("", "true", "some string value", "closed-value-namespace:option2")
        val expected = listOf(
            TypedValue(
                namespace = null,
                value = null,
                type = Classes.string,
            ),
            TypedValue(
                namespace = null,
                value = "true",
                type = Classes.boolean,
            ),
            TypedValue(
                namespace = null,
                value = "some string value",
                type = Classes.string,
            ),
            TypedValue(
                namespace = "closed-value-namespace",
                value = "option2",
                type = Classes.string,
            )
        )

        schemaBasedCSVRecordParser.parseRecord(values, 1, headers) shouldBe expected
    }

    @Test
    fun `Given a list of record values, when it contains a value with an unregistered namespace, it parses the namespace as part of the value`() {
        val values = listOf("unknown-namespace:is part of the value", "true", "some string value", "closed-value-namespace:option2")
        val expected = listOf(
            TypedValue(
                namespace = null,
                value = "unknown-namespace:is part of the value",
                type = Classes.string,
            ),
            TypedValue(
                namespace = null,
                value = "true",
                type = Classes.boolean,
            ),
            TypedValue(
                namespace = null,
                value = "some string value",
                type = Classes.string,
            ),
            TypedValue(
                namespace = "closed-value-namespace",
                value = "option2",
                type = Classes.string,
            )
        )

        schemaBasedCSVRecordParser.parseRecord(values, 1, headers) shouldBe expected
    }

    inline fun <reified T : Throwable> shouldThrowNestedException(block: () -> Any?): T =
        shouldThrow<RecordParsingException> { block() }
            .asClue {
                it.causes.size shouldBe 1
                it.causes.single().shouldBeInstanceOf<T>()
            }
}
