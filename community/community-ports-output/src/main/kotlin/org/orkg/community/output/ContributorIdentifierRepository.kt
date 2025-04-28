package org.orkg.community.output

import org.orkg.common.ContributorId
import org.orkg.community.domain.ContributorIdentifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ContributorIdentifierRepository {
    fun save(identifier: ContributorIdentifier)

    fun findByContributorIdAndValue(contributorId: ContributorId, value: String): Optional<ContributorIdentifier>

    fun findAllByContributorId(contributorId: ContributorId, pageable: Pageable): Page<ContributorIdentifier>

    fun deleteByContributorIdAndValue(contributorId: ContributorId, value: String)

    fun deleteAll()

    fun count(): Long
}
