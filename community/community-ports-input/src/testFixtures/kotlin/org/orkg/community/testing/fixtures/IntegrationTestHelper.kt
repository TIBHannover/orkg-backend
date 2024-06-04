package org.orkg.community.testing.fixtures

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.OrganizationType
import org.orkg.community.input.CreateObservatoryUseCase
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.mediastorage.domain.ImageId

// Organizations

fun OrganizationUseCases.createOrganization(
    createdBy: ContributorId,
    organizationName: String = "Test Organization",
    url: String = "https://www.example.org",
    displayId: String = organizationName.toDisplayId(),
    type: OrganizationType = OrganizationType.GENERAL,
    id: OrganizationId? = null,
    logoId: ImageId? = null
) = this.create(id, organizationName, createdBy, url, displayId, type, logoId)

// Observatories

fun ObservatoryUseCases.createObservatory(
    id: ObservatoryId? = null,
    name: String = "Test Observatory",
    description: String = "Example description",
    organizations: Set<OrganizationId> = emptySet(),
    researchField: ThingId = ThingId("R123"),
    displayId: String = name.toDisplayId(),
    sustainableDevelopmentGoals: Set<ThingId> = emptySet()
) = this.create(
    CreateObservatoryUseCase.CreateCommand(
        id = id,
        name = name,
        description = description,
        organizations = organizations,
        researchField = researchField,
        displayId = displayId,
        sustainableDevelopmentGoals = sustainableDevelopmentGoals
    )
)

private fun String.toDisplayId() = this.lowercase().replace(Regex("[^a-zA-Z0-9_]"), "_")
