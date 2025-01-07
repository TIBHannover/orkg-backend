package org.orkg.contenttypes.domain.actions.smartreviews

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyCreator
import org.orkg.contenttypes.input.testing.fixtures.dummySmartReviewComparisonSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummySmartReviewOntologySectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummySmartReviewPredicateSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummySmartReviewResourceSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummySmartReviewTextSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummySmartReviewVisualizationSectionDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

internal class AbstractSmartReviewSectionCreatorUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val statementCollectionPropertyCreator: StatementCollectionPropertyCreator = mockk()

    private val abstractSmartReviewSectionCreator = AbstractSmartReviewSectionCreator(
        statementService, resourceService, literalService, statementCollectionPropertyCreator
    )

    @Test
    fun `Given a comparison section definition, when creating a comparison section with a linked comparison, it returns success`() {
        val section = dummySmartReviewComparisonSectionDefinition()
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = section.heading,
            classes = setOf(Classes.comparisonSection),
            contributorId = contributorId
        )
        val sectionId = ThingId("R156465")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns sectionId
        every {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasLink,
                `object` = section.comparison!!
            )
        } just runs

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasLink,
                `object` = section.comparison!!
            )
        }
    }

    @Test
    fun `Given a comparison section definition, when creating a comparison section without a linked comparison, it returns success`() {
        val section = dummySmartReviewComparisonSectionDefinition().copy(comparison = null)
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = section.heading,
            classes = setOf(Classes.comparisonSection),
            contributorId = contributorId
        )
        val sectionId = ThingId("R156465")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns sectionId

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
    }

    @Test
    fun `Given a visualization section definition, when creating a visualization section with a linked visualization, it returns success`() {
        val section = dummySmartReviewVisualizationSectionDefinition()
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = section.heading,
            classes = setOf(Classes.visualizationSection),
            contributorId = contributorId
        )
        val sectionId = ThingId("R156465")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns sectionId
        every {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasLink,
                `object` = section.visualization!!
            )
        } just runs

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasLink,
                `object` = section.visualization!!
            )
        }
    }

    @Test
    fun `Given a visualization section definition, when creating a visualization section without a linked visualization, it returns success`() {
        val section = dummySmartReviewVisualizationSectionDefinition().copy(visualization = null)
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = section.heading,
            classes = setOf(Classes.visualizationSection),
            contributorId = contributorId
        )
        val sectionId = ThingId("R156465")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns sectionId

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
    }

    @Test
    fun `Given a resource section definition, when creating a resource section with a linked resource, it returns success`() {
        val section = dummySmartReviewResourceSectionDefinition()
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = section.heading,
            classes = setOf(Classes.resourceSection),
            contributorId = contributorId
        )
        val sectionId = ThingId("R156465")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns sectionId
        every {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasLink,
                `object` = section.resource!!
            )
        } just runs

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasLink,
                `object` = section.resource!!
            )
        }
    }

    @Test
    fun `Given a resource section definition, when creating a resource section without a linked resource, it returns success`() {
        val section = dummySmartReviewResourceSectionDefinition().copy(resource = null)
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = section.heading,
            classes = setOf(Classes.resourceSection),
            contributorId = contributorId
        )
        val sectionId = ThingId("R156465")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns sectionId

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
    }

    @Test
    fun `Given a predicate section definition, when creating a predicate section with a linked predicate, it returns success`() {
        val section = dummySmartReviewPredicateSectionDefinition()
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = section.heading,
            classes = setOf(Classes.propertySection),
            contributorId = contributorId
        )
        val sectionId = ThingId("R156465")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns sectionId
        every {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasLink,
                `object` = section.predicate!!
            )
        } just runs

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasLink,
                `object` = section.predicate!!
            )
        }
    }

    @Test
    fun `Given a predicate section definition, when creating a predicate section without a linked predicate, it returns success`() {
        val section = dummySmartReviewPredicateSectionDefinition().copy(predicate = null)
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = section.heading,
            classes = setOf(Classes.propertySection),
            contributorId = contributorId
        )
        val sectionId = ThingId("R156465")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns sectionId

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
    }

    @Test
    fun `Given an ontology section definition, when creating, it returns success`() {
        val section = dummySmartReviewOntologySectionDefinition()
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = section.heading,
            classes = setOf(Classes.ontologySection),
            contributorId = contributorId
        )
        val sectionId = ThingId("R156465")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns sectionId
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

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
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
    fun `Given a text section definition, when creating, it returns success`() {
        val section = dummySmartReviewTextSectionDefinition()
        val contributorId = ContributorId(UUID.randomUUID())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = section.heading,
            classes = setOfNotNull(Classes.section, section.`class`),
            contributorId = contributorId
        )
        val sectionId = ThingId("R156465")
        val literalCreateCommand = CreateLiteralUseCase.CreateCommand(
            contributorId = contributorId,
            label = section.text
        )
        val textId = ThingId("L123")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns sectionId
        every { literalService.create(literalCreateCommand) } returns textId
        every {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasContent,
                `object` = textId
            )
        } just runs

        abstractSmartReviewSectionCreator.create(contributorId, section)

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
        verify(exactly = 1) { literalService.create(literalCreateCommand) }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasContent,
                `object` = textId
            )
        }
    }
}
