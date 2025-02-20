package org.orkg.contenttypes.domain.actions.literaturelists

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureListListSection
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureListTextSection
import org.orkg.contenttypes.domain.testing.fixtures.toGroupedStatements
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.contenttypes.input.LiteratureListListSectionCommand
import org.orkg.contenttypes.input.LiteratureListListSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.toDefinitionEntry
import org.orkg.contenttypes.input.testing.fixtures.toLiteratureListListSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.toLiteratureListTextSectionDefinition
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase
import java.util.UUID

internal class AbstractLiteratureListSectionUpdaterUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val abstractLiteratureListSectionCreator: AbstractLiteratureListSectionCreator = mockk()
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater = mockk()

    private val abstractLiteratureListSectionUpdater = AbstractLiteratureListSectionUpdater(
        statementService = statementService,
        unsafeStatementUseCases = unsafeStatementUseCases,
        resourceService = resourceService,
        unsafeResourceUseCases = unsafeResourceUseCases,
        abstractLiteratureListSectionCreator = abstractLiteratureListSectionCreator,
        singleStatementPropertyUpdater = singleStatementPropertyUpdater
    )

    @Test
    fun `Given a list section, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createLiteratureListListSection()
        val newSection = oldSection.toLiteratureListListSectionDefinition()
        val statements = oldSection.toGroupedStatements()

        abstractLiteratureListSectionUpdater.updateListSection(contributorId, newSection, oldSection, statements)
    }

    @Test
    fun `Given a list section, when an entry has been deleted at the end of the list, it deletes the entry`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createLiteratureListListSection()
        val newSection = oldSection.copy(entries = oldSection.entries.take(1)).toLiteratureListListSectionDefinition()
        val statements = oldSection.toGroupedStatements()

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } just runs

        abstractLiteratureListSectionUpdater.updateListSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S1"), StatementId("S1_2"))) }
        verify(exactly = 1) { resourceService.delete(ThingId("R1"), contributorId) }
    }

    @Test
    fun `Given a list section, when an entry has been deleted at the beginning of the list, it deletes the entry`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createLiteratureListListSection()
        val newSection = oldSection.copy(entries = oldSection.entries.drop(1)).toLiteratureListListSectionDefinition()
        val statements = oldSection.toGroupedStatements()

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(any(), contributorId) } just runs

        abstractLiteratureListSectionUpdater.updateListSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S0"), StatementId("S0_2"), StatementId("S0_3"))) }
        verify(exactly = 1) { resourceService.delete(ThingId("R0"), contributorId) }
    }

    @Test
    fun `Given a list section, when an entry has been added to the beginning of the list, it creates a new entry node and reassigns existing entry nodes`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createLiteratureListListSection()
        val newSection = oldSection.toLiteratureListListSectionDefinition().copy(
            entries = listOf(LiteratureListListSectionDefinition.Entry(ThingId("R789"))) + oldSection.entries.map { it.toDefinitionEntry() }
        )
        val statements = oldSection.toGroupedStatements()
        val entryId = ThingId("R1564")

        every { abstractLiteratureListSectionCreator.createListSectionEntry(contributorId, any()) } returns entryId
        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")
        every { singleStatementPropertyUpdater.updateOptionalProperty(any<List<GeneralStatement>>(), any(), any(), any(), any<String>()) } just runs
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs

        abstractLiteratureListSectionUpdater.updateListSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = ThingId("R0"),
                    predicateId = Predicates.hasLink,
                    objectId = ThingId("R789")
                )
            )
        }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements[ThingId("R0")]!!.wherePredicate(Predicates.description),
                contributorId = contributorId,
                subjectId = ThingId("R0"),
                predicateId = Predicates.description,
                label = null
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = ThingId("R1"),
                    predicateId = Predicates.hasLink,
                    objectId = ThingId("R154686")
                )
            )
        }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements[ThingId("R1")]!!.wherePredicate(Predicates.description),
                contributorId = contributorId,
                subjectId = ThingId("R1"),
                predicateId = Predicates.description,
                label = "paper entry description"
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S0_2"), StatementId("S1_2"))) }
        verify(exactly = 1) {
            abstractLiteratureListSectionCreator.createListSectionEntry(
                contributorId = contributorId,
                entry = oldSection.entries.last().toDefinitionEntry()
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = oldSection.id,
                    predicateId = Predicates.hasEntry,
                    objectId = entryId
                )
            )
        }
    }

    @Test
    fun `Given a list section, when an entry has been added to the end of the list, it creates new a section entry`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createLiteratureListListSection()
        val newEntry = LiteratureListListSectionDefinition.Entry(ThingId("R789"))
        val newSection = oldSection.toLiteratureListListSectionDefinition().copy(
            entries = oldSection.entries.map { it.toDefinitionEntry() } + newEntry
        )
        val statements = oldSection.toGroupedStatements()
        val entryId = ThingId("R1564")

        every { abstractLiteratureListSectionCreator.createListSectionEntry(contributorId, any()) } returns entryId
        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")

        abstractLiteratureListSectionUpdater.updateListSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            abstractLiteratureListSectionCreator.createListSectionEntry(contributorId, newEntry)
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = oldSection.id,
                    predicateId = Predicates.hasEntry,
                    objectId = entryId
                )
            )
        }
    }

    @Test
    fun `Given a list section, when an entry has been inserted to the list, it reuses the existing entry node and creates a new entry node for each entry after the new one`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createLiteratureListListSection()
        val entry = oldSection.entries.last().toDefinitionEntry()
        val newSection = LiteratureListListSectionCommand(
            entries = listOf(
                oldSection.entries.first().toDefinitionEntry(),
                LiteratureListListSectionDefinition.Entry(ThingId("new")),
                entry
            )
        )
        val statements = oldSection.toGroupedStatements()
        val entryId = ThingId("R1564")

        every { abstractLiteratureListSectionCreator.createListSectionEntry(contributorId, any()) } returns entryId
        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")
        every { singleStatementPropertyUpdater.updateOptionalProperty(any<List<GeneralStatement>>(), any(), any(), any(), any<String>()) } just runs
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs

        abstractLiteratureListSectionUpdater.updateListSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = ThingId("R1"),
                    predicateId = Predicates.hasLink,
                    objectId = ThingId("new")
                )
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S1_2"))) }
        verify(exactly = 1) {
            abstractLiteratureListSectionCreator.createListSectionEntry(contributorId, entry)
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = oldSection.id,
                    predicateId = Predicates.hasEntry,
                    objectId = entryId
                )
            )
        }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = emptyList(),
                contributorId = contributorId,
                subjectId = ThingId("R1"),
                predicateId = Predicates.description,
                label = null
            )
        }
    }

    @Test
    fun `Given a list section, when description of an entry changes, it only updates the description`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createLiteratureListListSection()
        val newSection = LiteratureListListSectionCommand(
            entries = listOf(
                oldSection.entries.first().toDefinitionEntry(),
                oldSection.entries.last().toDefinitionEntry().copy(description = "updated description")
            )
        )
        val statements = oldSection.toGroupedStatements()

        every { singleStatementPropertyUpdater.updateOptionalProperty(any<List<GeneralStatement>>(), any(), any(), any(), any<String>()) } just runs

        abstractLiteratureListSectionUpdater.updateListSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = emptyList(),
                contributorId = contributorId,
                subjectId = ThingId("R1"),
                predicateId = Predicates.description,
                label = "updated description"
            )
        }
    }

    @Test
    fun `Given a text section, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createLiteratureListTextSection()
        val newSection = oldSection.toLiteratureListTextSectionDefinition()
        val statements = oldSection.toGroupedStatements()

        abstractLiteratureListSectionUpdater.updateTextSection(contributorId, newSection, oldSection, statements)
    }

    @Test
    fun `Given a text section, when heading has changed, it updates the resource`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createLiteratureListTextSection()
        val newSection = oldSection.toLiteratureListTextSectionDefinition().copy(heading = "new heading")
        val statements = oldSection.toGroupedStatements()

        every { unsafeResourceUseCases.update(any()) } just runs

        abstractLiteratureListSectionUpdater.updateTextSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    contributorId = contributorId,
                    label = newSection.heading
                )
            )
        }
    }

    @Test
    fun `Given a text section, when heading size has changed, it updates the literal`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createLiteratureListTextSection()
        val newSection = oldSection.toLiteratureListTextSectionDefinition().copy(headingSize = 5)
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
        val oldSection = createLiteratureListTextSection()
        val newSection = oldSection.toLiteratureListTextSectionDefinition().copy(text = "new text contents")
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
