package eu.tib.orkg.prototype.statements.domain.model

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResearchFieldService {

    fun getResearchProblemsOfField(fieldId: ResourceId, pageable: Pageable): Page<Any>
}
