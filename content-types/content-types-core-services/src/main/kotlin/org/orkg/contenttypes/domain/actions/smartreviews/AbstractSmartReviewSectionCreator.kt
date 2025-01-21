package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyCreator
import org.orkg.contenttypes.input.SmartReviewComparisonSectionDefinition
import org.orkg.contenttypes.input.SmartReviewOntologySectionDefinition
import org.orkg.contenttypes.input.SmartReviewPredicateSectionDefinition
import org.orkg.contenttypes.input.SmartReviewResourceSectionDefinition
import org.orkg.contenttypes.input.SmartReviewSectionDefinition
import org.orkg.contenttypes.input.SmartReviewTextSectionDefinition
import org.orkg.contenttypes.input.SmartReviewVisualizationSectionDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases

class AbstractSmartReviewSectionCreator(
    private val statementService: StatementUseCases,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val literalService: LiteralUseCases,
    private val statementCollectionPropertyCreator: StatementCollectionPropertyCreator = StatementCollectionPropertyCreator(literalService, statementService)
) {
    internal fun create(
        contributorId: ContributorId,
        section: SmartReviewSectionDefinition
    ): ThingId =
        when (section) {
            is SmartReviewComparisonSectionDefinition -> createComparisonSection(contributorId, section)
            is SmartReviewVisualizationSectionDefinition -> createVisualizationSection(contributorId, section)
            is SmartReviewResourceSectionDefinition -> createResourceSection(contributorId, section)
            is SmartReviewPredicateSectionDefinition -> createPredicateSection(contributorId, section)
            is SmartReviewOntologySectionDefinition -> createOntologySection(contributorId, section)
            is SmartReviewTextSectionDefinition -> createTextSection(contributorId, section)
        }

    private fun createComparisonSection(
        contributorId: ContributorId,
        section: SmartReviewComparisonSectionDefinition
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                label = section.heading,
                classes = setOf(Classes.comparisonSection),
                contributorId = contributorId
            )
        )
        section.comparison?.let { comparisonId ->
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasLink,
                `object` = comparisonId
            )
        }
        return sectionId
    }

    private fun createVisualizationSection(
        contributorId: ContributorId,
        section: SmartReviewVisualizationSectionDefinition
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                label = section.heading,
                classes = setOf(Classes.visualizationSection),
                contributorId = contributorId
            )
        )
        section.visualization?.let { visualizationId ->
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasLink,
                `object` = visualizationId
            )
        }
        return sectionId
    }

    private fun createResourceSection(
        contributorId: ContributorId,
        section: SmartReviewResourceSectionDefinition
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                label = section.heading,
                classes = setOf(Classes.resourceSection),
                contributorId = contributorId
            )
        )
        section.resource?.let { resourceId ->
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasLink,
                `object` = resourceId
            )
        }
        return sectionId
    }

    private fun createPredicateSection(
        contributorId: ContributorId,
        section: SmartReviewPredicateSectionDefinition
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                label = section.heading,
                classes = setOf(Classes.propertySection),
                contributorId = contributorId
            )
        )
        section.predicate?.let { predicateId ->
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasLink,
                `object` = predicateId
            )
        }
        return sectionId
    }

    private fun createOntologySection(
        contributorId: ContributorId,
        section: SmartReviewOntologySectionDefinition
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                label = section.heading,
                classes = setOf(Classes.ontologySection),
                contributorId = contributorId
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
        section: SmartReviewTextSectionDefinition
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                label = section.heading,
                classes = setOfNotNull(Classes.section, section.`class`),
                contributorId = contributorId
            )
        )
        val textId = literalService.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.text
            )
        )
        statementService.add(
            userId = contributorId,
            subject = sectionId,
            predicate = Predicates.hasContent,
            `object` = textId
        )
        return sectionId
    }
}
