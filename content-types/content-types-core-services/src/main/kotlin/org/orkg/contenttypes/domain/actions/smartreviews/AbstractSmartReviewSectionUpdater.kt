package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.SmartReviewComparisonSection
import org.orkg.contenttypes.domain.SmartReviewOntologySection
import org.orkg.contenttypes.domain.SmartReviewPredicateSection
import org.orkg.contenttypes.domain.SmartReviewResourceSection
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.SmartReviewVisualizationSection
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.input.SmartReviewComparisonSectionDefinition
import org.orkg.contenttypes.input.SmartReviewOntologySectionDefinition
import org.orkg.contenttypes.input.SmartReviewPredicateSectionDefinition
import org.orkg.contenttypes.input.SmartReviewResourceSectionDefinition
import org.orkg.contenttypes.input.SmartReviewTextSectionDefinition
import org.orkg.contenttypes.input.SmartReviewVisualizationSectionDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class AbstractSmartReviewSectionUpdater(
    private val resourceService: ResourceUseCases,
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater,
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater
) {
    constructor(
        literalService: LiteralUseCases,
        resourceService: ResourceUseCases,
        statementService: StatementUseCases,
    ) : this(
        resourceService,
        SingleStatementPropertyUpdater(literalService, statementService),
        StatementCollectionPropertyUpdater(literalService, statementService)
    )

    internal fun updateComparisonSection(
        contributorId: ContributorId,
        newSection: SmartReviewComparisonSectionDefinition,
        oldSection: SmartReviewComparisonSection,
        statements: Map<ThingId, List<GeneralStatement>>
    ) {
        if (newSection.heading != oldSection.heading) {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    label = newSection.heading
                )
            )
        }
        if (newSection.comparison != oldSection.comparison?.id) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasLink,
                objectId = newSection.comparison
            )
        }
    }

    internal fun updateVisualizationSection(
        contributorId: ContributorId,
        newSection: SmartReviewVisualizationSectionDefinition,
        oldSection: SmartReviewVisualizationSection,
        statements: Map<ThingId, List<GeneralStatement>>
    ) {
        if (newSection.heading != oldSection.heading) {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    label = newSection.heading
                )
            )
        }
        if (newSection.visualization != oldSection.visualization?.id) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasLink,
                objectId = newSection.visualization
            )
        }
    }

    internal fun updateResourceSection(
        contributorId: ContributorId,
        newSection: SmartReviewResourceSectionDefinition,
        oldSection: SmartReviewResourceSection,
        statements: Map<ThingId, List<GeneralStatement>>
    ) {
        if (newSection.heading != oldSection.heading) {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    label = newSection.heading
                )
            )
        }
        if (newSection.resource != oldSection.resource?.id) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasLink,
                objectId = newSection.resource
            )
        }
    }

    internal fun updatePredicateSection(
        contributorId: ContributorId,
        newSection: SmartReviewPredicateSectionDefinition,
        oldSection: SmartReviewPredicateSection,
        statements: Map<ThingId, List<GeneralStatement>>
    ) {
        if (newSection.heading != oldSection.heading) {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    label = newSection.heading
                )
            )
        }
        if (newSection.predicate != oldSection.predicate?.id) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasLink,
                objectId = newSection.predicate
            )
        }
    }

    internal fun updateOntologySection(
        contributorId: ContributorId,
        newSection: SmartReviewOntologySectionDefinition,
        oldSection: SmartReviewOntologySection,
        statements: Map<ThingId, List<GeneralStatement>>
    ) {
        if (newSection.heading != oldSection.heading) {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    label = newSection.heading
                )
            )
        }
        if (newSection.entities != oldSection.entities.map { it.id }) {
            statementCollectionPropertyUpdater.update(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasEntity,
                objects = newSection.entities
            )
        }
        if (newSection.predicates != oldSection.predicates.map { it.id }) {
            statementCollectionPropertyUpdater.update(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.showProperty,
                objects = newSection.predicates
            )
        }
    }

    internal fun updateTextSection(
        contributorId: ContributorId,
        newSection: SmartReviewTextSectionDefinition,
        oldSection: SmartReviewTextSection,
        statements: Map<ThingId, List<GeneralStatement>>
    ) {
        if (newSection.heading != oldSection.heading || newSection.`class` !in oldSection.classes) {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    label = newSection.heading,
                    classes = (oldSection.classes.filterNot { it in SmartReviewTextSection.types } + setOf(newSection.`class`, Classes.section)).toSet()
                )
            )
        }
        if (newSection.text != oldSection.text) {
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
