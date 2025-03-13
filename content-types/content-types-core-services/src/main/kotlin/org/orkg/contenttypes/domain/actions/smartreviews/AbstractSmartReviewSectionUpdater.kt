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
import org.orkg.contenttypes.input.AbstractSmartReviewComparisonSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewOntologySectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewPredicateSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewResourceSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewTextSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewVisualizationSectionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class AbstractSmartReviewSectionUpdater(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater,
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater,
) {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        unsafeResourceUseCases,
        SingleStatementPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases),
        StatementCollectionPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases)
    )

    internal fun updateComparisonSection(
        contributorId: ContributorId,
        newSection: AbstractSmartReviewComparisonSectionCommand,
        oldSection: SmartReviewComparisonSection,
        statements: Map<ThingId, List<GeneralStatement>>,
    ) {
        if (newSection.heading != oldSection.heading) {
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    contributorId = contributorId,
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
        newSection: AbstractSmartReviewVisualizationSectionCommand,
        oldSection: SmartReviewVisualizationSection,
        statements: Map<ThingId, List<GeneralStatement>>,
    ) {
        if (newSection.heading != oldSection.heading) {
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    contributorId = contributorId,
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
        newSection: AbstractSmartReviewResourceSectionCommand,
        oldSection: SmartReviewResourceSection,
        statements: Map<ThingId, List<GeneralStatement>>,
    ) {
        if (newSection.heading != oldSection.heading) {
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    contributorId = contributorId,
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
        newSection: AbstractSmartReviewPredicateSectionCommand,
        oldSection: SmartReviewPredicateSection,
        statements: Map<ThingId, List<GeneralStatement>>,
    ) {
        if (newSection.heading != oldSection.heading) {
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    contributorId = contributorId,
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
        newSection: AbstractSmartReviewOntologySectionCommand,
        oldSection: SmartReviewOntologySection,
        statements: Map<ThingId, List<GeneralStatement>>,
    ) {
        if (newSection.heading != oldSection.heading) {
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    contributorId = contributorId,
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
        newSection: AbstractSmartReviewTextSectionCommand,
        oldSection: SmartReviewTextSection,
        statements: Map<ThingId, List<GeneralStatement>>,
    ) {
        if (newSection.heading != oldSection.heading || newSection.`class` !in oldSection.classes) {
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    contributorId = contributorId,
                    label = newSection.heading,
                    classes = oldSection.classes.filterNot { it in SmartReviewTextSection.types } union setOfNotNull(newSection.`class`, Classes.section)
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
