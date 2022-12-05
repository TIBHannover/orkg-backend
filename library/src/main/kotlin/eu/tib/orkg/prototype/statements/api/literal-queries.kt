package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveLiteralUseCase {
    fun exists(id: LiteralId): Boolean
    // legacy methods:
    fun findAll(pageable: Pageable): Page<LiteralRepresentation>
    fun findById(id: LiteralId?): Optional<LiteralRepresentation>
    fun findAllByLabel(label: String, pageable: Pageable): Page<LiteralRepresentation>
    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<LiteralRepresentation>
    fun findDOIByContributionId(id: ResourceId): Optional<LiteralRepresentation>
}
