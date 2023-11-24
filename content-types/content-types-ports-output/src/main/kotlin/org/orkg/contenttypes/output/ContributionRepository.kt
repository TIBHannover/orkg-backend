package org.orkg.contenttypes.output

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ContributionRepository {
    fun findContributionByResourceId(id: ThingId): Optional<Resource>
    fun findAllListedContributions(pageable: Pageable): Page<Resource>
    fun findAllContributionsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource>
}
