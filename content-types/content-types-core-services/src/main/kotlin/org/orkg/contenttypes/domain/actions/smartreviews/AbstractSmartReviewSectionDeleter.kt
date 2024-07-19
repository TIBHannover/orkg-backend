package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.SmartReviewComparisonSection
import org.orkg.contenttypes.domain.SmartReviewOntologySection
import org.orkg.contenttypes.domain.SmartReviewPredicateSection
import org.orkg.contenttypes.domain.SmartReviewResourceSection
import org.orkg.contenttypes.domain.SmartReviewSection
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.SmartReviewVisualizationSection
import org.orkg.contenttypes.domain.actions.ContentTypePartDeleter
import org.orkg.contenttypes.domain.actions.tryDelete
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class AbstractSmartReviewSectionDeleter(
    private val statementService: StatementUseCases,
    private val resourceService: ResourceUseCases,
    private val contentTypePartDeleter: ContentTypePartDeleter
) {
    constructor(
        statementService: StatementUseCases,
        resourceService: ResourceUseCases
    ) : this(statementService, resourceService, ContentTypePartDeleter(statementService))

    internal fun delete(
        contributorId: ContributorId,
        contributionId: ThingId,
        section: SmartReviewSection,
        statements: Map<ThingId, List<GeneralStatement>>
    ) = when (section) {
        is SmartReviewComparisonSection -> deleteSingleNodeSection(contributorId, contributionId, section)
        is SmartReviewVisualizationSection -> deleteSingleNodeSection(contributorId, contributionId, section)
        is SmartReviewResourceSection -> deleteSingleNodeSection(contributorId, contributionId, section)
        is SmartReviewPredicateSection -> deleteSingleNodeSection(contributorId, contributionId, section)
        is SmartReviewOntologySection -> deleteSingleNodeSection(contributorId, contributionId, section)
        is SmartReviewTextSection -> deleteTextSection(contributorId, contributionId, section, statements)
    }

    private fun deleteSingleNodeSection(
        contributorId: ContributorId,
        contributionId: ThingId,
        section: SmartReviewSection
    ) {
        contentTypePartDeleter.delete(contributionId, section.id) { incomingStatements ->
            if (incomingStatements.isNotEmpty()) {
                statementService.delete(incomingStatements.map { it.id }.toSet())
            }
            resourceService.tryDelete(section.id, contributorId)
        }
    }

    private fun deleteTextSection(
        contributorId: ContributorId,
        contributionId: ThingId,
        section: SmartReviewTextSection,
        statements: Map<ThingId, List<GeneralStatement>>
    ) = contentTypePartDeleter.delete(contributionId, section.id) { incomingStatements ->
        val toRemove = statements[section.id]?.map { it.id }.orEmpty() union
            incomingStatements.filter { it.`object` is Literal }.map { it.id }
        if (toRemove.isNotEmpty()) {
            statementService.delete(toRemove)
        }
        resourceService.tryDelete(section.id, contributorId)
    }
}
