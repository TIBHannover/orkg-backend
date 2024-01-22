package org.orkg.community.adapter.output.jpa

import org.orkg.auth.adapter.output.jpa.internal.JpaUserRepository
import org.orkg.community.adapter.output.jpa.internal.ConferenceSeriesEntity
import org.orkg.community.adapter.output.jpa.internal.ObservatoryEntity
import org.orkg.community.adapter.output.jpa.internal.OrganizationEntity
import org.orkg.community.adapter.output.jpa.internal.PostgresConferenceSeriesRepository
import org.orkg.community.adapter.output.jpa.internal.PostgresObservatoryRepository
import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.community.domain.ConferenceSeries
import org.orkg.community.domain.Observatory
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.Organization
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

internal fun PostgresOrganizationRepository.toOrganizationEntity(
    organization: Organization,
    observatoryRepository: PostgresObservatoryRepository,
): OrganizationEntity = findById(organization.id!!.value).orElse(OrganizationEntity()).apply {
    id = organization.id!!.value
    name = organization.name
    createdBy = organization.createdBy?.value // FIXME: should always be set
    url = organization.homepage
    displayId = organization.displayId
    type = organization.type
    logoId = organization.logoId?.value
    observatories = organization.observatoryIds.map {
        observatoryRepository.findById(it.value).orElseThrow { ObservatoryNotFound(it) }
    }.toMutableSet()
    // "conferenceSeries" is read-only (not insertable)
}

internal fun PostgresConferenceSeriesRepository.toConferenceSeriesEntity(
    conferenceSeries: ConferenceSeries,
    postgresOrganizationRepository: PostgresOrganizationRepository,
): ConferenceSeriesEntity =
    findById(conferenceSeries.id.value).orElse(ConferenceSeriesEntity()).apply {
        id = conferenceSeries.id.value
        name = conferenceSeries.name
        url = conferenceSeries.homepage
        displayId = conferenceSeries.displayId
        startDate = conferenceSeries.metadata.startDate
        reviewType = conferenceSeries.metadata.reviewType
        organization = conferenceSeries.organizationId.let { id ->
            postgresOrganizationRepository.findById(id.value).orElseThrow { OrganizationNotFound(id) }
        }
    }
