package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.MissingTableHeaderValue
import org.orkg.contenttypes.domain.MissingTableRowValues
import org.orkg.contenttypes.domain.MissingTableRows
import org.orkg.contenttypes.domain.TooManyTableRowValues
import org.orkg.contenttypes.input.RowCommand

internal class TableDimensionsValidatorUnitTest {
    private val tableDimensionsValidator = TableDimensionsValidator<List<RowCommand>?, Unit> { it }

    @Test
    fun `Given a list of row definitions, when validating, it returns success`() {
        val rows = listOf(
            RowCommand(
                label = null,
                data = listOf("1", "2", "3")
            ),
            RowCommand(
                label = "contents",
                data = listOf(null, "content", "other")
            )
        )
        tableDimensionsValidator(rows, Unit)
    }

    @Test
    fun `Given a list of row definitions, when null, it returns success`() {
        tableDimensionsValidator(null, Unit)
    }

    @Test
    fun `Given a list of row definitions, when empty, it throws an exception`() {
        shouldThrow<MissingTableRows> { tableDimensionsValidator(listOf(), Unit) }
    }

    @Test
    fun `Given a list of row definitions, when header is missing a value, it throws an exception`() {
        val rows = listOf(
            RowCommand(
                label = null,
                data = listOf("1", null, "3")
            )
        )
        shouldThrow<MissingTableHeaderValue> { tableDimensionsValidator(rows, Unit) }
    }

    @Test
    fun `Given a list of row definitions, when header value is blank, it throws an exception`() {
        val rows = listOf(
            RowCommand(
                label = null,
                data = listOf("1", "2", "")
            )
        )
        shouldThrow<MissingTableHeaderValue> { tableDimensionsValidator(rows, Unit) }
    }

    @Test
    fun `Given a list of row definitions, when data row has more values than header, it throws an exception`() {
        val rows = listOf(
            RowCommand(
                label = null,
                data = listOf("1", "2", "3")
            ),
            RowCommand(
                label = "contents",
                data = listOf("1", "2", "3", "too many")
            )
        )
        shouldThrow<TooManyTableRowValues> { tableDimensionsValidator(rows, Unit) }
    }

    @Test
    fun `Given a list of row definitions, when data row has less values than header, it throws an exception`() {
        val rows = listOf(
            RowCommand(
                label = null,
                data = listOf("1", "2", "3")
            ),
            RowCommand(
                label = "contents",
                data = listOf("too", "few")
            )
        )
        shouldThrow<MissingTableRowValues> { tableDimensionsValidator(rows, Unit) }
    }
}
