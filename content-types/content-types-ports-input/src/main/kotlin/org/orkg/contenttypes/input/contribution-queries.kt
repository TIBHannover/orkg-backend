package org.orkg.contenttypes.input

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Contribution
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveContributionUseCase {
    fun findById(id: ThingId): Optional<Contribution>
    fun findAll(pageable: Pageable): Page<Contribution>
}
