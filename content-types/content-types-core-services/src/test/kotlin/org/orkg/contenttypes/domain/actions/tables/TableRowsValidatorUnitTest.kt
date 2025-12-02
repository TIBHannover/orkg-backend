package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.input.CreateRowCommand
import org.orkg.graph.domain.InvalidLabel

internal class TableRowsValidatorUnitTest {
    private val tableRowsValidator = TableRowsValidator<List<CreateRowCommand>?, Unit> { it }

    @Test
    fun `Given a list of row definitions, when validating their labels, it returns success`() {
        val rows = listOf(
            CreateRowCommand(
                label = null,
                data = emptyList()
            ),
            CreateRowCommand(
                label = "contents",
                data = emptyList()
            )
        )
        tableRowsValidator(rows, Unit)
    }

    @Test
    fun `Given a list of row definitions, when row label is invalid, it throws an exception`() {
        val rows = listOf(
            CreateRowCommand(
                label = "\n",
                data = emptyList()
            )
        )
        shouldThrow<InvalidLabel> { tableRowsValidator(rows, Unit) }.asClue {
            it.property shouldBe "rows[0].label"
        }
    }

    @Test
    fun `Given a list of row definitions, when null, it returns success`() {
        tableRowsValidator(null, Unit)
    }
}
