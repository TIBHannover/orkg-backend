package org.orkg.contenttypes.domain.actions

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createLiteral

class DescriptionCreatorUnitTest {
    private val literalService: LiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val descriptionCreator = object : DescriptionCreator(
        literalService = literalService,
        statementService = statementService
    ) {}

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(literalService, statementService)
    }

    @Test
    fun `Given a description and a subject id, it creates a new description literal and links it to the subject resource`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val description = "some description"
        val literal = createLiteral()

        every {
            literalService.create(
                userId = contributorId,
                label = description
            )
        } returns literal
        every {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.description,
                `object` = literal.id
            )
        } just runs

        descriptionCreator.create(contributorId, subjectId, description)

        verify(exactly = 1) {
            literalService.create(
                userId = contributorId,
                label = description
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.description,
                `object` = literal.id
            )
        }
    }
}