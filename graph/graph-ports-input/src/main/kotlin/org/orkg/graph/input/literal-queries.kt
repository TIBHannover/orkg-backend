package org.orkg.graph.input

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.SearchString
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveLiteralUseCase {
    fun exists(id: ThingId): Boolean
    // legacy methods:
    fun findAll(pageable: Pageable): Page<Literal>
    fun findById(id: ThingId): Optional<Literal>
    fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Literal>
    fun findDOIByContributionId(id: ThingId): Optional<Literal>
}
