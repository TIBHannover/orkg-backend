package org.orkg.community.adapter.output.jpa

import java.util.*
import org.orkg.auth.domain.User
import org.orkg.auth.output.UserRepository
import org.orkg.common.ContributorId
import org.orkg.community.adapter.output.jpa.internal.toContributor
import org.orkg.community.domain.Contributor
import org.orkg.community.output.ContributorRepository
import org.springframework.stereotype.Component

@Component
class ContributorFromUserAdapter(
    private val userRepository: UserRepository,
) : ContributorRepository {
    override fun findById(id: ContributorId): Optional<Contributor> =
        userRepository.findById(id.value).map(User::toContributor)

    override fun findAllByIds(ids: List<ContributorId>): List<Contributor> =
        ids.mapNotNull { userRepository.findById(it.value).map(User::toContributor).orElse(null) }

    override fun countActiveUsers(): Long = userRepository.count()
}
