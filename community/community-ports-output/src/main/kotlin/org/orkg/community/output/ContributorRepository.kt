package org.orkg.community.output

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.community.domain.Contributor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ContributorRepository {
    fun findAll(pageable: Pageable): Page<Contributor>

    fun findById(id: ContributorId): Optional<Contributor>

    fun findAllByIds(ids: List<ContributorId>): List<Contributor>

    fun save(contributor: Contributor)

    fun countActiveUsers(): Long

    fun deleteById(contributorId: ContributorId)

    fun deleteAll()
}
