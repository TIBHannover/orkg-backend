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
import org.orkg.contenttypes.domain.Identifiers
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

class IdentifierUpdaterUnitTest {
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()

    private val identifierUpdater = IdentifierUpdater(statementCollectionPropertyUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementCollectionPropertyUpdater)
    }

    @Test
    fun `Given a map of identifiers, when existing identifiers are identical, it does nothing`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val orcid = "0000-0002-1825-0097"
        val newIdentifiers = mapOf("orcid" to listOf(orcid))
        val statements = mapOf(
            subjectId to listOf(
                createStatement(
                    subject = createResource(subjectId),
                    predicate = createPredicate(Predicates.hasORCID),
                    `object` = createLiteral(label = orcid)
                )
            )
        )

        identifierUpdater.update(statements, contributorId, newIdentifiers, Identifiers.author, subjectId)
    }

    @Test
    fun `Given a map of identifiers, when existing identifiers are different, it updates each identifier set`() {
        val authorId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val oldOrcid = "0000-0002-1825-0097"
        val newOrcid = "0000-0002-1825-0098"
        val newIdentifiers = mapOf("orcid" to listOf(newOrcid))
        val statements = mapOf(
            authorId to listOf(
                createStatement(
                    subject = createResource(authorId),
                    predicate = createPredicate(Predicates.hasORCID),
                    `object` = createLiteral(label = oldOrcid)
                )
            )
        )

        every {
            statementCollectionPropertyUpdater.update(
                statements = statements[authorId]!!,
                contributorId = contributorId,
                subjectId = authorId,
                predicateId = any(),
                literals = any<Set<String>>()
            )
        } just runs

        identifierUpdater.update(statements, contributorId, newIdentifiers, Identifiers.author, authorId)

        Identifiers.author.forEach {
            verify(exactly = 1) {
                statementCollectionPropertyUpdater.update(
                    statements = statements[authorId]!!,
                    contributorId = contributorId,
                    subjectId = authorId,
                    predicateId = it.predicateId,
                    literals = newIdentifiers[it.id].orEmpty().toSet()
                )
            }
        }
    }
}
