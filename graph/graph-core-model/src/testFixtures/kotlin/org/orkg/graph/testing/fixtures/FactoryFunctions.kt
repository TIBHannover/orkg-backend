package org.orkg.graph.testing.fixtures

import java.net.URI
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.List
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility

typealias KotlinList<T> = kotlin.collections.List<T>

/**
 * Creates a resource that uses as many defaults as possible.
 */
fun createResource(
    id: ThingId = ThingId("R1"),
    label: String = "Default Label",
    createdAt: OffsetDateTime = OffsetDateTime.parse("2023-10-06T11:28:14.613254+01:00"),
    classes: Set<ThingId> = emptySet(),
    createdBy: ContributorId = ContributorId.UNKNOWN,
    observatoryId: ObservatoryId = ObservatoryId.UNKNOWN,
    extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    organizationId: OrganizationId = OrganizationId.UNKNOWN,
    visibility: Visibility = Visibility.DEFAULT,
    verified: Boolean? = null,
    unlistedBy: ContributorId? = null,
    modifiable: Boolean = true,
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
    unlistedBy = unlistedBy,
    modifiable = modifiable
)

fun createClass(
    id: ThingId = ThingId("OK"),
    label: String = "some label",
    createdAt: OffsetDateTime = OffsetDateTime.parse("2023-10-05T12:29:15.3155145+01:00"),
    uri: URI? = URI.create("https://example.org/OK"),
    createdBy: ContributorId = ContributorId("dc8b2055-c14a-4e9f-9fcd-e0b79cf1f834"),
    modifiable: Boolean = true
): Class = Class(id, label, uri, createdAt, createdBy, modifiable)

fun createClassWithoutURI(): Class = createClass(uri = null)

fun createPredicate(
    id: ThingId = ThingId("P1"),
    label: String = "some predicate label",
    createdAt: OffsetDateTime = OffsetDateTime.parse("2023-10-04T13:30:16.931457+01:00"),
    createdBy: ContributorId = ContributorId("a56cfd65-8d29-4eae-a252-1b806fe88d3c"),
    modifiable: Boolean = true
) = Predicate(id, label, createdAt, createdBy, modifiable)

fun createLiteral(
    id: ThingId = ThingId("L1"),
    label: String = "Default Label",
    createdAt: OffsetDateTime = OffsetDateTime.parse("2023-10-03T14:31:17.365491+01:00"),
    createdBy: ContributorId = ContributorId("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1"),
    datatype: String = "xsd:string",
    modifiable: Boolean = true
) = Literal(id, label, datatype, createdAt, createdBy, modifiable)

fun createStatement(
    id: StatementId = StatementId(1),
    subject: Thing = createClass(),
    predicate: Predicate = createPredicate(),
    `object`: Thing = createClass(),
    createdAt: OffsetDateTime = OffsetDateTime.parse("2023-10-02T15:32:18.753961+01:00"),
    createdBy: ContributorId = ContributorId("34da5516-7901-4b0d-94c5-b062082e11a7"),
    modifiable: Boolean = true
) = GeneralStatement(id, subject, predicate, `object`, createdAt, createdBy, modifiable)

fun createList(
    id: ThingId = ThingId("List1"),
    label: String = "Default Label",
    elements: KotlinList<ThingId> = emptyList(),
    createdAt: OffsetDateTime = OffsetDateTime.parse("2023-10-01T16:33:19.156943+01:00"),
    createdBy: ContributorId = ContributorId.UNKNOWN,
    modifiable: Boolean = true
) = List(id, label, elements, createdAt, createdBy, modifiable)

fun createPaperResource(
    id: ThingId = ThingId("Paper1"),
    title: String = "Some title",
) = createResource(id = id, label = title, classes = setOf(Classes.paper))

fun createComparisonResource(
    id: ThingId = ThingId("Comparison1"),
) = createResource(id = id, classes = setOf(Classes.comparison))

fun createVisualizationResource(
    id: ThingId = ThingId("Visualization1"),
) = createResource(id = id, classes = setOf(Classes.visualization))
