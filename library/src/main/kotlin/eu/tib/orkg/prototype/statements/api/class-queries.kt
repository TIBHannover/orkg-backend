package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveClassUseCase {
    fun exists(id: ThingId): Boolean
    // legacy methods:
    fun findAll(pageable: Pageable): Page<Class>
    fun findAllById(ids: Iterable<ThingId>, pageable: Pageable): Page<Class>
    fun findById(id: ThingId): Optional<Class>
    fun findAllByLabel(
        labelSearchString: SearchString,
        pageable: Pageable
    ): Page<Class>
}
