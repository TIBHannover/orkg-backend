package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.graph.domain.ContributionInfo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ContributionComparisonRepository {
    fun findContributionsDetailsById(ids: List<ThingId>, pageable: Pageable): Page<ContributionInfo>
}
