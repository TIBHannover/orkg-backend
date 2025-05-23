package org.orkg.contenttypes.domain.actions.smartreviews

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyCreator
import org.orkg.contenttypes.input.testing.fixtures.smartReviewComparisonSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.smartReviewOntologySectionCommand
import org.orkg.contenttypes.input.testing.fixtures.smartReviewPredicateSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.smartReviewResourceSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.smartReviewTextSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.smartReviewVisualizationSectionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import java.util.UUID

internal class AbstractSmartReviewSectionCreatorUnitTest : MockkBaseTest {
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val statementCollectionPropertyCreator: StatementCollectionPropertyCreator = mockk()

    private val abstractSmartReviewSectionCreator = AbstractSmartReviewSectionCreator(
        unsafeStatementUseCases,
        unsafeResourceUseCases,
        unsafeLiteralUseCases,
        statementCollectionPropertyCreator
    )

    @Test
    fun `Given a comparison section command, when creating a comparison section with a linked comparison, it returns success`() {
        val section = smartReviewComparisonSectionCommand()
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = section.heading,
            classes = setOf(Classes.comparisonSection)
        )
        val sectionId = ThingId("R156465")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns sectionId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = section.comparison!!
                )
            )
        } returns StatementId("S1")

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = section.comparison!!
                )
            )
        }
    }

    @Test
    fun `Given a comparison section command, when creating a comparison section without a linked comparison, it returns success`() {
        val section = smartReviewComparisonSectionCommand().copy(comparison = null)
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = section.heading,
            classes = setOf(Classes.comparisonSection)
        )
        val sectionId = ThingId("R156465")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns sectionId

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
    }

    @Test
    fun `Given a visualization section command, when creating a visualization section with a linked visualization, it returns success`() {
        val section = smartReviewVisualizationSectionCommand()
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = section.heading,
            classes = setOf(Classes.visualizationSection)
        )
        val sectionId = ThingId("R156465")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns sectionId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = section.visualization!!
                )
            )
        } returns StatementId("S1")

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = section.visualization!!
                )
            )
        }
    }

    @Test
    fun `Given a visualization section command, when creating a visualization section without a linked visualization, it returns success`() {
        val section = smartReviewVisualizationSectionCommand().copy(visualization = null)
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = section.heading,
            classes = setOf(Classes.visualizationSection)
        )
        val sectionId = ThingId("R156465")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns sectionId

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
    }

    @Test
    fun `Given a resource section command, when creating a resource section with a linked resource, it returns success`() {
        val section = smartReviewResourceSectionCommand()
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = section.heading,
            classes = setOf(Classes.resourceSection)
        )
        val sectionId = ThingId("R156465")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns sectionId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = section.resource!!
                )
            )
        } returns StatementId("S1")

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = section.resource!!
                )
            )
        }
    }

    @Test
    fun `Given a resource section command, when creating a resource section without a linked resource, it returns success`() {
        val section = smartReviewResourceSectionCommand().copy(resource = null)
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = section.heading,
            classes = setOf(Classes.resourceSection)
        )
        val sectionId = ThingId("R156465")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns sectionId

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
    }

    @Test
    fun `Given a predicate section command, when creating a predicate section with a linked predicate, it returns success`() {
        val section = smartReviewPredicateSectionCommand()
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = section.heading,
            classes = setOf(Classes.propertySection)
        )
        val sectionId = ThingId("R156465")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns sectionId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = section.predicate!!
                )
            )
        } returns StatementId("S1")

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = section.predicate!!
                )
            )
        }
    }

    @Test
    fun `Given a predicate section command, when creating a predicate section without a linked predicate, it returns success`() {
        val section = smartReviewPredicateSectionCommand().copy(predicate = null)
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = section.heading,
            classes = setOf(Classes.propertySection)
        )
        val sectionId = ThingId("R156465")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns sectionId

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
    }

    @Test
    fun `Given an ontology section command, when creating, it returns success`() {
        val section = smartReviewOntologySectionCommand()
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = section.heading,
            classes = setOf(Classes.ontologySection)
        )
        val sectionId = ThingId("R156465")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns sectionId
        every {
            statementCollectionPropertyCreator.create(
                contributorId = contributorId,
                subjectId = sectionId,
                predicateId = Predicates.hasEntity,
                objects = section.entities
            )
        } just runs
        every {
            statementCollectionPropertyCreator.create(
                contributorId = contributorId,
                subjectId = sectionId,
                predicateId = Predicates.showProperty,
                objects = section.predicates
            )
        } just runs

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
        verify(exactly = 1) {
            statementCollectionPropertyCreator.create(
                contributorId = contributorId,
                subjectId = sectionId,
                predicateId = Predicates.hasEntity,
                objects = section.entities
            )
        }
        verify(exactly = 1) {
            statementCollectionPropertyCreator.create(
                contributorId = contributorId,
                subjectId = sectionId,
                predicateId = Predicates.showProperty,
                objects = section.predicates
            )
        }
    }

    @Test
    fun `Given a text section command, when creating, it returns success`() {
        val section = smartReviewTextSectionCommand()
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = contributorId,
            label = section.heading,
            classes = setOfNotNull(Classes.section, section.`class`)
        )
        val sectionId = ThingId("R156465")
        val literalCreateCommand = CreateLiteralUseCase.CreateCommand(
            contributorId = contributorId,
            label = section.text
        )
        val textId = ThingId("L123")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns sectionId
        every { unsafeLiteralUseCases.create(literalCreateCommand) } returns textId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasContent,
                    objectId = textId
                )
            )
        } returns StatementId("S1")

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
        verify(exactly = 1) { unsafeLiteralUseCases.create(literalCreateCommand) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasContent,
                    objectId = textId
                )
            )
        }
    }
}
