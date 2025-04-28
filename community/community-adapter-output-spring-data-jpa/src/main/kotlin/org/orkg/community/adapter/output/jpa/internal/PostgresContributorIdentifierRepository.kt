package org.orkg.community.adapter.output.jpa.internal

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface PostgresContributorIdentifierRepository : JpaRepository<ContributorIdentifierEntity, UUID> {
    fun findByContributorIdAndValue(contributorId: UUID, value: String): Optional<ContributorIdentifierEntity>

    fun findAllByContributorId(contributorId: UUID, pageable: Pageable): Page<ContributorIdentifierEntity>

    fun deleteByContributorIdAndValue(contributorId: UUID, value: String)
}
