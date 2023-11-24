package org.orkg.community.output

import java.util.*
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.Observatory
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

    fun allMembers(id: ObservatoryId, pageable: Pageable): Page<Contributor>
}
