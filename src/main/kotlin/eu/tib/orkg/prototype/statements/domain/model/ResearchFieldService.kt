package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResearchFieldService {
    fun getResearchProblemsOfField(id: ResourceId, pageable: Pageable): Page<Any>

    fun getResearchProblemsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getContributorsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor>

    fun getPapersIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getComparisonsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getContributorsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor>

    fun getPapersExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getComparisonsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getResearchProblemsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>
}
