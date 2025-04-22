package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ContentTypeSubgraph
import org.orkg.contenttypes.domain.Table
import org.orkg.contenttypes.domain.TableNotFound
import org.orkg.contenttypes.domain.TableService
import org.orkg.contenttypes.domain.actions.UpdateTableState
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.input.testing.fixtures.updateTableCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import java.util.Optional

internal class TableExistenceValidatorUnitTest : MockkBaseTest {
    private val tableService: TableService = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val tableExistenceValidator = TableExistenceValidator(tableService, resourceRepository)

    @Test
    fun `Given a table update command, when checking for table existence, it returns success`() {
        val table = createTable()
        val command = updateTableCommand().copy(tableId = table.id)
        val state = UpdateTableState()
        val root = createResource(
            id = table.id,
            label = table.label,
            classes = setOf(Classes.table)
        )
        val statements = listOf(createStatement()).groupBy { it.subject.id }

        mockkObject(Table.Companion) {
            every { resourceRepository.findById(table.id) } returns Optional.of(root)
            every { tableService.findSubgraph(root) } returns ContentTypeSubgraph(root.id, statements)
            every { Table.from(root, statements) } returns table

            tableExistenceValidator(command, state).asClue {
                it.table shouldBe table
                it.statements shouldBe statements
                it.validationCache shouldBe state.validationCache
                it.tempIdToThingId shouldBe state.tempIdToThingId
                it.columns shouldBe state.columns
                it.rows shouldBe state.rows
            }

            verify(exactly = 1) { resourceRepository.findById(table.id) }
            verify(exactly = 1) { tableService.findSubgraph(root) }
            verify(exactly = 1) { Table.from(root, statements) }
        }
    }

    @Test
    fun `Given a table update command, when checking for table existence and table is not found, it throws an exception`() {
        val table = createTable()
        val command = updateTableCommand().copy(tableId = table.id)
        val state = UpdateTableState()

        every { resourceRepository.findById(table.id) } returns Optional.empty()

        shouldThrow<TableNotFound> { tableExistenceValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(table.id) }
    }
}
