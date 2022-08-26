package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchFieldsQuery
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.api.PagedResourcesGenerator
import eu.tib.orkg.prototype.statements.api.ResourceGenerator
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldUseCase
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResearchFieldRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val ResearchField = ClassId("ResearchField")

@Service
@Transactional
class ResearchFieldService(
    private val neo4jResearchFieldRepository: Neo4jResearchFieldRepository,
    private val researchFieldsQuery: FindResearchFieldsQuery,
    private val userRepository: UserRepository,
    private val resourceService: ResourceUseCases,
) : RetrieveResearchFieldUseCase {

    override fun findById(id: ResourceId): Optional<ResourceRepresentation> =
        Optional.ofNullable(resourceService.findByIdAndClasses(id, setOf(ResearchField)))

    override fun getResearchProblemsOfField(
        id: ResourceId,
        pageable: Pageable
    ): Page<RetrieveResearchFieldUseCase.PaperCountPerResearchProblem> {
        return neo4jResearchFieldRepository.getResearchProblemsOfField(id, pageable).map {
            RetrieveResearchFieldUseCase.PaperCountPerResearchProblem(
                problem = resourceService.map(ResourceGenerator { it.problem.toResource() }),
                papers = it.papers,
            )
        }
    }

    override fun getResearchProblemsIncludingSubFields(
        id: ResourceId,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        val modifiedFeatured: Boolean =
            setFeatured(unlisted, featured) ?: return resourceService.map(PagedResourcesGenerator {
                neo4jResearchFieldRepository.getProblemsIncludingSubFields(
                    id = id, pageable = pageable
                ).map(Neo4jResource::toResource)
            })

        return resourceService.map(PagedResourcesGenerator {
            neo4jResearchFieldRepository.getProblemsIncludingSubFieldsWithFlags(
                id = id, featured = modifiedFeatured, unlisted = unlisted, pageable = pageable
            ).map(Neo4jResource::toResource)
        })
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
    ): Page<ResourceRepresentation> {
        val modifiedFeatured: Boolean =
            setFeatured(unlisted, featured) ?: return resourceService.map(PagedResourcesGenerator {
                neo4jResearchFieldRepository.getPapersIncludingSubFields(
                    id = id, pageable = pageable
                ).map(Neo4jResource::toResource)
            })
        return resourceService.map(PagedResourcesGenerator {
            neo4jResearchFieldRepository.getPapersIncludingSubFieldsWithFlags(
                id = id, featured = modifiedFeatured, unlisted = unlisted, pageable = pageable
            ).map(Neo4jResource::toResource)
        })
    }

    override fun getComparisonsIncludingSubFields(
        id: ResourceId,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        val modifiedFeatured: Boolean =
            setFeatured(unlisted, featured) ?: return resourceService.map(PagedResourcesGenerator {
                neo4jResearchFieldRepository.getComparisonsIncludingSubFields(
                    id = id, pageable = pageable
                ).map(Neo4jResource::toResource)
            })
        return resourceService.map(PagedResourcesGenerator {
            neo4jResearchFieldRepository.getComparisonsIncludingSubFieldsWithFlags(
                id = id, featured = modifiedFeatured, unlisted = unlisted, pageable = pageable
            ).map(Neo4jResource::toResource)
        })
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
    ): Page<ResourceRepresentation> {
        val modifiedFeatured: Boolean =
            setFeatured(unlisted, featured) ?: return resourceService.map(PagedResourcesGenerator {
                neo4jResearchFieldRepository.getPapersExcludingSubFields(
                    id = id, pageable = pageable
                ).map(Neo4jResource::toResource)
            })
        return resourceService.map(PagedResourcesGenerator {
            neo4jResearchFieldRepository.getPapersExcludingSubFieldsWithFlags(
                id = id, featured = modifiedFeatured, unlisted = unlisted, pageable = pageable
            ).map(Neo4jResource::toResource)
        })
    }

    override fun getComparisonsExcludingSubFields(
        id: ResourceId,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        val modifiedFeatured: Boolean =
            setFeatured(unlisted, featured) ?: return resourceService.map(PagedResourcesGenerator {
                neo4jResearchFieldRepository.getComparisonsExcludingSubFields(
                    id = id, pageable = pageable
                ).map(Neo4jResource::toResource)
            })
        return resourceService.map(PagedResourcesGenerator {
            neo4jResearchFieldRepository.getComparisonsExcludingSubFieldsWithFlags(
                id = id, featured = modifiedFeatured, unlisted = unlisted, pageable = pageable
            ).map(Neo4jResource::toResource)
        })
    }

    override fun getResearchProblemsExcludingSubFields(
        id: ResourceId,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        val modifiedFeatured: Boolean =
            setFeatured(unlisted, featured) ?: return resourceService.map(PagedResourcesGenerator {
                neo4jResearchFieldRepository.getProblemsExcludingSubFields(
                    id = id, pageable = pageable
                ).map(Neo4jResource::toResource)
            })
        return resourceService.map(PagedResourcesGenerator {
            neo4jResearchFieldRepository.getProblemsExcludingSubFieldsWithFlags(
                id = id, featured = modifiedFeatured, unlisted = unlisted, pageable = pageable
            ).map(Neo4jResource::toResource)
        })
    }

    /**
     * We are checking for classes named SmartReviewPublished and LiteratureListPublished.
     * Please check with frontend team before modifying this function
     */
    override fun getEntitiesBasedOnClassesIncludingSubfields(
        id: ResourceId,
        classesList: List<String>,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        val resultList = mutableListOf<Neo4jResource>()

        when (featured) {
            null -> getListIncludingSubFieldsWithoutFeaturedFlag(classesList, id, unlisted, pageable, resultList)
            else -> getListIncludingSubFieldsWithFlags(classesList, id, featured, unlisted, pageable, resultList)
        }

        resultList.sortWith { o1, o2 -> o2.createdAt!!.compareTo(o1.createdAt) }

        return resourceService.map(PagedResourcesGenerator {
            PageImpl(resultList.map(Neo4jResource::toResource), pageable, resultList.size.toLong())
        })
    }

    /**
     * We are checking for classes named SmartReviewPublished and LiteratureListPublished.
     * Please check with frontend team before modifying this function
     */
    override fun getEntitiesBasedOnClassesExcludingSubfields(
        id: ResourceId,
        classesList: List<String>,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        val resultList = mutableListOf<Neo4jResource>()

        when (featured) {
            null -> getListExcludingSubFieldsWithoutFeaturedFlag(classesList, id, unlisted, pageable, resultList)
            else -> getListExcludingSubFieldsWithFlags(classesList, id, featured, unlisted, pageable, resultList)
        }

        resultList.sortBy(Neo4jResource::createdAt)

        return resourceService.map(PagedResourcesGenerator {
            PageImpl(resultList.map(Neo4jResource::toResource), pageable, resultList.size.toLong())
        })
    }

    override fun withBenchmarks(): List<ResearchField> = researchFieldsQuery.withBenchmarks()

    private fun setFeatured(unlisted: Boolean, featured: Boolean?): Boolean? =
        when (unlisted) {
            true -> false
            false -> featured
        }

    private fun getListIncludingSubFieldsWithoutFeaturedFlag(
        classesList: List<String>,
        id: ResourceId,
        unlisted: Boolean,
        pageable: Pageable,
        resultList: MutableList<Neo4jResource>
    ) {
        classesList.map { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> resultList.addAll(neo4jResearchFieldRepository.getPapersIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable).content)
                "COMPARISON" -> resultList.addAll(neo4jResearchFieldRepository.getComparisonsIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable).content)
                "VISUALIZATION" -> resultList.addAll(neo4jResearchFieldRepository.getVisualizationsIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable).content)
                "LITERATURELISTPUBLISHED" -> resultList.addAll(neo4jResearchFieldRepository.getLiteratureListIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable).content)
                "PROBLEM" -> resultList.addAll(neo4jResearchFieldRepository.getProblemsIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable).content)
                else -> {
                    resultList.addAll(neo4jResearchFieldRepository.getSmartReviewsIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable).content)
                }
            }
        }
    }

    private fun getListIncludingSubFieldsWithFlags(
        classesList: List<String>,
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable,
        resultList: MutableList<Neo4jResource>
    ) {
        classesList.map { classType ->
            when (classType.uppercase(Locale.getDefault())) {
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
    }

    private fun getListExcludingSubFieldsWithoutFeaturedFlag(
        classesList: List<String>,
        id: ResourceId,
        unlisted: Boolean,
        pageable: Pageable,
        resultList: MutableList<Neo4jResource>
    ) {
        classesList.map { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> resultList.addAll(neo4jResearchFieldRepository.getPapersExcludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable).content)
                "COMPARISON" -> resultList.addAll(neo4jResearchFieldRepository.getComparisonsExcludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable).content)
                "VISUALIZATION" -> resultList.addAll(neo4jResearchFieldRepository.getVisualizationsExcludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable).content)
                "LITERATURELISTPUBLISHED" -> resultList.addAll(neo4jResearchFieldRepository.getLiteratureListExcludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable).content)
                "PROBLEM" -> resultList.addAll(neo4jResearchFieldRepository.getProblemsExcludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable).content)
                else -> {
                    resultList.addAll(neo4jResearchFieldRepository.getSmartReviewsIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable).content)
                }
            }
        }
    }

    private fun getListExcludingSubFieldsWithFlags(
        classesList: List<String>,
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable,
        resultList: MutableList<Neo4jResource>
    ) {
        classesList.map { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> resultList.addAll(neo4jResearchFieldRepository.getPapersExcludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                "COMPARISON" -> resultList.addAll(neo4jResearchFieldRepository.getComparisonsExcludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                "VISUALIZATION" -> resultList.addAll(neo4jResearchFieldRepository.getVisualizationsExcludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                "LITERATURELISTPUBLISHED" -> resultList.addAll(neo4jResearchFieldRepository.getLiteratureListExcludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                "PROBLEM" -> resultList.addAll(neo4jResearchFieldRepository.getProblemsExcludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                else -> {
                    resultList.addAll(neo4jResearchFieldRepository.getSmartReviewsExcludingSubFieldsWithFlags(id, featured, unlisted, pageable).content)
                }
            }
        }
    }
}
