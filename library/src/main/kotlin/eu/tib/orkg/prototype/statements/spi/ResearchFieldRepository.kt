package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResearchFieldRepository {
    fun findById(id: ResourceId): Optional<Resource>
    fun getResearchProblemsOfField(fieldId: ResourceId, pageable: Pageable): Page<ProblemsPerField>
    fun getContributorIdsFromResearchFieldAndIncludeSubfields(id: ResourceId, pageable: Pageable): Page<ContributorId>
    fun getPapersIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>
    fun getPapersIncludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getPapersIncludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getComparisonsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>
    fun getComparisonsIncludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getComparisonsIncludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getProblemsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>
    fun getProblemsIncludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getProblemsIncludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getContributorIdsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<ContributorId>
    fun getPapersExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>
    fun getPapersExcludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getPapersExcludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getComparisonsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>
    fun getComparisonsExcludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getComparisonsExcludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getProblemsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>
    fun getProblemsExcludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getProblemsExcludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun findResearchFieldsWithBenchmarks(): Iterable<Resource>
    fun getVisualizationsIncludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getVisualizationsIncludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getSmartReviewsIncludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getSmartReviewsIncludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getLiteratureListIncludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getLiteratureListIncludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getVisualizationsExcludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getVisualizationsExcludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getSmartReviewsExcludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getSmartReviewsExcludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getLiteratureListExcludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getLiteratureListExcludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Resource>
}

data class ProblemsPerField(
    val problem: Resource,
    val papers: Long
)
