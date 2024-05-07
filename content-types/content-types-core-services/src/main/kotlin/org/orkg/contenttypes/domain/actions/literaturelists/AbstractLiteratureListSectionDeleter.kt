package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ListSection
import org.orkg.contenttypes.domain.LiteratureListSection
import org.orkg.contenttypes.domain.TextSection
import org.orkg.contenttypes.domain.actions.ContentTypePartDeleter
import org.orkg.contenttypes.domain.actions.tryDelete
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class AbstractLiteratureListSectionDeleter(
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
        literatureListId: ThingId,
        section: LiteratureListSection,
        statements: Map<ThingId, List<GeneralStatement>>
    ) = when (section) {
        is ListSection -> deleteListSection(contributorId, literatureListId, section, statements)
        is TextSection -> deleteTextSection(contributorId, literatureListId, section, statements)
    }

    private fun deleteListSection(
        contributorId: ContributorId,
        literatureListId: ThingId,
        section: ListSection,
        statements: Map<ThingId, List<GeneralStatement>>
    ) = contentTypePartDeleter.delete(literatureListId, section.id) {
        val entryNodes = statements[section.id]?.map { it.`object`.id }.orEmpty()
        val toRemove = statements[section.id]?.map { it.id }.orEmpty() + entryNodes.flatMap { node -> statements[node]?.map { it.id }.orEmpty() }
        if (toRemove.isNotEmpty()) {
            statementService.delete(toRemove.toSet())
        }
        entryNodes.forEach { resourceService.tryDelete(it, contributorId) }
        resourceService.tryDelete(section.id, contributorId)
    }

    private fun deleteTextSection(
        contributorId: ContributorId,
        literatureListId: ThingId,
        section: TextSection,
        statements: Map<ThingId, List<GeneralStatement>>
    ) = contentTypePartDeleter.delete(literatureListId, section.id) {
        val toRemove = statements[section.id]?.map { it.id }.orEmpty()
        if (toRemove.isNotEmpty()) {
            statementService.delete(toRemove.toSet())
        }
        resourceService.tryDelete(section.id, contributorId)
    }
}
