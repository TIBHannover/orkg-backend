package org.orkg.graph.input

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.SearchString
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrievePredicateUseCase {
    fun exists(id: ThingId): Boolean
    // legacy methods:
    fun findAll(pageable: Pageable): Page<Predicate>
    fun findById(id: ThingId): Optional<Predicate>
    fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Predicate>
}
