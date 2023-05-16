package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LiteralRepository {
    fun exists(id: ThingId): Boolean

    // legacy methods:
    fun nextIdentity(): ThingId
    fun save(literal: Literal)
    fun deleteAll()
    fun findAll(pageable: Pageable): Page<Literal>
    fun findByLiteralId(id: ThingId): Optional<Literal>
    fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Literal>
}
