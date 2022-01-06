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
import java.util.Collections
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
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        val modifiedFeatured: Boolean = setFeatured(unlisted, featured)
            ?: return neo4jResearchFieldRepository.getProblemsIncludingSubFields(
                id = id,
                pageable = pageable)
                .map(Neo4jResource::toResource)

        return neo4jResearchFieldRepository.getProblemsIncludingSubFieldsWithFlags(
            id = id,
            featured = modifiedFeatured,
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
        val modifiedFeatured: Boolean = setFeatured(unlisted, featured)
            ?: return neo4jResearchFieldRepository.getPapersIncludingSubFields(
                id = id, pageable = pageable).map(Neo4jResource::toResource)

        return neo4jResearchFieldRepository.getPapersIncludingSubFieldsWithFlags(
            id = id,
            featured = modifiedFeatured,
            unlisted = unlisted,
            pageable = pageable).map(Neo4jResource::toResource)
    }

    override fun getComparisonsIncludingSubFields(
        id: ResourceId,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        val modifiedFeatured: Boolean = setFeatured(unlisted, featured)
            ?: return neo4jResearchFieldRepository.getComparisonsIncludingSubFields(
                id = id,
                pageable = pageable)
                .map(Neo4jResource::toResource)

        return neo4jResearchFieldRepository.getComparisonsIncludingSubFieldsWithFlags(
            id = id,
            featured = modifiedFeatured,
            unlisted = unlisted,
            pageable = pageable)
            .map(Neo4jResource::toResource)
    }

    override fun getContributorsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor> {
        val contributors = neo4jResearchFieldRepository.getContributorIdsExcludingSubFields(id, pageable).map(ContributorId::value)
        return PageImpl(userRepository.findByIdIn(contributors.content.toTypedArray()).map(UserEntity::toContributor))
    }

    override fun getPapersExcludingSubFields(
        id: ResourceId,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        val modifiedFeatured: Boolean = setFeatured(unlisted, featured)
            ?: return neo4jResearchFieldRepository.getPapersExcludingSubFields(
                id = id,
                pageable = pageable)
                .map(Neo4jResource::toResource)

        return neo4jResearchFieldRepository.getPapersExcludingSubFieldsWithFlags(
            id = id,
            featured = modifiedFeatured,
            unlisted = unlisted,
            pageable = pageable)
            .map(Neo4jResource::toResource)
    }

    override fun getComparisonsExcludingSubFields(id: ResourceId, featured: Boolean?, unlisted: Boolean, pageable: Pageable):
        Page<Resource> {
        val modifiedFeatured: Boolean = setFeatured(unlisted, featured)
            ?: return neo4jResearchFieldRepository.getComparisonsExcludingSubFields(
                id = id,
                pageable = pageable)
                .map(Neo4jResource::toResource)

        return neo4jResearchFieldRepository.getComparisonsExcludingSubFieldsWithFlags(
            id = id,
            featured = modifiedFeatured,
            unlisted = unlisted,
            pageable = pageable)
            .map(Neo4jResource::toResource)
    }

    override fun getResearchProblemsExcludingSubFields(
        id: ResourceId,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        val modifiedFeatured: Boolean = setFeatured(unlisted, featured)
            ?: return neo4jResearchFieldRepository.getProblemsExcludingSubFields(
                id = id,
                pageable = pageable).map(Neo4jResource::toResource)

        return neo4jResearchFieldRepository.getProblemsExcludingSubFieldsWithFlags(
            id = id,
            featured = modifiedFeatured,
            unlisted = unlisted,
            pageable = pageable).map(Neo4jResource::toResource)
    }

    /**
     * We are checking for classes named SmartReviewPublished and LiteratureListPublished.
     * Please check with frontend team before modifying this function
     */
    override fun getEntitiesBasedOnClassesIncludingSubfields(
        id: ResourceId,
        classesList: List<String>,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        var resultList = mutableListOf<Neo4jResource>()
        classesList.map {
            when (it.toUpperCase()) {
                "PAPER" -> resultList.addAll(neo4jResearchFieldRepository.getPapersIncludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                "COMPARISON" -> resultList.addAll(neo4jResearchFieldRepository.getComparisonsIncludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                "VISUALIZATION" -> resultList.addAll(neo4jResearchFieldRepository.getVisualizationsIncludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                "LITERATURELISTPUBLISHED" -> resultList.addAll(neo4jResearchFieldRepository.getLiteratureListIncludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                "PROBLEM" -> resultList.addAll(neo4jResearchFieldRepository.getProblemsIncludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                else -> {
                    resultList.addAll(neo4jResearchFieldRepository.getSmartReviewsIncludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                }
            }
        }

        Collections.sort(resultList as List<Neo4jResource>,
            { o1, o2 -> o2.createdAt!!.compareTo(o1.createdAt) })

        return PageImpl(resultList as List<Resource>, pageable, resultList.size.toLong())
    }

    /**
     * We are checking for classes named SmartReviewPublished and LiteratureListPublished.
     * Please check with frontend team before modifying this function
     */
    override fun getEntitiesBasedOnClassesExcludingSubfields(
        id: ResourceId,
        classesList: List<String>,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        var resultList = mutableListOf<Neo4jResource>()
        classesList.map {
            when (it.toUpperCase()) {
                "PAPER" -> resultList.addAll(neo4jResearchFieldRepository.getPapersExcludingSubFieldsWithFlags(id, featured, unlisted, pageable))
                "COMPARISON" -> resultList.addAll(neo4jResearchFieldRepository.getComparisonsExcludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                "VISUALIZATION" -> resultList.addAll(neo4jResearchFieldRepository.getVisualizationsExcludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                "LITERATURELISTPUBLISHED" -> resultList.addAll(neo4jResearchFieldRepository.getLiteratureListExcludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                "PROBLEM" -> resultList.addAll(neo4jResearchFieldRepository.getProblemsExcludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                else -> {
                    resultList.addAll(neo4jResearchFieldRepository.getSmartReviewsExcludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                }
            }
        }

        Collections.sort(resultList as List<Neo4jResource>,
            { o1, o2 -> o2.createdAt!!.compareTo(o1.createdAt) })

        return PageImpl(resultList as List<Resource>, pageable, resultList.size.toLong())
    }

    override fun withBenchmarks(): List<ResearchField> =
        neo4jResearchFieldRepository.findResearchFieldsWithBenchmarks()
            .map { ResearchField(it.resourceId!!.value, it.label!!) }

    private fun setFeatured(unlisted: Boolean, featured: Boolean?): Boolean? =
        when (unlisted) {
            true -> false
            false -> featured
        }
}
