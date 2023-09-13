package eu.tib.orkg.prototype.community.adapter.output.jpa

import eu.tib.orkg.prototype.auth.domain.User
import eu.tib.orkg.prototype.auth.spi.UserRepository
import eu.tib.orkg.prototype.community.domain.model.toContributor
import eu.tib.orkg.prototype.community.domain.model.Contributor
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.spi.ContributorRepository
import java.util.*
import org.springframework.stereotype.Component

@Component
class ContributorFromUserAdapter(
    private val userRepository: UserRepository,
) : ContributorRepository {
    override fun findById(id: ContributorId): Optional<Contributor> =
        userRepository.findById(id.value).map(User::toContributor)

    override fun findAllByIds(ids: List<ContributorId>): List<Contributor> =
        ids.mapNotNull { userRepository.findById(it.value).map(User::toContributor).orElse(null) }
}
