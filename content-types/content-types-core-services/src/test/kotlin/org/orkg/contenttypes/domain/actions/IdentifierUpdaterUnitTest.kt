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
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Identifiers
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class IdentifierUpdaterUnitTest {
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val identifierCreator: IdentifierCreator = mockk()

    private val identifierUpdater = object : IdentifierUpdater(statementService, literalService, identifierCreator) {}

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, literalService)
    }

    @Test
    fun `Given a map of identifiers, when updating an identifier with the same key and value, it returns success`() {
        val subjectId = ThingId("Author")
        val contributorId = ContributorId(UUID.randomUUID())
        val orcid = "0000-0002-1825-0097"
        val oldIdentifiers = mapOf("orcid" to listOf(orcid))
        val newIdentifiers = mapOf("orcid" to listOf(orcid))

        identifierUpdater.update(contributorId, oldIdentifiers, newIdentifiers, Identifiers.author, subjectId)
    }

    @Test
    fun `Given a map of identifiers, when updating an identifier with the same key but different value, it replaces the identifier`() {
        val authorId = ThingId("Author")
        val contributorId = ContributorId(UUID.randomUUID())
        val oldOrcid = "0000-0002-1825-0097"
        val newOrcid = "0000-0002-1825-0098"
        val oldIdentifiers = mapOf("orcid" to listOf(oldOrcid))
        val newIdentifiers = mapOf("orcid" to listOf(newOrcid))
        val statementId = StatementId("S1")

        every {
            statementService.findAllBySubjectAndPredicate(
                subjectId = authorId,
                predicateId = Predicates.hasORCID,
                pagination = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(authorId),
                predicate = createPredicate(Predicates.hasORCID),
                `object` = createLiteral(label = oldOrcid)
            )
        )
        every { statementService.delete(statementId) } just runs
        every { identifierCreator.create(contributorId, newIdentifiers, Identifiers.author, authorId) } just runs

        identifierUpdater.update(contributorId, oldIdentifiers, newIdentifiers, Identifiers.author, authorId)

        verify(exactly = 1) {
            statementService.findAllBySubjectAndPredicate(
                subjectId = authorId,
                predicateId = Predicates.hasORCID,
                pagination = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(statementId) }
        verify(exactly = 1) { identifierCreator.create(contributorId, newIdentifiers, Identifiers.author, authorId) }
    }

    @Test
    fun `Given a map of identifiers, when new identifiers are empty, it removes the old identifiers`() {
        val authorId = ThingId("Author")
        val contributorId = ContributorId(UUID.randomUUID())
        val oldOrcid = "0000-0002-1825-0097"
        val oldIdentifiers = mapOf("orcid" to listOf(oldOrcid))
        val newIdentifiers = emptyMap<String, List<String>>()
        val statementId = StatementId("S1")

        every {
            statementService.findAllBySubjectAndPredicate(
                subjectId = authorId,
                predicateId = Predicates.hasORCID,
                pagination = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(authorId),
                predicate = createPredicate(Predicates.hasORCID),
                `object` = createLiteral(label = oldOrcid)
            )
        )
        every { statementService.delete(statementId) } just runs

        identifierUpdater.update(contributorId, oldIdentifiers, newIdentifiers, Identifiers.author, authorId)

        verify(exactly = 1) {
            statementService.findAllBySubjectAndPredicate(
                subjectId = authorId,
                predicateId = Predicates.hasORCID,
                pagination = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(statementId) }
    }

    @Test
    fun `Given a map of identifiers, when there are no old identifiers, it creates the new identifiers`() {
        val authorId = ThingId("Author")
        val contributorId = ContributorId(UUID.randomUUID())
        val orcid = "0000-0002-1825-0097"
        val newIdentifiers = mapOf("orcid" to listOf(orcid))

        every { identifierCreator.create(contributorId, newIdentifiers, Identifiers.author, authorId) } just runs

        identifierUpdater.update(contributorId, emptyMap(), newIdentifiers, Identifiers.author, authorId)

        verify(exactly = 1) { identifierCreator.create(contributorId, newIdentifiers, Identifiers.author, authorId) }
    }
}
