package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchFieldsQuery
import eu.tib.orkg.prototype.statements.api.PagedResourcesGenerator
import eu.tib.orkg.prototype.statements.api.ResourceGenerator
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldUseCase
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResearchFieldRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val ResearchField = ThingId("ResearchField")

@Service
@Transactional
class ResearchFieldService(
    private val researchFieldRepository: ResearchFieldRepository,
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
        return researchFieldRepository.getResearchProblemsOfField(id, pageable).map {
            RetrieveResearchFieldUseCase.PaperCountPerResearchProblem(
                problem = resourceService.map(ResourceGenerator { it.problem }),
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
                researchFieldRepository.getProblemsIncludingSubFields(
                    id = id, pageable = pageable
                )
            })

        return resourceService.map(PagedResourcesGenerator {
            researchFieldRepository.getProblemsIncludingSubFieldsWithFlags(
                id = id, featured = modifiedFeatured, unlisted = unlisted, pageable = pageable
            )
        })
    }

    override fun getContributorsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor> {
        val contributors = researchFieldRepository.getContributorIdsFromResearchFieldAndIncludeSubfields(id, pageable).map(ContributorId::value)
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
                researchFieldRepository.getPapersIncludingSubFields(
                    id = id, pageable = pageable
                )
            })
        return resourceService.map(PagedResourcesGenerator {
            researchFieldRepository.getPapersIncludingSubFieldsWithFlags(
                id = id, featured = modifiedFeatured, unlisted = unlisted, pageable = pageable
            )
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
                researchFieldRepository.getComparisonsIncludingSubFields(
                    id = id, pageable = pageable
                )
            })
        return resourceService.map(PagedResourcesGenerator {
            researchFieldRepository.getComparisonsIncludingSubFieldsWithFlags(
                id = id, featured = modifiedFeatured, unlisted = unlisted, pageable = pageable
            )
        })
    }

    override fun getContributorsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor> {
        val contributors = researchFieldRepository.getContributorIdsExcludingSubFields(id, pageable).map(ContributorId::value)
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
                researchFieldRepository.getPapersExcludingSubFields(
                    id = id, pageable = pageable
                )
            })
        return resourceService.map(PagedResourcesGenerator {
            researchFieldRepository.getPapersExcludingSubFieldsWithFlags(
                id = id, featured = modifiedFeatured, unlisted = unlisted, pageable = pageable
            )
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
                researchFieldRepository.getComparisonsExcludingSubFields(
                    id = id, pageable = pageable
                )
            })
        return resourceService.map(PagedResourcesGenerator {
            researchFieldRepository.getComparisonsExcludingSubFieldsWithFlags(
                id = id, featured = modifiedFeatured, unlisted = unlisted, pageable = pageable
            )
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
                researchFieldRepository.getProblemsExcludingSubFields(
                    id = id, pageable = pageable
                )
            })
        return resourceService.map(PagedResourcesGenerator {
            researchFieldRepository.getProblemsExcludingSubFieldsWithFlags(
                id = id, featured = modifiedFeatured, unlisted = unlisted, pageable = pageable
            )
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
        val pages = when (featured) {
            null -> getListIncludingSubFieldsWithoutFeaturedFlag(classesList, id, unlisted, pageable)
            else -> getListIncludingSubFieldsWithFlags(classesList, id, featured, unlisted, pageable)
        }
        val resultList = pages.map { it.content }.flatten().sortedWith { o1, o2 ->
            o2.createdAt.compareTo(o1.createdAt)
        }
        val totalElements = pages.sumOf { it.totalElements }
        return resourceService.map(PagedResourcesGenerator {
            PageImpl(resultList, pageable, totalElements)
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
        val pages = when (featured) {
            null -> getListExcludingSubFieldsWithoutFeaturedFlag(classesList, id, unlisted, pageable)
            else -> getListExcludingSubFieldsWithFlags(classesList, id, featured, unlisted, pageable)
        }
        val resultList = pages.map { it.content }.flatten().sortedBy(Resource::createdAt)
        val totalElements = pages.sumOf { it.totalElements }
        return resourceService.map(PagedResourcesGenerator {
            PageImpl(resultList, pageable, totalElements)
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
        pageable: Pageable
    ): List<Page<Resource>> {
        return classesList.map { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> researchFieldRepository.getPapersIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
                "COMPARISON" -> researchFieldRepository.getComparisonsIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
                "VISUALIZATION" -> researchFieldRepository.getVisualizationsIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
                "LITERATURELISTPUBLISHED" -> researchFieldRepository.getLiteratureListIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
                "PROBLEM" -> researchFieldRepository.getProblemsIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
                else -> researchFieldRepository.getSmartReviewsIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
            }
        }
    }

    private fun getListIncludingSubFieldsWithFlags(
        classesList: List<String>,
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): List<Page<Resource>> {
        return classesList.map { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> researchFieldRepository.getPapersIncludingSubFieldsWithFlags(id, featured, unlisted, pageable)
                "COMPARISON" -> researchFieldRepository.getComparisonsIncludingSubFieldsWithFlags(id, featured, unlisted, pageable)
                "VISUALIZATION" -> researchFieldRepository.getVisualizationsIncludingSubFieldsWithFlags(id, featured, unlisted, pageable)
                "LITERATURELISTPUBLISHED" -> researchFieldRepository.getLiteratureListIncludingSubFieldsWithFlags(id, featured, unlisted, pageable)
                "PROBLEM" -> researchFieldRepository.getProblemsIncludingSubFieldsWithFlags(id, featured, unlisted, pageable)
                else -> researchFieldRepository.getSmartReviewsIncludingSubFieldsWithFlags(id, featured, unlisted, pageable)
            }
        }
    }

    private fun getListExcludingSubFieldsWithoutFeaturedFlag(
        classesList: List<String>,
        id: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): List<Page<Resource>> {
        return classesList.map { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> researchFieldRepository.getPapersExcludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
                "COMPARISON" -> researchFieldRepository.getComparisonsExcludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
                "VISUALIZATION" -> researchFieldRepository.getVisualizationsExcludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
                "LITERATURELISTPUBLISHED" -> researchFieldRepository.getLiteratureListExcludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
                "PROBLEM" -> researchFieldRepository.getProblemsExcludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
                else -> researchFieldRepository.getSmartReviewsIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
            }
        }
    }

    private fun getListExcludingSubFieldsWithFlags(
        classesList: List<String>,
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): List<Page<Resource>> {
        return classesList.map { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> researchFieldRepository.getPapersExcludingSubFieldsWithFlags(id, featured, unlisted, pageable)
                "COMPARISON" -> researchFieldRepository.getComparisonsExcludingSubFieldsWithFlags(id, featured, unlisted, pageable)
                "VISUALIZATION" -> researchFieldRepository.getVisualizationsExcludingSubFieldsWithFlags(id, featured, unlisted, pageable)
                "LITERATURELISTPUBLISHED" -> researchFieldRepository.getLiteratureListExcludingSubFieldsWithFlags(id, featured, unlisted, pageable)
                "PROBLEM" -> researchFieldRepository.getProblemsExcludingSubFieldsWithFlags(id, featured, unlisted, pageable)
                else -> researchFieldRepository.getSmartReviewsExcludingSubFieldsWithFlags(id, featured, unlisted, pageable)
            }
        }
    }
}
