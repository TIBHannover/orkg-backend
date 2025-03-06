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

internal class AbstractTableColumnCreatorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()

    private val abstractTableColumnCreator = AbstractTableColumnCreator(
        unsafeResourceUseCases,
        unsafeStatementUseCases,
        unsafeLiteralUseCases
    )

    @Test
    fun `Given a table column command, when creating, it returns success`() {
        val contributorId = ContributorId(MockUserId.USER)
        val tableId = ThingId("Table")
        val index = 5
        val columnTitleLiteralId = ThingId("TitleLiteral")
        val columnId = ThingId("Column")
        val columnNumberLiteralId = ThingId("ColumnNumberLiteral")

        val createColumnResourceCommand = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = "",
            classes = setOf(Classes.column),
        )
        val createColumnNumberLiteralCommand = CreateLiteralUseCase.CreateCommand(
            contributorId = contributorId,
            label = "${index + 1}",
            datatype = Literals.XSD.INT.prefixedUri
        )
        val createColumnNumberStatementCommand = CreateStatementUseCase.CreateCommand(
            contributorId = contributorId,
            subjectId = columnId,
            predicateId = Predicates.csvwNumber,
            objectId = columnNumberLiteralId
        )
        val createColumnTitleStatementCommand = CreateStatementUseCase.CreateCommand(
            contributorId = contributorId,
            subjectId = columnId,
            predicateId = Predicates.csvwTitles,
            objectId = columnTitleLiteralId
        )
        val createColumnStatementCommand = CreateStatementUseCase.CreateCommand(
            contributorId = contributorId,
            subjectId = tableId,
            predicateId = Predicates.csvwColumns,
            objectId = columnId
        )

        every { unsafeResourceUseCases.create(createColumnResourceCommand) } returns columnId
        every { unsafeLiteralUseCases.create(createColumnNumberLiteralCommand) } returns columnNumberLiteralId
        every { unsafeStatementUseCases.create(createColumnNumberStatementCommand) } returns StatementId("S1")
        every { unsafeStatementUseCases.create(createColumnTitleStatementCommand) } returns StatementId("S2")
        every { unsafeStatementUseCases.create(createColumnStatementCommand) } returns StatementId("S3")

        val result = abstractTableColumnCreator.create(
            contributorId = contributorId,
            tableId = tableId,
            index = index,
            titleLiteralId = columnTitleLiteralId
        )

        result shouldBe columnId

        verify(exactly = 1) { unsafeResourceUseCases.create(createColumnResourceCommand) }
        verify(exactly = 1) { unsafeLiteralUseCases.create(createColumnNumberLiteralCommand) }
        verify(exactly = 1) { unsafeStatementUseCases.create(createColumnNumberStatementCommand) }
        verify(exactly = 1) { unsafeStatementUseCases.create(createColumnTitleStatementCommand) }
        verify(exactly = 1) { unsafeStatementUseCases.create(createColumnStatementCommand) }
    }
}
