package org.orkg.community.adapter.output.jpa

import org.orkg.auth.adapter.output.jpa.internal.JpaUserRepository
import org.orkg.auth.adapter.output.jpa.internal.UserEntity
import org.orkg.auth.domain.User
import org.orkg.common.OrganizationId
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.toContributor
import org.orkg.community.output.OrganizationRepository
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
