package org.orkg.community.adapter.output.jpa

import org.orkg.community.adapter.output.jpa.internal.ConferenceSeriesEntity
import org.orkg.community.adapter.output.jpa.internal.ObservatoryEntity
import org.orkg.community.adapter.output.jpa.internal.OrganizationEntity
import org.orkg.community.adapter.output.jpa.internal.PostgresConferenceSeriesRepository
import org.orkg.community.adapter.output.jpa.internal.PostgresContributorRepository
import org.orkg.community.adapter.output.jpa.internal.PostgresObservatoryRepository
import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.community.domain.ConferenceSeries
import org.orkg.community.domain.Observatory
import org.orkg.community.domain.ObservatoryMemberNotFound
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.Organization
import org.orkg.community.domain.OrganizationNotFound

internal fun PostgresObservatoryRepository.toObservatoryEntity(
    observatory: Observatory,
    organizationRepository: PostgresOrganizationRepository,
    contributorRepository: PostgresContributorRepository,
): ObservatoryEntity =
    findById(observatory.id.value).orElse(ObservatoryEntity()).apply {
        id = observatory.id.value
        name = observatory.name
        description = observatory.description
        researchField = observatory.researchField?.value
        users = observatory.members.map {
            contributorRepository.findById(it.value)
                .orElseThrow { ObservatoryMemberNotFound(it.value) }
        }.toMutableSet()
        displayId = observatory.displayId
        organizations = observatory.organizationIds.map {
            organizationRepository.findById(it.value)
                .orElseThrow { OrganizationNotFound(it.value.toString()) }
        }.toMutableSet()
        sustainableDevelopmentGoals = observatory.sustainableDevelopmentGoals.map { it.value }.toMutableSet()
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
