package org.orkg.community.adapter.output.jpa

import org.orkg.auth.domain.Role
import org.orkg.auth.domain.User
import org.orkg.auth.output.UserRepository
import org.orkg.common.ContributorId
import org.orkg.community.adapter.output.jpa.internal.toContributor
import org.orkg.community.domain.Contributor
import org.orkg.community.output.CuratorRepository
import org.springframework.stereotype.Component

@Component
class CuratorFromUserAdapter(
    private val userRepository: UserRepository,
) : CuratorRepository {
    override fun findById(id: ContributorId): Contributor? =
        userRepository.findById(id.value)
            .filter { Role("ADMIN") in it.roles }
            .map(User::toContributor)
            .orElse(null)
}
