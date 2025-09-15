package org.orkg.contenttypes.domain.actions.tables.cells

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.CannotDeleteTableHeader
import org.orkg.contenttypes.domain.TableHeaderValueMustBeLiteral
import org.orkg.contenttypes.domain.actions.UpdateTableCellState
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.input.testing.fixtures.updateTableCellCommand
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class TableCellValueValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()

    private val tableCellValueValidator = TableCellValueValidator(thingRepository)

    @Test
    fun `Given a table cell update command, when validating the cell value, it returns success`() {
        val command = updateTableCellCommand().copy(rowIndex = 1)
        val state = UpdateTableCellState(table = createTable())

        every { thingRepository.findById(command.id!!) } returns Optional.of(createResource())

        tableCellValueValidator(command, state) shouldBe state

        verify(exactly = 1) { thingRepository.findById(command.id!!) }
    }

    @Test
    fun `Given a table cell update command, when validating the cell value, and thing does not exist, it throws an exception`() {
        val command = updateTableCellCommand()
        val state = UpdateTableCellState(table = createTable())

        every { thingRepository.findById(command.id!!) } returns Optional.empty()

        shouldThrow<ThingNotFound> { tableCellValueValidator(command, state) }

        verify(exactly = 1) { thingRepository.findById(command.id!!) }
    }

    @Test
    fun `Given a table cell update command, when validating the cell value, and value should be used in header and is not a literal, it throws an exception`() {
        val command = updateTableCellCommand().copy(rowIndex = 0)
        val state = UpdateTableCellState(table = createTable())

        every { thingRepository.findById(command.id!!) } returns Optional.of(createResource())

        shouldThrow<TableHeaderValueMustBeLiteral> { tableCellValueValidator(command, state) }

        verify(exactly = 1) { thingRepository.findById(command.id!!) }
    }

    @Test
    fun `Given a table cell update command, when validating the cell value, and value should be used in header and value is a literal, it returns success`() {
        val command = updateTableCellCommand().copy(rowIndex = 0)
        val state = UpdateTableCellState(table = createTable())

        every { thingRepository.findById(command.id!!) } returns Optional.of(createLiteral())

        tableCellValueValidator(command, state) shouldBe state

        verify(exactly = 1) { thingRepository.findById(command.id!!) }
    }

    @Test
    fun `Given a table cell update command, when validating the cell value, and value should be used in header and value is null, it throws an exception`() {
        val command = updateTableCellCommand().copy(rowIndex = 0, id = null)
        val state = UpdateTableCellState(table = createTable())

        shouldThrow<CannotDeleteTableHeader> { tableCellValueValidator(command, state) }
    }
}
