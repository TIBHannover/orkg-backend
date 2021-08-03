package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.paperswithcode.application.port.input.RetrieveResearchFieldUseCase
import eu.tib.orkg.prototype.statements.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.domain.model.ResearchFieldService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResearchFieldRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import java.util.Optional
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
) : ResearchFieldService, RetrieveResearchFieldUseCase {
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

    override fun getResearchProblemsIncludingSubFields(
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> {
            return neo4jResearchFieldRepository.getProblemsIncludingSubFieldsWithFlags(
                id = id,
                featured = featured,
                unlisted = unlisted,
                pageable = pageable)
                .map(Neo4jResource::toResource)
    }

    override fun getContributorsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor> {
        val contributors = neo4jResearchFieldRepository.getContributorIdsFromResearchFieldAndIncludeSubfields(id, pageable).map(ContributorId::value)
        return PageImpl(userRepository.findByIdIn(contributors.content.toTypedArray()).map(UserEntity::toContributor))
    }

    override fun getPapersIncludingSubFields(
        id: ResourceId,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        if (featured === null) {
            return neo4jResearchFieldRepository.getPapersIncludingSubFields(
                id = id, pageable = pageable)
                .map(Neo4jResource::toResource)
        }
        return neo4jResearchFieldRepository.getPapersIncludingSubFieldsWithFlags(
            id = id,
            featured = featured,
            unlisted = unlisted,
            pageable = pageable)
            .map(Neo4jResource::toResource)
    }

    override fun getComparisonsIncludingSubFields(
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> {
            return neo4jResearchFieldRepository.getComparisonsIncludingSubFieldsWithFlags(
                id = id,
                featured = featured,
                unlisted = unlisted,
                pageable = pageable)
                .map(Neo4jResource::toResource)
    }

    override fun getContributorsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor> {
        val contributors = neo4jResearchFieldRepository.getContributorIdsExcludingSubFields(id, pageable).map(ContributorId::value)
        return PageImpl(userRepository.findByIdIn(contributors.content.toTypedArray()).map(UserEntity::toContributor))
    }

    override fun getPapersExcludingSubFields(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable):
        Page<Resource> {
            return neo4jResearchFieldRepository.getPapersExcludingSubFieldsWithFlags(
                id = id,
                featured = featured,
                unlisted = unlisted,
                pageable = pageable)
                .map(Neo4jResource::toResource)
    }

    override fun getComparisonsExcludingSubFields(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable):
        Page<Resource> {
            return neo4jResearchFieldRepository.getComparisonsExcludingSubFieldsWithFlags(
                id = id,
                featured = featured,
                unlisted = unlisted,
                pageable = pageable)
                .map(Neo4jResource::toResource)
    }

    override fun getResearchProblemsExcludingSubFields(
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> {
            return neo4jResearchFieldRepository.getProblemsExcludingSubFieldsWithFlags(
                id = id,
                featured = featured,
                unlisted = unlisted,
                pageable = pageable).map(Neo4jResource::toResource)
    }

    override fun withBenchmarks(): List<ResearchField> =
        neo4jResearchFieldRepository.findResearchFieldsWithBenchmarks()
            .map { ResearchField(it.resourceId!!.value, it.label!!) }
}
