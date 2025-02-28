package org.orkg.contenttypes.domain.actions.smartreviews

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReviewComparisonSection
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReviewOntologySection
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReviewPredicateSection
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReviewResourceSection
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReviewTextSection
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReviewVisualizationSection
import org.orkg.contenttypes.domain.testing.fixtures.toGroupedStatements
import org.orkg.contenttypes.input.testing.fixtures.toSmartReviewComparisonSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.toSmartReviewOntologySectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.toSmartReviewPredicateSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.toSmartReviewResourceSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.toSmartReviewTextSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.toSmartReviewVisualizationSectionDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase
import java.util.UUID

internal class AbstractSmartReviewSectionUpdaterUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater = mockk()
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()

    private val abstractSmartReviewSectionUpdater = AbstractSmartReviewSectionUpdater(
        unsafeResourceUseCases,
        singleStatementPropertyUpdater,
        statementCollectionPropertyUpdater
    )

    @Test
    fun `Given a comparison section, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewComparisonSection()
        val newSection = oldSection.toSmartReviewComparisonSectionDefinition()
        val statements = oldSection.toGroupedStatements()

        abstractSmartReviewSectionUpdater.updateComparisonSection(contributorId, newSection, oldSection, statements)
    }

    @Test
    fun `Given a comparison section, when heading has changed, it updates the section resource`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewComparisonSection()
        val newSection = oldSection.toSmartReviewComparisonSectionDefinition().copy(heading = "new heading")
        val statements = oldSection.toGroupedStatements()

        every { unsafeResourceUseCases.update(any()) } just runs

        abstractSmartReviewSectionUpdater.updateComparisonSection(contributorId, newSection, oldSection, statements)

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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["R12346"])
    fun `Given a comparison section, when comparison id has changed, it updates the hasLink statement`(id: String?) {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewComparisonSection()
        val newSection = oldSection.toSmartReviewComparisonSectionDefinition().copy(comparison = id?.let(::ThingId))
        val statements = oldSection.toGroupedStatements()

        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasLink,
                objectId = newSection.comparison
            )
        } just runs

        abstractSmartReviewSectionUpdater.updateComparisonSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasLink,
                objectId = newSection.comparison
            )
        }
    }

    @Test
    fun `Given a visualization section, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewVisualizationSection()
        val newSection = oldSection.toSmartReviewVisualizationSectionDefinition()
        val statements = oldSection.toGroupedStatements()

        abstractSmartReviewSectionUpdater.updateVisualizationSection(contributorId, newSection, oldSection, statements)
    }

    @Test
    fun `Given a visualization section, when heading has changed, it updates the section resource`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewVisualizationSection()
        val newSection = oldSection.toSmartReviewVisualizationSectionDefinition().copy(heading = "new heading")
        val statements = oldSection.toGroupedStatements()

        every { unsafeResourceUseCases.update(any()) } just runs

        abstractSmartReviewSectionUpdater.updateVisualizationSection(contributorId, newSection, oldSection, statements)

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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["R12346"])
    fun `Given a visualization section, when visualization id has changed, it updates the hasLink statement`(id: String?) {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewVisualizationSection()
        val newSection = oldSection.toSmartReviewVisualizationSectionDefinition().copy(visualization = id?.let(::ThingId))
        val statements = oldSection.toGroupedStatements()

        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasLink,
                objectId = newSection.visualization
            )
        } just runs

        abstractSmartReviewSectionUpdater.updateVisualizationSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasLink,
                objectId = newSection.visualization
            )
        }
    }

    @Test
    fun `Given a resource section, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewResourceSection()
        val newSection = oldSection.toSmartReviewResourceSectionDefinition()
        val statements = oldSection.toGroupedStatements()

        abstractSmartReviewSectionUpdater.updateResourceSection(contributorId, newSection, oldSection, statements)
    }

    @Test
    fun `Given a resource section, when heading has changed, it updates the section resource`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewResourceSection()
        val newSection = oldSection.toSmartReviewResourceSectionDefinition().copy(heading = "new heading")
        val statements = oldSection.toGroupedStatements()

        every { unsafeResourceUseCases.update(any()) } just runs

        abstractSmartReviewSectionUpdater.updateResourceSection(contributorId, newSection, oldSection, statements)

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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["R12346"])
    fun `Given a resource section, when resource id has changed, it updates the hasLink statement`(id: String?) {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewResourceSection()
        val newSection = oldSection.toSmartReviewResourceSectionDefinition().copy(resource = id?.let(::ThingId))
        val statements = oldSection.toGroupedStatements()

        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasLink,
                objectId = newSection.resource
            )
        } just runs

        abstractSmartReviewSectionUpdater.updateResourceSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasLink,
                objectId = newSection.resource
            )
        }
    }

    @Test
    fun `Given a predicate section, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewPredicateSection()
        val newSection = oldSection.toSmartReviewPredicateSectionDefinition()
        val statements = oldSection.toGroupedStatements()

        abstractSmartReviewSectionUpdater.updatePredicateSection(contributorId, newSection, oldSection, statements)
    }

    @Test
    fun `Given a predicate section, when heading has changed, it updates the section resource`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewPredicateSection()
        val newSection = oldSection.toSmartReviewPredicateSectionDefinition().copy(heading = "new heading")
        val statements = oldSection.toGroupedStatements()

        every { unsafeResourceUseCases.update(any()) } just runs

        abstractSmartReviewSectionUpdater.updatePredicateSection(contributorId, newSection, oldSection, statements)

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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["R12346"])
    fun `Given a predicate section, when predicate id has changed, it updates the hasLink statement`(id: String?) {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewPredicateSection()
        val newSection = oldSection.toSmartReviewPredicateSectionDefinition().copy(predicate = id?.let(::ThingId))
        val statements = oldSection.toGroupedStatements()

        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasLink,
                objectId = newSection.predicate
            )
        } just runs

        abstractSmartReviewSectionUpdater.updatePredicateSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasLink,
                objectId = newSection.predicate
            )
        }
    }

    @Test
    fun `Given an ontology section, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewOntologySection()
        val newSection = oldSection.toSmartReviewOntologySectionDefinition()
        val statements = oldSection.toGroupedStatements()

        abstractSmartReviewSectionUpdater.updateOntologySection(contributorId, newSection, oldSection, statements)
    }

    @Test
    fun `Given an ontology section, when heading has changed, it updates the section resource`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewOntologySection()
        val newSection = oldSection.toSmartReviewOntologySectionDefinition().copy(heading = "new heading")
        val statements = oldSection.toGroupedStatements()

        every { unsafeResourceUseCases.update(any()) } just runs

        abstractSmartReviewSectionUpdater.updateOntologySection(contributorId, newSection, oldSection, statements)

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
    fun `Given an ontology section, when entity ids have changed, it updates the entity statements`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewOntologySection()
        val newSection = oldSection.toSmartReviewOntologySectionDefinition().copy(
            entities = listOf(ThingId("different"))
        )
        val statements = oldSection.toGroupedStatements()

        every {
            statementCollectionPropertyUpdater.update(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasEntity,
                objects = newSection.entities
            )
        } just runs

        abstractSmartReviewSectionUpdater.updateOntologySection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasEntity,
                objects = newSection.entities
            )
        }
    }

    @Test
    fun `Given an ontology section, when predicate ids have changed, it updates the predicate statements`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewOntologySection()
        val newSection = oldSection.toSmartReviewOntologySectionDefinition().copy(
            predicates = listOf(ThingId("different"))
        )
        val statements = oldSection.toGroupedStatements()

        every {
            statementCollectionPropertyUpdater.update(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.showProperty,
                objects = newSection.predicates
            )
        } just runs

        abstractSmartReviewSectionUpdater.updateOntologySection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.showProperty,
                objects = newSection.predicates
            )
        }
    }

    @Test
    fun `Given a text section, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewTextSection()
        val newSection = oldSection.toSmartReviewTextSectionDefinition()
        val statements = oldSection.toGroupedStatements()

        abstractSmartReviewSectionUpdater.updateTextSection(contributorId, newSection, oldSection, statements)
    }

    @Test
    fun `Given a text section, when heading has changed, it updates the section resource`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewTextSection()
        val newSection = oldSection.toSmartReviewTextSectionDefinition().copy(heading = "new heading")
        val statements = oldSection.toGroupedStatements()

        every { unsafeResourceUseCases.update(any()) } just runs

        abstractSmartReviewSectionUpdater.updateTextSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    contributorId = contributorId,
                    label = newSection.heading,
                    classes = oldSection.classes + Classes.section
                )
            )
        }
    }

    @Test
    fun `Given a text section, when class has changed, it updates the section resource`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewTextSection()
        val newSection = oldSection.toSmartReviewTextSectionDefinition().copy(`class` = Classes.epilogue)
        val statements = oldSection.toGroupedStatements()

        every { unsafeResourceUseCases.update(any()) } just runs

        abstractSmartReviewSectionUpdater.updateTextSection(contributorId, newSection, oldSection, statements)

        verify(exactly = 1) {
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    contributorId = contributorId,
                    label = oldSection.heading,
                    classes = setOfNotNull(newSection.`class`, Classes.section)
                )
            )
        }
    }

    @Test
    fun `Given a text section, when text has changed, it updates the text content literal`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldSection = createSmartReviewTextSection()
        val newSection = oldSection.toSmartReviewTextSectionDefinition().copy(text = "new and different text")
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

        abstractSmartReviewSectionUpdater.updateTextSection(contributorId, newSection, oldSection, statements)

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
