package org.orkg.graph.input

import org.orkg.common.ContributorId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetContributorsQuery {
    fun findAllContributorIds(pageable: Pageable): Page<ContributorId>
}
