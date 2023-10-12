package eu.tib.orkg.prototype.community.testing.fixtures

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.OrganizationEntity
import eu.tib.orkg.prototype.community.domain.model.Contributor
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.Organization
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.OrganizationType
import eu.tib.orkg.prototype.files.domain.model.ImageId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime

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

/**
 * This method should only be used for mocking purposes, as does not return a valid database entity.
 */
fun Organization.toOrganizationEntity(): OrganizationEntity =
    OrganizationEntity().also {
        it.id = id!!.value
        it.name = name
        it.createdBy = createdBy?.value
        it.url = homepage
        it.displayId = displayId
        it.type = type
        it.logoId = logoId?.value
    }

fun createObservatory(
    organizationIds: Set<OrganizationId> = emptySet(),
    id: ObservatoryId = ObservatoryId("95565e51-2b80-4c28-918c-6fbc5e2a9b33"),
    name: String = "Test Observatory",
    description: String = "Example Description",
    researchField: ThingId = ThingId("R1234"),
    members: Set<ContributorId> = emptySet(),
    displayId: String = "test_observatory"
) = Observatory(id, name, description, researchField, members, organizationIds, displayId)
