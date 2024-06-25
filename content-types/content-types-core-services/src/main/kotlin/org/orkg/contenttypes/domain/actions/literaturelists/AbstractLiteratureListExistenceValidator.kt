package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.LiteratureList
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.domain.LiteratureListNotModifiable
import org.orkg.contenttypes.domain.LiteratureListService
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.output.ResourceRepository

class AbstractLiteratureListExistenceValidator(
    private val literatureListService: LiteratureListService,
    private val resourceRepository: ResourceRepository
) {
    internal fun findUnpublishedLiteratureListById(id: ThingId): Pair<LiteratureList, Map<ThingId, List<GeneralStatement>>> {
        val resource = resourceRepository.findById(id)
            .filter {
                if (Classes.literatureListPublished in it.classes) {
                    throw LiteratureListNotModifiable(id)
                }
                Classes.literatureList in it.classes
            }
            .orElseThrow { LiteratureListNotFound(id) }
        val subgraph = literatureListService.findSubgraph(resource)
        val literatureList = LiteratureList.from(resource, subgraph.root, subgraph.statements)
        return literatureList to subgraph.statements
    }
}
