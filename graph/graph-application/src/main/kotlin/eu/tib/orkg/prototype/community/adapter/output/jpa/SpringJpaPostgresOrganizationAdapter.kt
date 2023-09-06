package eu.tib.orkg.prototype.community.adapter.output.jpa

import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.JpaUserRepository
import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.UserEntity
import eu.tib.orkg.prototype.auth.domain.User
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.toContributor
import eu.tib.orkg.prototype.community.spi.OrganizationRepository
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringJpaPostgresOrganizationAdapter(
    private val userRepository: JpaUserRepository,
) : OrganizationRepository {
    override fun allMembers(id: OrganizationId, pageable: Pageable): Page<Contributor> =
        userRepository.findByOrganizationId(id.value, pageable).map(UserEntity::toUser).map(User::toContributor)
}
