package org.orkg.community.adapter.output.jpa

import org.orkg.auth.adapter.output.jpa.internal.JpaUserRepository
import org.orkg.community.adapter.output.jpa.internal.ObservatoryEntity
import org.orkg.community.adapter.output.jpa.internal.PostgresObservatoryRepository
import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.community.domain.Observatory
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.graph.domain.UserNotFound

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
