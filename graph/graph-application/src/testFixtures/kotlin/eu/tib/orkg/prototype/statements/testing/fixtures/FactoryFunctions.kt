package eu.tib.orkg.prototype.statements.testing.fixtures

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.List
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.net.URI
import java.time.OffsetDateTime

typealias KotlinList<T> = kotlin.collections.List<T>

/**
 * Creates a resource that uses as many defaults as possible.
 */
fun createResource(
    id: ThingId = ThingId("R1"),
    label: String = "Default Label",
    createdAt: OffsetDateTime = OffsetDateTime.parse("2023-10-06T11:28:14.613254+01:00"),
    classes: Set<ThingId> = emptySet(),
    createdBy: ContributorId = ContributorId.createUnknownContributor(),
    observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),
    extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    organizationId: OrganizationId = OrganizationId.createUnknownOrganization(),
    visibility: Visibility = Visibility.DEFAULT,
    verified: Boolean? = null,
    unlistedBy: ContributorId? = null
) = Resource(
    id = id,
    label = label,
    createdAt = createdAt,
    classes = classes,
    createdBy = createdBy,
    observatoryId = observatoryId,
    extractionMethod = extractionMethod,
    organizationId = organizationId,
    visibility = visibility,
    verified = verified,
    unlistedBy = unlistedBy
)

fun createClass(
    id: ThingId = ThingId("OK"),
    label: String = "some label",
    createdAt: OffsetDateTime = OffsetDateTime.parse("2023-10-05T12:29:15.3155145+01:00"),
    uri: URI? = URI.create("https://example.org/OK"),
    createdBy: ContributorId = ContributorId("dc8b2055-c14a-4e9f-9fcd-e0b79cf1f834"),
    description: String? = null
): Class = Class(id, label, uri, createdAt, createdBy, description)

fun createClassWithoutURI(): Class = createClass(uri = null)

fun createPredicate(
    id: ThingId = ThingId("P1"),
    label: String = "some predicate label",
    createdAt: OffsetDateTime = OffsetDateTime.parse("2023-10-04T13:30:16.931457+01:00"),
    createdBy: ContributorId = ContributorId("a56cfd65-8d29-4eae-a252-1b806fe88d3c"),
    description: String? = null
) = Predicate(id, label, createdAt, createdBy, description)

fun createLiteral(
    id: ThingId = ThingId("L1"),
    label: String = "Default Label",
    createdAt: OffsetDateTime = OffsetDateTime.parse("2023-10-03T14:31:17.365491+01:00"),
    createdBy: ContributorId = ContributorId("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1"),
    datatype: String = "xsd:string"
) = Literal(id, label, datatype, createdAt, createdBy)

fun createStatement(
    id: StatementId = StatementId(1),
    subject: Thing = createClass(),
    predicate: Predicate = createPredicate(),
    `object`: Thing = createClass(),
    createdAt: OffsetDateTime = OffsetDateTime.parse("2023-10-02T15:32:18.753961+01:00"),
    createdBy: ContributorId = ContributorId("34da5516-7901-4b0d-94c5-b062082e11a7")
) = GeneralStatement(id, subject, predicate, `object`, createdAt, createdBy)

fun createList(
    id: ThingId = ThingId("List1"),
    label: String = "Default Label",
    elements: KotlinList<ThingId> = emptyList(),
    createdAt: OffsetDateTime = OffsetDateTime.parse("2023-10-01T16:33:19.156943+01:00"),
    createdBy: ContributorId = ContributorId.createUnknownContributor(),
) = List(id, label, elements, createdAt, createdBy)
