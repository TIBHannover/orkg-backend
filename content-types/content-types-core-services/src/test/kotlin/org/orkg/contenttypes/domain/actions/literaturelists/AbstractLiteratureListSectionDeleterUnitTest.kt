package org.orkg.contenttypes.domain.actions.literaturelists

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
import org.junit.jupiter.api.assertDoesNotThrow
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteratureListListSection
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteratureListTextSection
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

class AbstractLiteratureListSectionDeleterUnitTest {
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val abstractLiteratureListSectionDeleter = AbstractLiteratureListSectionDeleter(statementService, resourceService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, resourceService)
    }

    @Test
    fun `Given a list section, when referenced by no resource other than the literature list, it deletes the list section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literatureListId = ThingId("R123")
        val section = createDummyLiteratureListListSection()
        val statements = section.toGroupedStatements()
        val literatureListHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(literatureListId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(literatureListHasSectionStatement)
        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } just runs

        abstractLiteratureListSectionDeleter.delete(contributorId, literatureListId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementService.delete(setOf(
                StatementId("S0"),
                StatementId("S1"),
                StatementId("S0_2"),
                StatementId("S1_2"),
                StatementId("S0_3"),
                literatureListHasSectionStatement.id
            ))
        }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
        verify(exactly = 1) { resourceService.delete(ThingId("R0"), contributorId) }
        verify(exactly = 1) { resourceService.delete(ThingId("R1"), contributorId) }
    }

    @Test
    fun `Given a list section, when referenced by another resource, it just removes the list section from the literature list`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literatureListId = ThingId("R123")
        val section = createDummyLiteratureListListSection()
        val statements = section.toGroupedStatements()
        val literatureListHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(literatureListId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )
        val otherStatementAboutListSection = createStatement(
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
        } returns pageOf(literatureListHasSectionStatement, otherStatementAboutListSection)
        every { statementService.delete(setOf(literatureListHasSectionStatement.id)) } just runs

        abstractLiteratureListSectionDeleter.delete(contributorId, literatureListId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(setOf(literatureListHasSectionStatement.id)) }
    }

    @Test
    fun `Given a list section, when list section is owned by another user, it does not throw an exception`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literatureListId = ThingId("R123")
        val section = createDummyLiteratureListListSection()
        val statements = section.toGroupedStatements()
        val literatureListHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(literatureListId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(literatureListHasSectionStatement)
        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } throws NeitherOwnerNorCurator(contributorId)

        assertDoesNotThrow {
            abstractLiteratureListSectionDeleter.delete(contributorId, literatureListId, section, statements)
        }

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementService.delete(setOf(
                StatementId("S0"),
                StatementId("S1"),
                StatementId("S0_2"),
                StatementId("S1_2"),
                StatementId("S0_3"),
                literatureListHasSectionStatement.id
            ))
        }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
        verify(exactly = 1) { resourceService.delete(ThingId("R0"), contributorId) }
        verify(exactly = 1) { resourceService.delete(ThingId("R1"), contributorId) }
    }

    @Test
    fun `Given a text section, when referenced by no resource other than the literature list, it deletes the text section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literatureListId = ThingId("R123")
        val section = createDummyLiteratureListTextSection()
        val statements = section.toGroupedStatements()
        val literatureListHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(literatureListId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(literatureListHasSectionStatement)
        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } just runs

        abstractLiteratureListSectionDeleter.delete(contributorId, literatureListId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(setOf(StatementId("S1"), StatementId("S2"), literatureListHasSectionStatement.id)) }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
    }

    @Test
    fun `Given a text section, when referenced by another resource, it just removes the text section from the literature list`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literatureListId = ThingId("R123")
        val section = createDummyLiteratureListTextSection()
        val statements = section.toGroupedStatements()
        val literatureListHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(literatureListId),
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
        } returns pageOf(literatureListHasSectionStatement, otherStatementAboutTextSection)
        every { statementService.delete(setOf(literatureListHasSectionStatement.id)) } just runs

        abstractLiteratureListSectionDeleter.delete(contributorId, literatureListId, section, statements)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(setOf(literatureListHasSectionStatement.id)) }
    }

    @Test
    fun `Given a text section, when text section is owned by another user, it does not throw an exception`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literatureListId = ThingId("R123")
        val section = createDummyLiteratureListTextSection()
        val statements = section.toGroupedStatements()
        val literatureListHasSectionStatement = createStatement(
            id = StatementId("S123"),
            subject = createResource(literatureListId),
            predicate = createPredicate(Predicates.hasSection),
            `object` = createResource(section.id)
        )

        every {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        } returns pageOf(literatureListHasSectionStatement)
        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } throws NeitherOwnerNorCurator(contributorId)

        assertDoesNotThrow {
            abstractLiteratureListSectionDeleter.delete(contributorId, literatureListId, section, statements)
        }

        verify(exactly = 1) {
            statementService.findAll(
                objectId = section.id,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(setOf(StatementId("S1"), StatementId("S2"), literatureListHasSectionStatement.id)) }
        verify(exactly = 1) { resourceService.delete(section.id, contributorId) }
    }
}
