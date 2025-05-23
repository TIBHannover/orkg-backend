package org.orkg.community.testing.fixtures

import org.orkg.common.ContributorId
import org.orkg.common.IdentifierValue
import org.orkg.common.ORCID
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.ConferenceSeries
import org.orkg.community.domain.ConferenceSeriesId
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.ContributorIdentifier
import org.orkg.community.domain.Metadata
import org.orkg.community.domain.Observatory
import org.orkg.community.domain.ObservatoryFilter
import org.orkg.community.domain.ObservatoryFilterId
import org.orkg.community.domain.Organization
import org.orkg.community.domain.OrganizationType
import org.orkg.community.domain.PeerReviewType
import org.orkg.community.domain.internal.MD5Hash
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.PredicatePath
import org.orkg.graph.domain.Predicates
import org.orkg.mediastorage.domain.ImageId
import org.orkg.testing.MockUserId
import java.time.LocalDate
import java.time.OffsetDateTime

fun createContributor(
    id: ContributorId = ContributorId("824e21b5-5df6-44c7-b2db-5929598f7398"),
    name: String = "Some Name",
    email: String = "user@example.org",
    observatoryId: ObservatoryId = ObservatoryId.UNKNOWN,
    organizationId: OrganizationId = OrganizationId.UNKNOWN,
    // FIXME: See https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/533
    joinedAt: OffsetDateTime = OffsetDateTime.parse("2023-10-06T10:37:17.055493Z"),
    isCurator: Boolean = false,
    isAdmin: Boolean = false,
): Contributor = Contributor(
    id = id,
    name = name,
    joinedAt = joinedAt,
    observatoryId = observatoryId,
    organizationId = organizationId,
    emailMD5 = MD5Hash.fromEmail(email),
    isCurator = isCurator,
    isAdmin = isAdmin,
)

fun createContributorIdentifier(
    contributorId: ContributorId = ContributorId("824e21b5-5df6-44c7-b2db-5929598f7398"),
    type: ContributorIdentifier.Type = ContributorIdentifier.Type.ORCID,
    value: IdentifierValue = ORCID.of("0000-0001-5109-3700"),
    createdAt: OffsetDateTime = OffsetDateTime.parse("2023-10-06T10:37:17.055493Z"),
) = ContributorIdentifier(contributorId, type, value, createdAt)

fun createOrganization(
    id: OrganizationId = OrganizationId("d02073bc-30fd-481e-9167-f3fc3595d590"),
    name: String = "some organization name",
    createdBy: ContributorId = ContributorId("ee06bdf3-d6f3-41d1-8af2-64c583d9057e"),
    homepage: String = "https://example.org",
    observatories: Set<ObservatoryId> = emptySet(),
    displayId: String = "some display id",
    type: OrganizationType = OrganizationType.GENERAL,
    logoId: ImageId? = null,
) = Organization(id, name, createdBy, homepage, observatories, displayId, type, logoId)

fun createObservatory(
    organizationIds: Set<OrganizationId> = emptySet(),
    id: ObservatoryId = ObservatoryId("95565e51-2b80-4c28-918c-6fbc5e2a9b33"),
    name: String = "Test Observatory",
    description: String = "Example Description",
    researchField: ThingId = ThingId("R1234"),
    members: Set<ContributorId> = emptySet(),
    displayId: String = "test_observatory",
    sustainableDevelopmentGoals: Set<ThingId> = emptySet(),
) = Observatory(id, name, description, researchField, members, organizationIds, displayId, sustainableDevelopmentGoals)

fun createObservatoryFilter(
    id: ObservatoryFilterId = ObservatoryFilterId("c6b41f1e-ee46-48ea-8d47-57ce85760831"),
    observatoryId: ObservatoryId = ObservatoryId("b0c02a5a-e40f-41bb-af93-aebcfb0a58b5"),
    label: String = "Observatory filter",
    createdBy: ContributorId = ContributorId(MockUserId.USER),
    createdAt: OffsetDateTime = OffsetDateTime.parse("2023-10-17T10:42:48.324973+01:00"),
    path: PredicatePath = listOf(Predicates.hasResearchProblem),
    range: ThingId = Classes.resources,
    exact: Boolean = false,
    featured: Boolean = false,
): ObservatoryFilter = ObservatoryFilter(id, observatoryId, label, createdBy, createdAt, path, range, exact, featured)

fun createConferenceSeries(
    id: ConferenceSeriesId = ConferenceSeriesId("138acf53-0e81-46e1-b828-18ec6a8bb863"),
    organizationId: OrganizationId = OrganizationId("d02073bc-30fd-481e-9167-f3fc3595d590"),
    name: String = "some conference name",
    homepage: String = "https://example.org",
    displayId: String = "some display id",
    metadata: Metadata = Metadata(
        startDate = LocalDate.parse("2023-10-17"),
        reviewType = PeerReviewType.SINGLE_BLIND
    ),
) = ConferenceSeries(id, organizationId, name, homepage, displayId, metadata)
