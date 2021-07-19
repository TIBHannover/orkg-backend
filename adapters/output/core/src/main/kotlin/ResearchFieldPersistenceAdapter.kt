package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResearchFieldRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.ports.ResearchFieldRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional


@Component
class ResearchFieldPersistenceAdapter(
    private val neo4jResearchFieldRepository: Neo4jResearchFieldRepository,
): ResearchFieldRepository {
    override fun findById(id: ResourceId): Optional<Resource> =
        neo4jResearchFieldRepository
            .findById(id)
            .map(Neo4jResource::toResource)

    override fun getResearchProblemsOfField(id: ResourceId, pageable: Pageable): Page<Any> {
        return neo4jResearchFieldRepository.getResearchProblemsOfField(id, pageable)
            .map {
                object {
                    val problem = it.problem.toResource()
                    val papers = it.papers
                }
            }
    }

    override fun getResearchProblemsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jResearchFieldRepository.getProblemsIncludingSubFields(id, pageable).map(Neo4jResource::toResource)

    override fun getPapersIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jResearchFieldRepository.getPapersIncludingSubFields(id, pageable).map(Neo4jResource::toResource)

    override fun getComparisonsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jResearchFieldRepository.getComparisonsIncludingSubFields(id, pageable).map(Neo4jResource::toResource)

     override fun getPapersExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jResearchFieldRepository.getPapersExcludingSubFields(id, pageable).map(Neo4jResource::toResource)

    override fun getComparisonsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jResearchFieldRepository.getComparisonsExcludingSubFields(id, pageable).map(Neo4jResource::toResource)

    override fun getResearchProblemsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jResearchFieldRepository.getProblemsExcludingSubFields(id, pageable).map(Neo4jResource::toResource)

}
