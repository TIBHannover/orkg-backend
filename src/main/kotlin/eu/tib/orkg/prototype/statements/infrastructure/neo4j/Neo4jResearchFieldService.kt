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
        pageable: Pageable
    ): Page<Resource> {
        if(featured){
            return neo4jResearchFieldRepository.getProblemsIncludingSubFieldsAndFeatured(
                id = id,
                pageable = pageable)
                .map(Neo4jResource::toResource)
        }

        return neo4jResearchFieldRepository.getProblemsIncludingSubFieldsAndNonFeatured(
            id = id,
            pageable = pageable)
            .map(Neo4jResource::toResource)
    }


    override fun getResearchProblemsIncludingSubFields(
        id: ResourceId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jResearchFieldRepository.getProblemsIncludingSubFields(
            id = id,
            pageable = pageable)
            .map(Neo4jResource::toResource)

    override fun getContributorsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor> {
        val contributors = neo4jResearchFieldRepository.getContributorIdsFromResearchFieldAndIncludeSubfields(id, pageable).map(ContributorId::value)
        return PageImpl(userRepository.findByIdIn(contributors.content.toTypedArray()).map(UserEntity::toContributor))
    }

    override fun getPapersIncludingSubFields(
        id: ResourceId,
        featured: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        if(featured){
            return neo4jResearchFieldRepository
                .getPapersIncludingSubFieldsAndFeatured(
                id = id,
                pageable = pageable).map(Neo4jResource::toResource)
        }
            return neo4jResearchFieldRepository
                    .getPapersIncludingSubFieldsAndNonFeatured(
                id = id,
                pageable = pageable
            ).map(Neo4jResource::toResource)
    }

    override fun getPapersIncludingSubFields(
        id: ResourceId,
        pageable: Pageable
    ): Page<Resource> =
        neo4jResearchFieldRepository.getPapersIncludingSubFields(
            id = id,
            pageable = pageable)
            .map(Neo4jResource::toResource)

    override fun getComparisonsIncludingSubFields(id: ResourceId, pageable: Pageable):
        Page<Resource> = neo4jResearchFieldRepository.getComparisonsIncludingSubFields(
        id = id,
        pageable = pageable)
        .map(Neo4jResource::toResource)

    override fun getComparisonsIncludingSubFields(
        id: ResourceId,
        featured: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        if(featured){
            return neo4jResearchFieldRepository.getComparisonsIncludingSubFieldsAndFeatured(
                id = id,
                pageable = pageable)
                .map(Neo4jResource::toResource)
        }

        return neo4jResearchFieldRepository.getComparisonsIncludingSubFieldsAndNonFeatured(
            id = id,
            pageable = pageable)
            .map(Neo4jResource::toResource)

    }


    override fun getContributorsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor> {
        val contributors = neo4jResearchFieldRepository.getContributorIdsExcludingSubFields(id, pageable).map(ContributorId::value)
        return PageImpl(userRepository.findByIdIn(contributors.content.toTypedArray()).map(UserEntity::toContributor))
    }

    override fun getPapersExcludingSubFields(
        id: ResourceId,
        featured: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        if(featured){
            return neo4jResearchFieldRepository.getPapersExcludingSubFieldsAndFeatured(
                id = id,
                pageable = pageable)
                .map(Neo4jResource::toResource)
        }
        return neo4jResearchFieldRepository.getPapersExcludingSubFieldsAndNonFeatured(
            id = id,
            pageable = pageable)
            .map(Neo4jResource::toResource)
    }


    override fun getPapersExcludingSubFields(id: ResourceId, pageable: Pageable):
        Page<Resource> =
        neo4jResearchFieldRepository.getPapersExcludingSubFields(id = id, pageable = pageable).map(Neo4jResource::toResource)

    override fun getComparisonsExcludingSubFields(id: ResourceId, featured: Boolean, pageable: Pageable):
        Page<Resource> {
        if(featured){
            return neo4jResearchFieldRepository.getComparisonsExcludingSubFieldsAndFeatured(
                id = id,
                pageable = pageable)
                .map(Neo4jResource::toResource)
        }
        return neo4jResearchFieldRepository.getComparisonsExcludingSubFieldsAndNonFeatured(
            id = id,
            pageable = pageable)
            .map(Neo4jResource::toResource)
    }

    override fun getComparisonsExcludingSubFields(id: ResourceId, pageable: Pageable):
        Page<Resource> = neo4jResearchFieldRepository.getComparisonsExcludingSubFields(
        id = id,
        pageable = pageable)
        .map(Neo4jResource::toResource)

    override fun getResearchProblemsExcludingSubFields(
        id: ResourceId,
        featured: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        if(featured){
            return neo4jResearchFieldRepository.getProblemsExcludingSubFieldsAndFeatured(
                id = id,
                pageable = pageable).map(Neo4jResource::toResource)
        }

        return neo4jResearchFieldRepository.getProblemsExcludingSubFieldsAndNonFeatured(
            id = id,
            pageable = pageable).map(Neo4jResource::toResource)
    }


    override fun getResearchProblemsExcludingSubFields(id: ResourceId, pageable: Pageable):
        Page<Resource> =
        neo4jResearchFieldRepository.getProblemsExcludingSubFields(
            id = id,
            pageable = pageable)
            .map(Neo4jResource::toResource)

    override fun withBenchmarks(): List<ResearchField> =
        neo4jResearchFieldRepository.findResearchFieldsWithBenchmarks()
            .map { ResearchField(it.resourceId!!.value, it.label!!) }
}
