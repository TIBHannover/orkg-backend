package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.statements.domain.model.ResearchFieldService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResearchFieldRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

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

    override fun getResearchProblemsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource> =
        neo4jResearchFieldRepository.getProblemsIncludingSubFields(id, pageable)

    override fun getContributorsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor>{
        val userList = mutableListOf<UUID>()
        neo4jResearchFieldRepository.getContributorsFromResearchFieldAndIncludeSubfields(id, pageable).map {
            userList.add(it.value)
        }

        return userRepository.findByIdIn(userList.toTypedArray(), pageable).map(UserEntity::toContributor)
    }

    override fun getPapersIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource> =
        neo4jResearchFieldRepository.getListOfPapersIncludingSubFields(id, pageable)

    override fun getComparisonsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource> =
        neo4jResearchFieldRepository.getComparisonsIncludingSubFields(id, pageable)

    override fun getContributorsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor> {
        val userList = mutableListOf<UUID>()
        neo4jResearchFieldRepository.getListOfContributorsExcludingSubFields(id, pageable).map {
            userList.add(it.value)
        }

        return userRepository.findByIdIn(userList.toTypedArray(), pageable).map(UserEntity::toContributor)
    }

    override fun getPapersExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource> =
        neo4jResearchFieldRepository.getListOfPapersExcludingSubFields(id, pageable)

    override fun getComparisonsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource> =
        neo4jResearchFieldRepository.getListOfComparisonsExcludingSubFields(id, pageable)
}
