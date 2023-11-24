package org.orkg.graph.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetResourceFlagQuery {
    fun getFeaturedResourceFlag(id: ThingId): Boolean
    fun getUnlistedResourceFlag(id: ThingId): Boolean
}

interface GetContributorsQuery {
    fun findAllContributorIds(pageable: Pageable): Page<ContributorId>
}
