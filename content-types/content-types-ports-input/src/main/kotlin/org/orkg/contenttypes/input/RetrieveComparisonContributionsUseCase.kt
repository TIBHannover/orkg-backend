package org.orkg.contenttypes.input

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContributionInfo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveComparisonContributionsUseCase {
    fun findContributionsDetailsById(ids: List<ThingId>, pageable: Pageable): Page<ContributionInfo>
}
