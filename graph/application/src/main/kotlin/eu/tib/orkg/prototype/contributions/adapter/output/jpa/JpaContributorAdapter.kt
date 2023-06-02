package eu.tib.orkg.prototype.contributions.adapter.output.jpa

import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.JpaUserRepository
import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.UserEntity
import eu.tib.orkg.prototype.auth.domain.User
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.toContributor
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.contributions.spi.ContributorRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class JpaContributorAdapter(
    private val repository: JpaUserRepository
) : ContributorRepository {
    override fun findById(id: ContributorId): Optional<Contributor> =
        repository.findById(id.value)
            .map(UserEntity::toUser)
            .map(User::toContributor)

    override fun findAllByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Contributor> =
        repository.findByOrganizationId(id.value, pageable)
            .map(UserEntity::toUser)
            .map(User::toContributor)

    override fun findAllByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Contributor> =
        repository.findByObservatoryId(id.value, pageable)
            .map(UserEntity::toUser)
            .map(User::toContributor)

    override fun findAllByIds(ids: List<ContributorId>): List<Contributor> =
        repository.findByIdIn(ids.map { it.value })
            .map(UserEntity::toUser)
            .map(User::toContributor)
}
