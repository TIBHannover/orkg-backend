package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResearchFieldHierarchyRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldHierarchyUseCase.ResearchFieldWithChildCount
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldHierarchyUseCase.ResearchFieldHierarchyEntry
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResearchFieldHierarchyRepository
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
