package org.orkg.community.output

import org.orkg.common.ContributorId
import org.orkg.community.domain.Contributor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ContributorRepository {
    fun findAll(pageable: Pageable): Page<Contributor>

    fun findById(id: ContributorId): Optional<Contributor>

    fun findAllById(ids: List<ContributorId>): List<Contributor>

    fun save(contributor: Contributor)

    fun count(): Long

    fun deleteById(contributorId: ContributorId)

    fun deleteAll()
}
