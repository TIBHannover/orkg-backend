package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.statements.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveResearchFieldUseCase {
    fun findById(id: ResourceId): Optional<ResourceRepresentation>

    fun getResearchProblemsOfField(id: ResourceId, pageable: Pageable): Page<PaperCountPerResearchProblem>

    fun getResearchProblemsIncludingSubFields(id: ResourceId, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun getContributorsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor>

    fun getPapersIncludingSubFields(id: ResourceId, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun getComparisonsIncludingSubFields(id: ResourceId, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun getContributorsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor>

    fun getPapersExcludingSubFields(id: ResourceId, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun getComparisonsExcludingSubFields(id: ResourceId, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun getResearchProblemsExcludingSubFields(id: ResourceId, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun getEntitiesBasedOnClassesIncludingSubfields(id: ResourceId, classesList: List<String>, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun getEntitiesBasedOnClassesExcludingSubfields(id: ResourceId, classesList: List<String>, featured: Boolean?, unlisted: Boolean, pageable: Pageable): Page<ResourceRepresentation>

    fun withBenchmarks(): List<ResearchField>

    data class PaperCountPerResearchProblem(
        val problem: ResourceRepresentation,
        val papers: Long
    )
}
