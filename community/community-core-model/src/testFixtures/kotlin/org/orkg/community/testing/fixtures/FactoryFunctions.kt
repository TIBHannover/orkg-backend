package org.orkg.community.testing.fixtures

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.Observatory
import org.orkg.community.domain.Organization
import org.orkg.community.domain.OrganizationType
import org.orkg.mediastorage.domain.ImageId

fun createContributor(
    id: ContributorId = ContributorId("824e21b5-5df6-44c7-b2db-5929598f7398"),
    name: String = "Some Name",
    email: String? = null,
    observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),
    organizationId: OrganizationId = OrganizationId.createUnknownOrganization(),
    joinedAt: OffsetDateTime = OffsetDateTime.parse("2023-10-06T10:37:17.055493+01:00"),
): Contributor = Contributor(
    id = id,
    name = name,
    joinedAt = joinedAt,
    observatoryId = observatoryId,
    organizationId = organizationId,
    email = email,
)

fun createOrganization(
    id: OrganizationId = OrganizationId("d02073bc-30fd-481e-9167-f3fc3595d590"),
    name: String = "some organization name",
    createdBy: ContributorId = ContributorId("ee06bdf3-d6f3-41d1-8af2-64c583d9057e"),
    homepage: String = "https://example.org",
    observatories: Set<ObservatoryId> = emptySet(),
    displayId: String = "some display id",
    type: OrganizationType = OrganizationType.GENERAL,
    logoId: ImageId? = null
) = Organization(id, name, createdBy, homepage, observatories, displayId, type, logoId)

fun createObservatory(
    organizationIds: Set<OrganizationId> = emptySet(),
    id: ObservatoryId = ObservatoryId("95565e51-2b80-4c28-918c-6fbc5e2a9b33"),
    name: String = "Test Observatory",
    description: String = "Example Description",
    researchField: ThingId = ThingId("R1234"),
    members: Set<ContributorId> = emptySet(),
    displayId: String = "test_observatory"
) = Observatory(id, name, description, researchField, members, organizationIds, displayId)
