package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.*

interface LiteralRepository {
    fun exists(id: LiteralId): Boolean

    // legacy methods:
    fun nextIdentity(): LiteralId
    fun save(literal: Literal)
    fun deleteAll()
    fun findAll(): Iterable<Literal>
    fun findByLiteralId(id: LiteralId?): Optional<Literal>
    fun findAllByLabel(value: String): Iterable<Literal>
    fun findAllByLabelMatchesRegex(label: String): Iterable<Literal>
    fun findAllByLabelContaining(part: String): Iterable<Literal>
    fun findDOIByContributionId(id: ResourceId): Optional<Literal>
}
