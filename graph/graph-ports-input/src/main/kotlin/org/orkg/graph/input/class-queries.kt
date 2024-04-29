package org.orkg.graph.input

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.SearchString
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
