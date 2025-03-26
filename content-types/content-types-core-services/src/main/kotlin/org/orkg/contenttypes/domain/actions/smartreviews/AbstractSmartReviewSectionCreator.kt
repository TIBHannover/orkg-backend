package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyCreator
import org.orkg.contenttypes.input.AbstractSmartReviewComparisonSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewOntologySectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewPredicateSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewResourceSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewTextSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewVisualizationSectionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class AbstractSmartReviewSectionCreator(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val statementCollectionPropertyCreator: StatementCollectionPropertyCreator = StatementCollectionPropertyCreator(
        unsafeLiteralUseCases,
        unsafeStatementUseCases
    ),
) {
    internal fun create(
        contributorId: ContributorId,
        section: AbstractSmartReviewSectionCommand,
    ): ThingId =
        when (section) {
            is AbstractSmartReviewComparisonSectionCommand -> createComparisonSection(contributorId, section)
            is AbstractSmartReviewVisualizationSectionCommand -> createVisualizationSection(contributorId, section)
            is AbstractSmartReviewResourceSectionCommand -> createResourceSection(contributorId, section)
            is AbstractSmartReviewPredicateSectionCommand -> createPredicateSection(contributorId, section)
            is AbstractSmartReviewOntologySectionCommand -> createOntologySection(contributorId, section)
            is AbstractSmartReviewTextSectionCommand -> createTextSection(contributorId, section)
        }

    private fun createComparisonSection(
        contributorId: ContributorId,
        section: AbstractSmartReviewComparisonSectionCommand,
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.heading,
                classes = setOf(Classes.comparisonSection)
            )
        )
        section.comparison?.also { comparisonId ->
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = comparisonId
                )
            )
        }
        return sectionId
    }

    private fun createVisualizationSection(
        contributorId: ContributorId,
        section: AbstractSmartReviewVisualizationSectionCommand,
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.heading,
                classes = setOf(Classes.visualizationSection)
            )
        )
        section.visualization?.also { visualizationId ->
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = visualizationId
                )
            )
        }
        return sectionId
    }

    private fun createResourceSection(
        contributorId: ContributorId,
        section: AbstractSmartReviewResourceSectionCommand,
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.heading,
                classes = setOf(Classes.resourceSection)
            )
        )
        section.resource?.also { resourceId ->
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = resourceId
                )
            )
        }
        return sectionId
    }

    private fun createPredicateSection(
        contributorId: ContributorId,
        section: AbstractSmartReviewPredicateSectionCommand,
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.heading,
                classes = setOf(Classes.propertySection)
            )
        )
        section.predicate?.also { predicateId ->
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = predicateId
                )
            )
        }
        return sectionId
    }

    private fun createOntologySection(
        contributorId: ContributorId,
        section: AbstractSmartReviewOntologySectionCommand,
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.heading,
                classes = setOf(Classes.ontologySection)
            )
        )
        statementCollectionPropertyCreator.create(
            contributorId = contributorId,
            subjectId = sectionId,
            predicateId = Predicates.hasEntity,
            objects = section.entities
        )
        statementCollectionPropertyCreator.create(
            contributorId = contributorId,
            subjectId = sectionId,
            predicateId = Predicates.showProperty,
            objects = section.predicates
        )
        return sectionId
    }

    private fun createTextSection(
        contributorId: ContributorId,
        section: AbstractSmartReviewTextSectionCommand,
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.heading,
                classes = setOfNotNull(Classes.section, section.`class`)
            )
        )
        val textId = unsafeLiteralUseCases.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.text
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = sectionId,
                predicateId = Predicates.hasContent,
                objectId = textId
            )
        )
        return sectionId
    }
}
