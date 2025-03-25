package org.orkg.community.adapter.output.jpa.internal

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PostgresContributorRepository : JpaRepository<ContributorEntity, UUID> {
    fun findAllByObservatoryId(observatoryId: UUID, pageable: Pageable): Page<ContributorEntity>

    fun findAllByOrganizationId(organizationId: UUID, pageable: Pageable): Page<ContributorEntity>

    fun findAllByDisplayNameContainsIgnoreCase(label: String, pageable: Pageable): Page<ContributorEntity>
}
