package org.orkg.community.input

import java.util.*
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.Observatory
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
