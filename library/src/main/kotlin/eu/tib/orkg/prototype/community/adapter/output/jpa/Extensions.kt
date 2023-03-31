package eu.tib.orkg.prototype.community.adapter.output.jpa

import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.JpaUserRepository
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.ObservatoryEntity
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresObservatoryRepository
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.domain.model.Observatory

internal fun PostgresObservatoryRepository.toObservatoryEntity(
    observatory: Observatory,
    organizationRepository: PostgresOrganizationRepository,
    userRepository: JpaUserRepository,
): ObservatoryEntity =
    this.findById(observatory.id?.value!!).orElse(ObservatoryEntity()).apply {
        id = observatory.id.value
        name = observatory.name
        description = observatory.description
        researchField = observatory.researchField?.id
        users = observatory.members.map {
            userRepository.findById(it.id.value).get()
        }.toMutableSet()
        displayId = observatory.displayId
        organizations = observatory.organizationIds.map {
            organizationRepository.findById(it.value).get()
        }.toMutableSet()
    }
