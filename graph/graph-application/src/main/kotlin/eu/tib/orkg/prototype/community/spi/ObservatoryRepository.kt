package eu.tib.orkg.prototype.community.spi

import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ObservatoryRepository {
    fun save(observatory: Observatory)

    fun findById(id: ObservatoryId): Optional<Observatory>

    fun findByName(name: String): Optional<Observatory>

    fun findAllByNameContains(name: String, pageable: Pageable): Page<Observatory>

    fun findByDisplayId(displayId: String): Optional<Observatory>

    fun findAll(pageable: Pageable): Page<Observatory>

    fun findAllByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Observatory>

    fun findAllByResearchField(researchField: ThingId, pageable: Pageable): Page<Observatory>

    fun findAllResearchFields(pageable: Pageable): Page<ThingId>

    fun deleteAll()
}
