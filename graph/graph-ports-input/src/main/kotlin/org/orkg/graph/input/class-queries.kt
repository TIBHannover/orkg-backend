package org.orkg.graph.input

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.SearchString
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveClassUseCase {
    fun exists(id: ThingId): Boolean
    // legacy methods:
    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null
    ): Page<Class>
    @Deprecated(message = "For removal")
    fun findAllById(ids: Iterable<ThingId>, pageable: Pageable): Page<Class>
    fun findById(id: ThingId): Optional<Class>
}
