package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.*

interface RetrieveLiteralUseCase {
    fun exists(id: LiteralId): Boolean
    // legacy methods:
    fun findAll(): Iterable<LiteralRepresentation>
    fun findById(id: LiteralId?): Optional<LiteralRepresentation>
    fun findAllByLabel(label: String): Iterable<LiteralRepresentation>
    fun findAllByLabelContaining(part: String): Iterable<LiteralRepresentation>
    fun findDOIByContributionId(id: ResourceId): Optional<LiteralRepresentation>
}
