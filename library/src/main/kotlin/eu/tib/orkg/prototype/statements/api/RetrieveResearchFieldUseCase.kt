package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveResearchFieldUseCase {
    fun findById(id: ThingId): Optional<ResourceRepresentation>

    fun getResearchProblemsOfField(id: ThingId, pageable: Pageable): Page<PaperCountPerResearchProblem>

    fun getResearchProblemsIncludingSubFields(id: ThingId, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun getContributorsIncludingSubFields(id: ThingId, pageable: Pageable): Page<Contributor>

    fun getPapersIncludingSubFields(id: ThingId, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun getComparisonsIncludingSubFields(id: ThingId, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun getContributorsExcludingSubFields(id: ThingId, pageable: Pageable): Page<Contributor>

    fun getPapersExcludingSubFields(id: ThingId, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun getComparisonsExcludingSubFields(id: ThingId, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun getResearchProblemsExcludingSubFields(id: ThingId, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun getEntitiesBasedOnClassesIncludingSubfields(id: ThingId, classesList: List<String>, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun getEntitiesBasedOnClassesExcludingSubfields(id: ThingId, classesList: List<String>, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun withBenchmarks(pageable: Pageable): Page<ResearchField>

    data class PaperCountPerResearchProblem(
        val problem: ResourceRepresentation,
        val papers: Long
    )
}
