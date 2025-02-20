package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.LiteratureListListSection
import org.orkg.contenttypes.domain.LiteratureListSection
import org.orkg.contenttypes.domain.LiteratureListTextSection
import org.orkg.contenttypes.domain.actions.ContentTypePartDeleter
import org.orkg.contenttypes.domain.actions.tryDelete
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class AbstractLiteratureListSectionDeleter(
    private val statementService: StatementUseCases,
    private val resourceService: ResourceUseCases,
    private val contentTypePartDeleter: ContentTypePartDeleter,
) {
    constructor(
        statementService: StatementUseCases,
        resourceService: ResourceUseCases,
    ) : this(statementService, resourceService, ContentTypePartDeleter(statementService))

    internal fun delete(
        contributorId: ContributorId,
        literatureListId: ThingId,
        section: LiteratureListSection,
        statements: Map<ThingId, List<GeneralStatement>>,
    ) = when (section) {
        is LiteratureListListSection -> deleteListSection(contributorId, literatureListId, section, statements)
        is LiteratureListTextSection -> deleteTextSection(contributorId, literatureListId, section, statements)
    }

    private fun deleteListSection(
        contributorId: ContributorId,
        literatureListId: ThingId,
        section: LiteratureListListSection,
        statements: Map<ThingId, List<GeneralStatement>>,
    ) {
        contentTypePartDeleter.delete(literatureListId, section.id) { incomingStatements ->
            val entryNodes = statements[section.id]?.map { it.`object`.id }.orEmpty()
            val toRemove = statements[section.id]?.map { it.id }.orEmpty() +
                entryNodes.flatMap { node -> statements[node]?.map { it.id }.orEmpty() } +
                incomingStatements.map { it.id }
            if (toRemove.isNotEmpty()) {
                statementService.deleteAllById(toRemove.toSet())
            }
            entryNodes.forEach { resourceService.tryDelete(it, contributorId) }
            resourceService.tryDelete(section.id, contributorId)
        }
    }

    private fun deleteTextSection(
        contributorId: ContributorId,
        literatureListId: ThingId,
        section: LiteratureListTextSection,
        statements: Map<ThingId, List<GeneralStatement>>,
    ) {
        contentTypePartDeleter.delete(literatureListId, section.id) { incomingStatements ->
            val toRemove = statements[section.id]?.map { it.id }.orEmpty() + incomingStatements.map { it.id }
            if (toRemove.isNotEmpty()) {
                statementService.deleteAllById(toRemove.toSet())
            }
            resourceService.tryDelete(section.id, contributorId)
        }
    }
}
