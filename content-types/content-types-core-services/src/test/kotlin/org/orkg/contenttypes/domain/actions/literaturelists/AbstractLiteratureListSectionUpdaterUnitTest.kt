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
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.testing.fixtures.createDummyListSection
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTextSection
import org.orkg.contenttypes.domain.testing.fixtures.toGroupedStatements
import org.orkg.contenttypes.input.ListSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.toListSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.toTextSectionDefinition
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class AbstractLiteratureListSectionUpdaterUnitTest {
    private val statementService: StatementUseCases = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater = mockk()

    private val abstractLiteratureListSectionUpdater = AbstractLiteratureListSectionUpdater(
        statementService, resourceService, singleStatementPropertyUpdater
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, resourceService, singleStatementPropertyUpdater)
    }

    @Test
    fun `Given a list section, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createDummyListSection()
        val newSection = oldSection.toListSectionDefinition()
        val statements = oldSection.toGroupedStatements()

        abstractLiteratureListSectionUpdater.updateListSection(contributorId, newSection, oldSection, statements)
    }

    @Test
    fun `Given a list section, when an entry has been deleted at the end of the list, it deletes the entry`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createDummyListSection()
        val newSection = oldSection.copy(entries = oldSection.entries.take(1)).toListSectionDefinition()
        val statements = oldSection.toGroupedStatements()

        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } just runs

        abstractLiteratureListSectionUpdater.updateListSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) { statementService.delete(setOf(StatementId("S1"), StatementId("S1_2"))) }
        verify(exactly = 1) { resourceService.delete(ThingId("R1"), contributorId) }
    }

    @Test
    fun `Given a list section, when an entry has been deleted at the beginning of the list, it deletes the entry`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createDummyListSection()
        val newSection = oldSection.copy(entries = oldSection.entries.drop(1)).toListSectionDefinition()
        val statements = oldSection.toGroupedStatements()

        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } just runs

        abstractLiteratureListSectionUpdater.updateListSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) { statementService.delete(setOf(StatementId("S0"), StatementId("S0_2"))) }
        verify(exactly = 1) { resourceService.delete(ThingId("R0"), contributorId) }
    }

    @Test
    fun `Given a list section, when an entry has been added to the beginning of the list, it creates a new hasEntry and hasLink statement and reassigns existing entry nodes`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createDummyListSection()
        val newSection = oldSection.toListSectionDefinition().copy(entries = listOf(ThingId("R789")) + oldSection.entries.map { it.id })
        val statements = oldSection.toGroupedStatements()
        val entryId = ThingId("R1564")

        every { resourceService.createUnsafe(any()) } returns entryId
        every { statementService.add(any(), any(), any(), any()) } just runs
        every { statementService.delete(any<Set<StatementId>>()) } just runs

        abstractLiteratureListSectionUpdater.updateListSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = ThingId("R0"),
                predicate = Predicates.hasLink,
                `object` = ThingId("R789")
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = ThingId("R1"),
                predicate = Predicates.hasLink,
                `object` = ThingId("R154686")
            )
        }
        verify(exactly = 1) { statementService.delete(setOf(StatementId("S0_2"), StatementId("S1_2"))) }
        verify(exactly = 1) {
            resourceService.createUnsafe(CreateResourceUseCase.CreateCommand(contributorId = contributorId, label = "Entry"))
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = oldSection.id,
                predicate = Predicates.hasEntry,
                `object` = entryId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = entryId,
                predicate = Predicates.hasLink,
                `object` = ThingId("R6416")
            )
        }
    }

    @Test
    fun `Given a list section, when an entry has been added to the end of the list, it creates a new hasEntry and hasLink statement`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createDummyListSection()
        val newSection = oldSection.toListSectionDefinition().copy(entries = oldSection.entries.map { it.id } + ThingId("R789"))
        val statements = oldSection.toGroupedStatements()
        val entryId = ThingId("R1564")

        every { resourceService.createUnsafe(any()) } returns entryId
        every { statementService.add(any(), any(), any(), any()) } just runs

        abstractLiteratureListSectionUpdater.updateListSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            resourceService.createUnsafe(CreateResourceUseCase.CreateCommand(contributorId = contributorId, label = "Entry"))
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = oldSection.id,
                predicate = Predicates.hasEntry,
                `object` = entryId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = entryId,
                predicate = Predicates.hasLink,
                `object` = ThingId("R789")
            )
        }
    }

    @Test
    fun `Given a list section, when an entry has been inserted to the list, it reassigns updates the hasLink statement and creates a new hasEntry and hasLink statement`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createDummyListSection()
        val newSection = ListSectionCommand(
            entries = listOf(
                ThingId("R154686"),
                ThingId("new"),
                ThingId("R6416")
            )
        )
        val statements = oldSection.toGroupedStatements()
        val entryId = ThingId("R1564")

        every { resourceService.createUnsafe(any()) } returns entryId
        every { statementService.add(any(), any(), any(), any()) } just runs
        every { statementService.delete(any<Set<StatementId>>()) } just runs

        abstractLiteratureListSectionUpdater.updateListSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = ThingId("R1"),
                predicate = Predicates.hasLink,
                `object` = ThingId("new")
            )
        }
        verify(exactly = 1) { statementService.delete(setOf(StatementId("S1_2"))) }
        verify(exactly = 1) {
            resourceService.createUnsafe(CreateResourceUseCase.CreateCommand(contributorId = contributorId, label = "Entry"))
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = oldSection.id,
                predicate = Predicates.hasEntry,
                `object` = entryId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = entryId,
                predicate = Predicates.hasLink,
                `object` = ThingId("R6416")
            )
        }
    }

    @Test
    fun `Given a text section, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createDummyTextSection()
        val newSection = oldSection.toTextSectionDefinition()
        val statements = oldSection.toGroupedStatements()

        abstractLiteratureListSectionUpdater.updateTextSection(contributorId, newSection, oldSection, statements)
    }

    @Test
    fun `Given a text section, when heading has changes, it updates the resource`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createDummyTextSection()
        val newSection = oldSection.toTextSectionDefinition().copy(heading = "new heading")
        val statements = oldSection.toGroupedStatements()

        every { resourceService.update(any()) } just runs

        abstractLiteratureListSectionUpdater.updateTextSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    label = newSection.heading
                )
            )
        }
    }

    @Test
    fun `Given a text section, when heading size has changed, it updates the literal`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createDummyTextSection()
        val newSection = oldSection.toTextSectionDefinition().copy(headingSize = 5)
        val statements = oldSection.toGroupedStatements()

        every {
            singleStatementPropertyUpdater.updateRequiredProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasHeadingLevel,
                label = newSection.headingSize.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } just runs

        abstractLiteratureListSectionUpdater.updateTextSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateRequiredProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasHeadingLevel,
                label = newSection.headingSize.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
    }

    @Test
    fun `Given a text section, when text has changed, it updates the literal`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createDummyTextSection()
        val newSection = oldSection.toTextSectionDefinition().copy(text = "new text contents")
        val statements = oldSection.toGroupedStatements()

        every {
            singleStatementPropertyUpdater.updateRequiredProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasContent,
                label = newSection.text
            )
        } just runs

        abstractLiteratureListSectionUpdater.updateTextSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateRequiredProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasContent,
                label = newSection.text
            )
        }
    }
}
