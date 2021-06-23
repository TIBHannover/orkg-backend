package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResearchFieldService {
    fun findById(id: ResourceId): Optional<Resource>

    fun getResearchProblemsOfField(id: ResourceId, pageable: Pageable): Page<Any>

    // fun getResearchProblemsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getResearchProblemsIncludingSubFields(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>

    fun getContributorsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor>

    // fun getPapersIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getPapersIncludingSubFields(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>

    // fun getComparisonsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getComparisonsIncludingSubFields(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>

    fun getContributorsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor>

    // fun getPapersExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getPapersExcludingSubFields(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>

    // fun getComparisonsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getComparisonsExcludingSubFields(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>

    // fun getResearchProblemsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getResearchProblemsExcludingSubFields(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
}
