package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ClassRepository : EntityRepository<Class, ThingId> {
    // legacy methods:
    fun save(c: Class)
    fun findById(id: ThingId): Optional<Class>
    fun findAllById(id: Iterable<ThingId>, pageable: Pageable): Page<Class>
    fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Class>
    fun findByUri(uri: String): Optional<Class>
    fun findAllWithFilters(
        uri: String? = null,
        createdBy: ContributorId? = null,
        createdAt: OffsetDateTime? = null,
        pageable: Pageable
    ): Page<Class>
    fun deleteAll()
    fun nextIdentity(): ThingId

    /**
     * Determine if all classes in a set of given classes exist.
     *
     * @param ids The set of class IDs to be checked.
     * @return `true` if [ids] is non-empty and all classes exist, `false` otherwise.
     */
    fun existsAll(ids: Set<ThingId>): Boolean
}
