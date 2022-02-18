package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.*

interface RetrieveLiteralUseCase {
    // legacy methods:
    fun findAll(): Iterable<Literal>
    fun findById(id: LiteralId?): Optional<Literal>
    fun findAllByLabel(label: String): Iterable<Literal>
    fun findAllByLabelContaining(part: String): Iterable<Literal>
    fun findDOIByContributionId(id: ResourceId): Optional<Literal>
}
