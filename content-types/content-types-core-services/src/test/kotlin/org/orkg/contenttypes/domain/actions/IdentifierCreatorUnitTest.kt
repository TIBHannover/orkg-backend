package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.Identifiers
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

internal class IdentifierCreatorUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()

    private val identifierCreator = object : IdentifierCreator(statementService, literalService) {}

    @Test
    fun `Given a map of identifiers, it crates new paper identifiers`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val doi = "10.1234/56789"
        val identifiers = mapOf("doi" to listOf(doi))
        val doiLiteralId = ThingId("L1")
        val command = CreateStatementUseCase.CreateCommand(
            contributorId = contributorId,
            subjectId = paperId,
            predicateId = Predicates.hasDOI,
            objectId = doiLiteralId
        )

        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = doi
                )
            )
        } returns doiLiteralId
        every { statementService.create(command) } returns StatementId("S435")

        identifierCreator.create(contributorId, identifiers, Identifiers.paper, paperId)

        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = doi
                )
            )
        }
        verify(exactly = 1) { statementService.create(command) }
    }

    @Test
    fun `Given a map of identifiers, when an unknown identifier is specified, it does not create the identifier`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val identifiers = mapOf("unknown" to listOf("value"))

        identifierCreator.create(contributorId, identifiers, Identifiers.paper, paperId)

        verify(exactly = 0) { literalService.create(any()) }
        verify(exactly = 0) { statementService.create(any()) }
    }
}
