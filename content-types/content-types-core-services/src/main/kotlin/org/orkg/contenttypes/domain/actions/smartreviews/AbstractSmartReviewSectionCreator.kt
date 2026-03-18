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
import org.orkg.graph.domain.ExtractionMethod
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
        unsafeStatementUseCases,
    ),
) {
    internal fun create(
        contributorId: ContributorId,
        section: AbstractSmartReviewSectionCommand,
        extractionMethod: ExtractionMethod,
    ): ThingId =
        when (section) {
            is AbstractSmartReviewComparisonSectionCommand -> createComparisonSection(contributorId, section, extractionMethod)
            is AbstractSmartReviewVisualizationSectionCommand -> createVisualizationSection(contributorId, section, extractionMethod)
            is AbstractSmartReviewResourceSectionCommand -> createResourceSection(contributorId, section, extractionMethod)
            is AbstractSmartReviewPredicateSectionCommand -> createPredicateSection(contributorId, section, extractionMethod)
            is AbstractSmartReviewOntologySectionCommand -> createOntologySection(contributorId, section, extractionMethod)
            is AbstractSmartReviewTextSectionCommand -> createTextSection(contributorId, section, extractionMethod)
        }

    private fun createComparisonSection(
        contributorId: ContributorId,
        section: AbstractSmartReviewComparisonSectionCommand,
        extractionMethod: ExtractionMethod,
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.heading,
                classes = setOf(Classes.comparisonSection),
                extractionMethod = extractionMethod,
            ),
        )
        section.comparison?.also { comparisonId ->
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = comparisonId,
                    extractionMethod = extractionMethod,
                ),
            )
        }
        return sectionId
    }

    private fun createVisualizationSection(
        contributorId: ContributorId,
        section: AbstractSmartReviewVisualizationSectionCommand,
        extractionMethod: ExtractionMethod,
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.heading,
                classes = setOf(Classes.visualizationSection),
                extractionMethod = extractionMethod,
            ),
        )
        section.visualization?.also { visualizationId ->
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = visualizationId,
                    extractionMethod = extractionMethod,
                ),
            )
        }
        return sectionId
    }

    private fun createResourceSection(
        contributorId: ContributorId,
        section: AbstractSmartReviewResourceSectionCommand,
        extractionMethod: ExtractionMethod,
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.heading,
                classes = setOf(Classes.resourceSection),
                extractionMethod = extractionMethod,
            ),
        )
        section.resource?.also { resourceId ->
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = resourceId,
                    extractionMethod = extractionMethod,
                ),
            )
        }
        return sectionId
    }

    private fun createPredicateSection(
        contributorId: ContributorId,
        section: AbstractSmartReviewPredicateSectionCommand,
        extractionMethod: ExtractionMethod,
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.heading,
                classes = setOf(Classes.propertySection),
                extractionMethod = extractionMethod,
            ),
        )
        section.predicate?.also { predicateId ->
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasLink,
                    objectId = predicateId,
                    extractionMethod = extractionMethod,
                ),
            )
        }
        return sectionId
    }

    private fun createOntologySection(
        contributorId: ContributorId,
        section: AbstractSmartReviewOntologySectionCommand,
        extractionMethod: ExtractionMethod,
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.heading,
                classes = setOf(Classes.ontologySection),
                extractionMethod = extractionMethod,
            ),
        )
        statementCollectionPropertyCreator.create(
            contributorId = contributorId,
            subjectId = sectionId,
            predicateId = Predicates.hasEntity,
            objects = section.entities,
            extractionMethod = extractionMethod,
        )
        statementCollectionPropertyCreator.create(
            contributorId = contributorId,
            subjectId = sectionId,
            predicateId = Predicates.showProperty,
            objects = section.predicates,
            extractionMethod = extractionMethod,
        )
        return sectionId
    }

    private fun createTextSection(
        contributorId: ContributorId,
        section: AbstractSmartReviewTextSectionCommand,
        extractionMethod: ExtractionMethod,
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.heading,
                classes = setOfNotNull(Classes.section, section.`class`),
                extractionMethod = extractionMethod,
            ),
        )
        val textId = unsafeLiteralUseCases.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.text,
                extractionMethod = extractionMethod,
            ),
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = sectionId,
                predicateId = Predicates.hasContent,
                objectId = textId,
                extractionMethod = extractionMethod,
            ),
        )
        return sectionId
    }
}
