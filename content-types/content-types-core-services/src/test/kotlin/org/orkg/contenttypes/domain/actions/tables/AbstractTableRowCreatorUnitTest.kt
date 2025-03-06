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
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.testing.MockUserId

internal class AbstractTableRowCreatorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()

    private val abstractTableRowCreator = AbstractTableRowCreator(
        unsafeResourceUseCases,
        unsafeStatementUseCases,
        unsafeLiteralUseCases
    )

    @Test
    fun `Given a table row command, when creating a row with label, it returns success`() {
        val contributorId = ContributorId(MockUserId.USER)
        val tableId = ThingId("Table")
        val index = 5
        val label = "Row title"
        val rowLabelLiteralId = ThingId("Label")
        val rowId = ThingId("Row")
        val rowNumberLiteralId = ThingId("RowNumberLiteral")

        val createRowResourceCommand = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = "",
            classes = setOf(Classes.row),
        )
        val createRowNumberLiteralCommand = CreateLiteralUseCase.CreateCommand(
            contributorId = contributorId,
            label = "${index + 1}",
            datatype = Literals.XSD.INT.prefixedUri
        )
        val createRowNumberStatementCommand = CreateStatementUseCase.CreateCommand(
            contributorId = contributorId,
            subjectId = rowId,
            predicateId = Predicates.csvwNumber,
            objectId = rowNumberLiteralId
        )
        val createRowLabelLiteralCommand = CreateLiteralUseCase.CreateCommand(
            contributorId = contributorId,
            label = label
        )
        val createRowTitleStatementCommand = CreateStatementUseCase.CreateCommand(
            contributorId = contributorId,
            subjectId = rowId,
            predicateId = Predicates.csvwTitles,
            objectId = rowLabelLiteralId
        )
        val createRowStatementCommand = CreateStatementUseCase.CreateCommand(
            contributorId = contributorId,
            subjectId = tableId,
            predicateId = Predicates.csvwRows,
            objectId = rowId
        )

        every { unsafeResourceUseCases.create(createRowResourceCommand) } returns rowId
        every { unsafeLiteralUseCases.create(createRowNumberLiteralCommand) } returns rowNumberLiteralId
        every { unsafeStatementUseCases.create(createRowNumberStatementCommand) } returns StatementId("S1")
        every { unsafeLiteralUseCases.create(createRowLabelLiteralCommand) } returns rowLabelLiteralId
        every { unsafeStatementUseCases.create(createRowTitleStatementCommand) } returns StatementId("S2")
        every { unsafeStatementUseCases.create(createRowStatementCommand) } returns StatementId("S3")

        val result = abstractTableRowCreator.create(contributorId, tableId, index, label)

        result shouldBe rowId

        verify(exactly = 1) { unsafeResourceUseCases.create(createRowResourceCommand) }
        verify(exactly = 1) { unsafeLiteralUseCases.create(createRowNumberLiteralCommand) }
        verify(exactly = 1) { unsafeStatementUseCases.create(createRowNumberStatementCommand) }
        verify(exactly = 1) { unsafeLiteralUseCases.create(createRowLabelLiteralCommand) }
        verify(exactly = 1) { unsafeStatementUseCases.create(createRowTitleStatementCommand) }
        verify(exactly = 1) { unsafeStatementUseCases.create(createRowStatementCommand) }
    }

    @Test
    fun `Given a table row command, when creating a row without label, it returns success`() {
        val contributorId = ContributorId(MockUserId.USER)
        val tableId = ThingId("Table")
        val index = 5
        val label = null
        val rowId = ThingId("Row")
        val rowNumberLiteralId = ThingId("RowNumberLiteral")

        val createRowResourceCommand = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = "",
            classes = setOf(Classes.row),
        )
        val createRowNumberLiteralCommand = CreateLiteralUseCase.CreateCommand(
            contributorId = contributorId,
            label = "${index + 1}",
            datatype = Literals.XSD.INT.prefixedUri
        )
        val createRowNumberStatementCommand = CreateStatementUseCase.CreateCommand(
            contributorId = contributorId,
            subjectId = rowId,
            predicateId = Predicates.csvwNumber,
            objectId = rowNumberLiteralId
        )
        val createRowStatementCommand = CreateStatementUseCase.CreateCommand(
            contributorId = contributorId,
            subjectId = tableId,
            predicateId = Predicates.csvwRows,
            objectId = rowId
        )

        every { unsafeResourceUseCases.create(createRowResourceCommand) } returns rowId
        every { unsafeLiteralUseCases.create(createRowNumberLiteralCommand) } returns rowNumberLiteralId
        every { unsafeStatementUseCases.create(createRowNumberStatementCommand) } returns StatementId("S1")
        every { unsafeStatementUseCases.create(createRowStatementCommand) } returns StatementId("S3")

        val result = abstractTableRowCreator.create(contributorId, tableId, index, label)

        result shouldBe rowId

        verify(exactly = 1) { unsafeResourceUseCases.create(createRowResourceCommand) }
        verify(exactly = 1) { unsafeLiteralUseCases.create(createRowNumberLiteralCommand) }
        verify(exactly = 1) { unsafeStatementUseCases.create(createRowNumberStatementCommand) }
        verify(exactly = 1) { unsafeStatementUseCases.create(createRowStatementCommand) }
    }
}
