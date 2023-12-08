package org.orkg.community.adapter.output.jpa

import org.orkg.auth.domain.User
import org.orkg.auth.output.UserRepository
import org.orkg.common.ContributorId
import org.orkg.community.output.AdminRepository
import org.springframework.stereotype.Component

@Component
class AdminFromUserAdapter(
    private val userRepository: UserRepository,
) : AdminRepository {
    override fun hasAdminPriviledges(id: ContributorId): Boolean =
        userRepository.findById(id.value).map(User::isAdmin).orElse(false)
}
