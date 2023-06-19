package eu.tib.orkg.prototype.community.api

import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveObservatoryUseCase {
    fun findAll(pageable: Pageable): Page<Observatory>

    fun findAllByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Observatory>

    fun findAllResearchFields(pageable: Pageable): Page<ThingId>

    fun findByName(name: String): Optional<Observatory>

    fun findAllByNameContains(name: String, pageable: Pageable): Page<Observatory>

    fun findById(id: ObservatoryId): Optional<Observatory>

    fun findByDisplayId(id: String): Optional<Observatory>

    fun findAllByResearchField(researchFieldId: ThingId, pageable: Pageable): Page<Observatory>
}
