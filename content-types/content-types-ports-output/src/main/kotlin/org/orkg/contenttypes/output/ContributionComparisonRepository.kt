package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContributionInfo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ContributionComparisonRepository {
    fun findAllContributionDetailsById(ids: List<ThingId>, pageable: Pageable): Page<ContributionInfo>
}
