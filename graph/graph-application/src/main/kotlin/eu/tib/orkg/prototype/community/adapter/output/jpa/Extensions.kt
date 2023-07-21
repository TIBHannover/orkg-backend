package eu.tib.orkg.prototype.community.adapter.output.jpa

import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.JpaUserRepository
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.ObservatoryEntity
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresObservatoryRepository
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.application.OrganizationNotFound
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.statements.application.UserNotFound

internal fun PostgresObservatoryRepository.toObservatoryEntity(
    observatory: Observatory,
    organizationRepository: PostgresOrganizationRepository,
    userRepository: JpaUserRepository,
): ObservatoryEntity =
    findById(observatory.id.value).orElse(ObservatoryEntity()).apply {
        id = observatory.id.value
        name = observatory.name
        description = observatory.description
        researchField = observatory.researchField?.value
        users = observatory.members.map {
            userRepository.findById(it.value)
                .orElseThrow { UserNotFound(it.value) }
        }.toMutableSet()
        displayId = observatory.displayId
        organizations = observatory.organizationIds.map {
            organizationRepository.findById(it.value)
                .orElseThrow { OrganizationNotFound(it.value.toString()) }
        }.toMutableSet()
    }
