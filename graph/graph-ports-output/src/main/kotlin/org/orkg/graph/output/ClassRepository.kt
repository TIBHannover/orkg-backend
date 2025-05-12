package org.orkg.graph.output

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.SearchString
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface ClassRepository : EntityRepository<Class, ThingId> {
    fun save(c: Class)

    fun findById(id: ThingId): Optional<Class>

    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
    ): Page<Class>

    fun findByUri(uri: String): Optional<Class>

    fun deleteAll()

    fun nextIdentity(): ThingId

    /**
     * Determine if all classes in a set of given classes exist.
     *
     * @param ids The set of class IDs to be checked.
     * @return `true` if [ids] is non-empty and all classes exist, `false` otherwise.
     */
    fun existsAllById(ids: Set<ThingId>): Boolean
}
