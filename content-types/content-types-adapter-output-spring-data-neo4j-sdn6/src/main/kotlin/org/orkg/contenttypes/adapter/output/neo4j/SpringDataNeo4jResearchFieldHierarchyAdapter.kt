package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jResearchFieldHierarchyRepository
import org.orkg.contenttypes.domain.ResearchFieldHierarchyEntry
import org.orkg.contenttypes.domain.ResearchFieldWithChildCount
import org.orkg.contenttypes.output.ResearchFieldHierarchyRepository
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.orkg.graph.domain.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jResearchFieldHierarchyAdapter(
    private val neo4jRepository: Neo4jResearchFieldHierarchyRepository
) : ResearchFieldHierarchyRepository {
    override fun findChildren(id: ThingId, pageable: Pageable): Page<ResearchFieldWithChildCount> =
        neo4jRepository.findChildren(id, pageable)
            .map { ResearchFieldWithChildCount(it.resource.toResource(), it.childCount) }

    override fun findParents(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findParents(id, pageable).map(Neo4jResource::toResource)

    override fun findRoots(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findRoots(id, pageable).map(Neo4jResource::toResource)

    override fun findAllRoots(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllRoots(pageable).map(Neo4jResource::toResource)

    override fun findResearchFieldHierarchy(id: ThingId, pageable: Pageable): Page<ResearchFieldHierarchyEntry> =
        neo4jRepository.findResearchFieldHierarchy(id, pageable)
            .map {
                ResearchFieldHierarchyEntry(
                    it.resource.toResource(),
                    it.parentIds.mapTo(mutableSetOf(), ::ThingId)
                )
            }
}
