package org.orkg.community.adapter.output.jpa

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.community.adapter.output.jpa.internal.ContributorEntity
import org.orkg.community.adapter.output.jpa.internal.PostgresContributorRepository
import org.orkg.community.adapter.output.jpa.internal.toContributor
import org.orkg.community.domain.Contributor
import org.orkg.community.output.ContributorRepository
import org.orkg.eventbus.Event
import org.orkg.eventbus.EventBus
import org.orkg.eventbus.Listener
import org.orkg.eventbus.events.DisplayNameUpdated
import org.orkg.eventbus.events.UserRegistered
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.beans.factory.InitializingBean
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
@TransactionalOnJPA
class ContributorFromUserAdapter(
    private val eventBus: EventBus,
    private val postgresContributorRepository: PostgresContributorRepository,
) : ContributorRepository, Listener, InitializingBean {
    override fun afterPropertiesSet() {
        eventBus.register(this)
    }

    override fun findAll(pageable: Pageable): Page<Contributor> =
        postgresContributorRepository.findAll(pageable).map(ContributorEntity::toContributor)

    override fun findById(id: ContributorId): Optional<Contributor> =
        postgresContributorRepository.findById(id.value).map(ContributorEntity::toContributor)

    override fun findAllById(ids: List<ContributorId>): List<Contributor> =
        postgresContributorRepository.findAllById(ids.map { it.value })
            .map(ContributorEntity::toContributor)

    override fun save(contributor: Contributor) {
        postgresContributorRepository.save(
            ContributorEntity(
                id = contributor.id.value,
                displayName = contributor.name,
                joinedAt = contributor.joinedAt.toLocalDateTime(),
                organizationId = contributor.organizationId.takeUnless { it == OrganizationId.UNKNOWN }?.value,
                observatoryId = contributor.observatoryId.takeUnless { it == ObservatoryId.UNKNOWN }?.value,
                emailMD5 = contributor.emailMD5.value,
                curator = contributor.isCurator,
                admin = contributor.isAdmin
            )
        )
    }

    override fun count(): Long = postgresContributorRepository.count()

    override fun deleteById(contributorId: ContributorId) {
        postgresContributorRepository.deleteById(contributorId.value)
    }

    override fun deleteAll() {
        postgresContributorRepository.deleteAll()
    }

    override fun notify(event: Event) {
        when (event) {
            is UserRegistered -> postgresContributorRepository.save(ContributorEntity.from(event))
            is DisplayNameUpdated -> postgresContributorRepository.findById(event.id).ifPresent { entity ->
                entity.displayName = event.displayName
                postgresContributorRepository.save(entity)
            }
            else -> { /* silently ignore unknown events */
            }
        }
    }
}
