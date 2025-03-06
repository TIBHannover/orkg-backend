package org.orkg.contenttypes.domain.actions.tables

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.testing.MockUserId

internal class AbstractTableCellCreatorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()

    private val abstractTableCellCreator = AbstractTableCellCreator(
        unsafeResourceUseCases,
        unsafeStatementUseCases
    )

    @Test
    fun `Given a table cell command, when creating a cell with value, it returns success`() {
        val contributorId = ContributorId(MockUserId.USER)
        val rowId = ThingId("Row1")
        val columnId = ThingId("Column1")
        val cellValueId = ThingId("CellValue")
        val cellId = ThingId("Cell")

        val createCellResourceCommand = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = "",
            classes = setOf(Classes.cell),
        )
        val createCellColumnStatementCommand = CreateStatementUseCase.CreateCommand(
            contributorId = contributorId,
            subjectId = cellId,
            predicateId = Predicates.csvwColumn,
            objectId = columnId
        )
        val createCellValueStatementCommand = CreateStatementUseCase.CreateCommand(
            contributorId = contributorId,
            subjectId = cellId,
            predicateId = Predicates.csvwValue,
            objectId = cellValueId
        )
        val createCellStatementCommand = CreateStatementUseCase.CreateCommand(
            contributorId = contributorId,
            subjectId = rowId,
            predicateId = Predicates.csvwCells,
            objectId = cellId
        )

        every { unsafeResourceUseCases.create(createCellResourceCommand) } returns cellId
        every { unsafeStatementUseCases.create(createCellColumnStatementCommand) } returns StatementId("S1")
        every { unsafeStatementUseCases.create(createCellValueStatementCommand) } returns StatementId("S2")
        every { unsafeStatementUseCases.create(createCellStatementCommand) } returns StatementId("S3")

        val result = abstractTableCellCreator.create(contributorId, rowId, columnId, cellValueId)

        result shouldBe cellId

        verify(exactly = 1) { unsafeResourceUseCases.create(createCellResourceCommand) }
        verify(exactly = 1) { unsafeStatementUseCases.create(createCellColumnStatementCommand) }
        verify(exactly = 1) { unsafeStatementUseCases.create(createCellValueStatementCommand) }
        verify(exactly = 1) { unsafeStatementUseCases.create(createCellStatementCommand) }
    }

    @Test
    fun `Given a table cell command, when creating a cell without value, it returns success`() {
        val contributorId = ContributorId(MockUserId.USER)
        val rowId = ThingId("Row1")
        val columnId = ThingId("Column1")
        val cellId = ThingId("Cell")

        val createCellResourceCommand = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = "",
            classes = setOf(Classes.cell),
        )
        val createCellColumnStatementCommand = CreateStatementUseCase.CreateCommand(
            contributorId = contributorId,
            subjectId = cellId,
            predicateId = Predicates.csvwColumn,
            objectId = columnId
        )
        val createCellStatementCommand = CreateStatementUseCase.CreateCommand(
            contributorId = contributorId,
            subjectId = rowId,
            predicateId = Predicates.csvwCells,
            objectId = cellId
        )

        every { unsafeResourceUseCases.create(createCellResourceCommand) } returns cellId
        every { unsafeStatementUseCases.create(createCellColumnStatementCommand) } returns StatementId("S1")
        every { unsafeStatementUseCases.create(createCellStatementCommand) } returns StatementId("S3")

        val result = abstractTableCellCreator.create(contributorId, rowId, columnId, null)

        result shouldBe cellId

        verify(exactly = 1) { unsafeResourceUseCases.create(createCellResourceCommand) }
        verify(exactly = 1) { unsafeStatementUseCases.create(createCellColumnStatementCommand) }
        verify(exactly = 1) { unsafeStatementUseCases.create(createCellStatementCommand) }
    }
}
