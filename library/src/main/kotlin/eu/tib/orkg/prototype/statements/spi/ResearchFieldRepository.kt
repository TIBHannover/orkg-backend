package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResearchFieldRepository {
    fun findById(id: ThingId): Optional<Resource>
    fun getResearchProblemsOfField(fieldId: ThingId, pageable: Pageable): Page<ProblemsPerField>
    fun getContributorIdsFromResearchFieldAndIncludeSubfields(id: ThingId, pageable: Pageable): Page<ContributorId>
    fun getPapersIncludingSubFields(id: ThingId, pageable: Pageable): Page<Resource>
    fun getPapersIncludingSubFieldsWithFlags(id: ThingId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getPapersIncludingSubFieldsWithoutFeaturedFlag(id: ThingId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getComparisonsIncludingSubFields(id: ThingId, pageable: Pageable): Page<Resource>
    fun getComparisonsIncludingSubFieldsWithFlags(id: ThingId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getComparisonsIncludingSubFieldsWithoutFeaturedFlag(id: ThingId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getProblemsIncludingSubFields(id: ThingId, pageable: Pageable): Page<Resource>
    fun getProblemsIncludingSubFieldsWithFlags(id: ThingId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getProblemsIncludingSubFieldsWithoutFeaturedFlag(id: ThingId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getContributorIdsExcludingSubFields(id: ThingId, pageable: Pageable): Page<ContributorId>
    fun getPapersExcludingSubFields(id: ThingId, pageable: Pageable): Page<Resource>
    fun getPapersExcludingSubFieldsWithFlags(id: ThingId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getPapersExcludingSubFieldsWithoutFeaturedFlag(id: ThingId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getComparisonsExcludingSubFields(id: ThingId, pageable: Pageable): Page<Resource>
    fun getComparisonsExcludingSubFieldsWithFlags(id: ThingId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getComparisonsExcludingSubFieldsWithoutFeaturedFlag(id: ThingId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getProblemsExcludingSubFields(id: ThingId, pageable: Pageable): Page<Resource>
    fun getProblemsExcludingSubFieldsWithFlags(id: ThingId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getProblemsExcludingSubFieldsWithoutFeaturedFlag(id: ThingId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun findResearchFieldsWithBenchmarks(pageable: Pageable): Page<Resource>
    fun getVisualizationsIncludingSubFieldsWithFlags(id: ThingId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getVisualizationsIncludingSubFieldsWithoutFeaturedFlag(id: ThingId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getSmartReviewsIncludingSubFieldsWithFlags(id: ThingId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getSmartReviewsIncludingSubFieldsWithoutFeaturedFlag(id: ThingId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getLiteratureListIncludingSubFieldsWithFlags(id: ThingId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getLiteratureListIncludingSubFieldsWithoutFeaturedFlag(id: ThingId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getVisualizationsExcludingSubFieldsWithFlags(id: ThingId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getVisualizationsExcludingSubFieldsWithoutFeaturedFlag(id: ThingId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getSmartReviewsExcludingSubFieldsWithFlags(id: ThingId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getSmartReviewsExcludingSubFieldsWithoutFeaturedFlag(id: ThingId, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getLiteratureListExcludingSubFieldsWithFlags(id: ThingId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun getLiteratureListExcludingSubFieldsWithoutFeaturedFlag(id: ThingId, unlisted: Boolean, pageable: Pageable): Page<Resource>
}

data class ProblemsPerField(
    val problem: Resource,
    val papers: Long
)
