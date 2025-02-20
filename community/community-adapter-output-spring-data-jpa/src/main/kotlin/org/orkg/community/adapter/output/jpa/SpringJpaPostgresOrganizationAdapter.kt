package org.orkg.community.adapter.output.jpa

import org.orkg.common.OrganizationId
import org.orkg.community.adapter.output.jpa.internal.ContributorEntity
import org.orkg.community.adapter.output.jpa.internal.OrganizationEntity
import org.orkg.community.adapter.output.jpa.internal.PostgresContributorRepository
import org.orkg.community.adapter.output.jpa.internal.PostgresObservatoryRepository
import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.community.adapter.output.jpa.internal.toContributor
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.Organization
import org.orkg.community.domain.OrganizationType
import org.orkg.community.output.OrganizationRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional

@Component
@TransactionalOnJPA
class SpringJpaPostgresOrganizationAdapter(
    private val postgresOrganizationRepository: PostgresOrganizationRepository,
    private val postgresObservatoryRepository: PostgresObservatoryRepository,
    private val postgresContributorRepository: PostgresContributorRepository,
) : OrganizationRepository {
    override fun save(organization: Organization) {
        postgresOrganizationRepository.save(
            postgresOrganizationRepository.toOrganizationEntity(organization, postgresObservatoryRepository)
        )
    }

    override fun deleteAll() = postgresOrganizationRepository.deleteAll()

    override fun findAllMembersByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Contributor> =
        postgresContributorRepository.findAllByOrganizationId(id.value, pageable).map(ContributorEntity::toContributor)

    override fun findById(id: OrganizationId): Optional<Organization> =
        postgresOrganizationRepository.findById(id.value).map(OrganizationEntity::toOrganization)

    override fun findByDisplayId(name: String): Optional<Organization> =
        postgresOrganizationRepository.findByDisplayId(name).map(OrganizationEntity::toOrganization)

    override fun findByName(name: String): Optional<Organization> =
        postgresOrganizationRepository.findByName(name).map(OrganizationEntity::toOrganization)

    override fun findAllByType(type: OrganizationType): List<Organization> =
        postgresOrganizationRepository.findByType(type).map(OrganizationEntity::toOrganization)

    override fun count(): Long = postgresOrganizationRepository.count()
}
