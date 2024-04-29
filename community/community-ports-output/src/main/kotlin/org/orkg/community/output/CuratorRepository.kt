package org.orkg.community.output

import org.orkg.common.ContributorId
import org.orkg.community.domain.Contributor

interface CuratorRepository {
    fun findById(id: ContributorId): Contributor?
}
