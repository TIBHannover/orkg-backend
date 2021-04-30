package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ResearchFieldService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResearchFieldRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jResearchFieldService(
    private val neo4jResearchFieldRepository: Neo4jResearchFieldRepository,
    private val userRepository: UserRepository
) : ResearchFieldService {

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

    override fun getContributorsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor> {
        val contributors = neo4jResearchFieldRepository.getContributorIdsFromResearchFieldAndIncludeSubfields(id, pageable).map(ContributorId::value)
        return PageImpl(userRepository.findByIdIn(contributors.content.toTypedArray()).map(UserEntity::toContributor))
    }

    override fun getPapersIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jResearchFieldRepository.getPapersIncludingSubFields(id, pageable).map(Neo4jResource::toResource)

    override fun getComparisonsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jResearchFieldRepository.getComparisonsIncludingSubFields(id, pageable).map(Neo4jResource::toResource)

    override fun getContributorsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor> {
        val contributors = neo4jResearchFieldRepository.getContributorIdsExcludingSubFields(id, pageable).map(ContributorId::value)
        return PageImpl(userRepository.findByIdIn(contributors.content.toTypedArray()).map(UserEntity::toContributor))
    }

    override fun getPapersExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jResearchFieldRepository.getPapersExcludingSubFields(id, pageable).map(Neo4jResource::toResource)

    override fun getComparisonsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jResearchFieldRepository.getComparisonsExcludingSubFields(id, pageable).map(Neo4jResource::toResource)

    override fun getResearchProblemsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jResearchFieldRepository.getProblemsExcludingSubFields(id, pageable).map(Neo4jResource::toResource)
}
