package org.orkg.contenttypes.domain.actions.smartreviews

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReviewComparisonSection
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReviewOntologySection
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReviewPredicateSection
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReviewResourceSection
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReviewTextSection
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReviewVisualizationSection
import org.orkg.contenttypes.domain.testing.fixtures.toGroupedStatements
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

internal class AbstractSmartReviewSectionDeleterUnitTest : MockkBaseTest {
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val abstractTemplatePropertyDeleter = AbstractSmartReviewSectionDeleter(statementService, resourceService)

    @Test
    fun `Given a comparison section, when referenced by no resource other than the smart review contribution, it deletes the comparison section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewComparisonSection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement)
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } just runs

        abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S1"), contributionHasSectionStatement.id)) }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
    }

    @Test
    fun `Given a comparison section, when referenced by another resource, it just removes the comparison section from the smart review`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewComparisonSection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )
        val otherStatementAboutComparisonSection = createStatement(
            id = StatementId("S456"),
            subject = createResource(),
            predicate = createPredicate(Predicates.hasLink),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement, otherStatementAboutComparisonSection)
        every { statementService.deleteAllById(setOf(contributionHasSectionStatement.id)) } just runs

        abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(contributionHasSectionStatement.id)) }
    }

    @Test
    fun `Given a comparison section, when comparison section is owned by another user, it does not throw an exception`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewComparisonSection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement)
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } throws NeitherOwnerNorCurator(contributorId)

        assertDoesNotThrow {
            abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)
        }

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S1"), contributionHasSectionStatement.id)) }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
    }

    @Test
    fun `Given a visualization section, when referenced by no resource other than the smart review contribution, it deletes the visualization section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewVisualizationSection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement)
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } just runs

        abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S1"), contributionHasSectionStatement.id)) }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
    }

    @Test
    fun `Given a visualization section, when referenced by another resource, it just removes the visualization section from the smart review`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewVisualizationSection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )
        val otherStatementAboutVisualizationSection = createStatement(
            id = StatementId("S456"),
            subject = createResource(),
            predicate = createPredicate(Predicates.hasLink),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement, otherStatementAboutVisualizationSection)
        every { statementService.deleteAllById(setOf(contributionHasSectionStatement.id)) } just runs

        abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(contributionHasSectionStatement.id)) }
    }

    @Test
    fun `Given a visualization section, when visualization section is owned by another user, it does not throw an exception`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewVisualizationSection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement)
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } throws NeitherOwnerNorCurator(contributorId)

        assertDoesNotThrow {
            abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)
        }

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S1"), contributionHasSectionStatement.id)) }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
    }

    @Test
    fun `Given a resource section, when referenced by no resource other than the smart review contribution, it deletes the resource section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewResourceSection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement)
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } just runs

        abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S1"), contributionHasSectionStatement.id)) }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
    }

    @Test
    fun `Given a resource section, when referenced by another resource, it just removes the resource section from the smart review`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewResourceSection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )
        val otherStatementAboutResourceSection = createStatement(
            id = StatementId("S456"),
            subject = createResource(),
            predicate = createPredicate(Predicates.hasLink),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement, otherStatementAboutResourceSection)
        every { statementService.deleteAllById(setOf(contributionHasSectionStatement.id)) } just runs

        abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(contributionHasSectionStatement.id)) }
    }

    @Test
    fun `Given a resource section, when resource section is owned by another user, it does not throw an exception`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewResourceSection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement)
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } throws NeitherOwnerNorCurator(contributorId)

        assertDoesNotThrow {
            abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)
        }

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S1"), contributionHasSectionStatement.id)) }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
    }

    @Test
    fun `Given a predicate section, when referenced by no resource other than the smart review contribution, it deletes the predicate section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewPredicateSection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement)
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } just runs

        abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S1"), contributionHasSectionStatement.id)) }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
    }

    @Test
    fun `Given a predicate section, when referenced by another resource, it just removes the predicate section from the smart review`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewPredicateSection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )
        val otherStatementAboutPredicateSection = createStatement(
            id = StatementId("S456"),
            subject = createResource(),
            predicate = createPredicate(Predicates.hasLink),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement, otherStatementAboutPredicateSection)
        every { statementService.deleteAllById(setOf(contributionHasSectionStatement.id)) } just runs

        abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(contributionHasSectionStatement.id)) }
    }

    @Test
    fun `Given a predicate section, when predicate section is owned by another user, it does not throw an exception`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewPredicateSection()
        val statements = section.toGroupedStatements()
        val smartReviewHasSectionStatement = createStatement(
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(smartReviewHasSectionStatement)
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } throws NeitherOwnerNorCurator(contributorId)

        assertDoesNotThrow {
            abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)
        }

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S1"))) }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
    }

    @Test
    fun `Given a ontology section, when referenced by no resource other than the smart review contribution, it deletes the ontology section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewOntologySection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement)
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } just runs

        abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementService.deleteAllById(
                setOf(
                    StatementId("S0"),
                    StatementId("S1"),
                    StatementId("S2"),
                    StatementId("S123")
                )
            )
        }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
    }

    @Test
    fun `Given a ontology section, when referenced by another resource, it just removes the ontology section from the smart review`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewOntologySection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )
        val otherStatementAboutOntologySection = createStatement(
            subject = createResource(),
            predicate = createPredicate(Predicates.hasLink),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement, otherStatementAboutOntologySection)
        every { statementService.deleteAllById(setOf(contributionHasSectionStatement.id)) } just runs

        abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(contributionHasSectionStatement.id)) }
    }

    @Test
    fun `Given a ontology section, when ontology section is owned by another user, it does not throw an exception`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewOntologySection()
        val statements = section.toGroupedStatements()
        val smartReviewHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(smartReviewHasSectionStatement)
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } throws NeitherOwnerNorCurator(contributorId)

        assertDoesNotThrow {
            abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)
        }

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementService.deleteAllById(
                setOf(
                    StatementId("S0"),
                    StatementId("S1"),
                    StatementId("S2"),
                    StatementId("S123")
                )
            )
        }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
    }

    @Test
    fun `Given a text section, when referenced by no resource other than the smart review contribution, it deletes the text section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewTextSection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement)
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } just runs

        abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S1"), contributionHasSectionStatement.id)) }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
    }

    @Test
    fun `Given a text section, when referenced by another resource, it just removes the text section from the smart review`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewTextSection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )
        val otherStatementAboutTextSection = createStatement(
            id = StatementId("S456"),
            subject = createResource(),
            predicate = createPredicate(Predicates.hasLink),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement, otherStatementAboutTextSection)
        every { statementService.deleteAllById(setOf(contributionHasSectionStatement.id)) } just runs

        abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(contributionHasSectionStatement.id)) }
    }

    @Test
    fun `Given a text section, when text section is owned by another user, it does not throw an exception`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R123")
        val section = createSmartReviewTextSection()
        val statements = section.toGroupedStatements()
        val contributionHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(contributionId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(contributionHasSectionStatement)
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } throws NeitherOwnerNorCurator(contributorId)

        assertDoesNotThrow {
            abstractTemplatePropertyDeleter.delete(contributorId, contributionId, section, statements)
        }

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S1"), contributionHasSectionStatement.id)) }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
    }
}
