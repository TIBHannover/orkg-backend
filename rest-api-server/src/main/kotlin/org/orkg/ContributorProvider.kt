package org.orkg

import org.orkg.auth.domain.Role
import org.orkg.auth.output.UserRepository
import org.orkg.common.PageRequests
import org.orkg.eventbus.EventBus
import org.orkg.eventbus.events.UserRegistered
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class ContributorProvider(
    private val eventBus: EventBus,
    private val userRepository: UserRepository,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        userRepository.findAll(PageRequests.ALL).forEach { user ->
            eventBus.post(
                UserRegistered(
                    id = user.id.toString(),
                    displayName = user.displayName,
                    enabled = true,
                    email = user.email,
                    roles = user.roles.map { UserRegistered.Role.from(it.name.removePrefix("ROLE_")) }.toSet(),
                    createdAt = user.createdAt,
                    observatoryId = user.observatoryId?.toString(),
                    organizationId = user.organizationId?.toString(),
                )
            )
        }
    }
}
