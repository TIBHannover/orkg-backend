package org.orkg.community.output

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.community.domain.Contributor

interface ContributorRepository {
    fun findById(id: ContributorId): Optional<Contributor>

    fun findAllByIds(ids: List<ContributorId>): List<Contributor>
}
