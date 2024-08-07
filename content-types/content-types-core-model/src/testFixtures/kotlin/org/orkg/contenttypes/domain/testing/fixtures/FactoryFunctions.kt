package org.orkg.contenttypes.domain.testing.fixtures

import java.time.OffsetDateTime
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Visibility

fun createPublishedContentType(
    rootId: ThingId = ThingId("R123"),
    subgraph: List<GeneralStatement> = listOf(
        GeneralStatement(
            id = StatementId("S663825"),
            subject = Resource(
                id = ThingId("R166714"),
                label = "Entry",
                createdAt = OffsetDateTime.parse("2022-02-22T08:01:13.261082+01:00"),
                classes = emptySet(),
                createdBy = ContributorId("d5416c16-1a45-4aee-8069-be1b6097478b"),
                observatoryId = ObservatoryId.UNKNOWN,
                extractionMethod = ExtractionMethod.UNKNOWN,
                organizationId = OrganizationId.UNKNOWN,
                visibility = Visibility.DEFAULT
            ),
            predicate = Predicate(
                id = Predicates.hasPaper,
                label = "has paper",
                createdAt = OffsetDateTime.parse("2021-04-26T16:57:34.745465+02:00"),
                createdBy = ContributorId.UNKNOWN
            ),
            `object` = Class(
                id = ThingId("C12457"),
                label = "Some class",
                uri = ParsedIRI("https://orkg.org/class/C12457"),
                createdAt = OffsetDateTime.parse("2022-02-22T08:01:13.261082+01:00"),
                createdBy = ContributorId("d5416c16-1a45-4aee-8069-be1b6097478b")
            ),
            createdAt = OffsetDateTime.parse("2022-02-22T08:01:15.253502+01:00"),
            createdBy = ContributorId("d5416c16-1a45-4aee-8069-be1b6097478b")
        ),
        GeneralStatement(
            id = StatementId("S5436"),
            subject = Resource(
                id = ThingId("R56984"),
                label = "Other resource",
                createdAt = OffsetDateTime.parse("2022-02-22T08:01:12.709843+01:00"),
                classes = setOf(ThingId("Entry")),
                createdBy = ContributorId("d5416c16-1a45-4aee-8069-be1b6097478b"),
                observatoryId = ObservatoryId.UNKNOWN,
                extractionMethod = ExtractionMethod.MANUAL,
                organizationId = OrganizationId.UNKNOWN,
                visibility = Visibility.FEATURED
            ),
            predicate = Predicate(
                id = Predicates.hasPaper,
                label = "has paper",
                createdAt = OffsetDateTime.parse("2021-04-26T16:57:34.745465+02:00"),
                createdBy = ContributorId.UNKNOWN
            ),
            `object` = Literal(
                id = ThingId("L354354"),
                label = "Some literal",
                datatype = "xsd:string",
                createdAt = OffsetDateTime.parse("2022-02-22T08:01:12.709843+01:00"),
                createdBy = ContributorId("d5416c16-1a45-4aee-8069-be1b6097478b")
            ),
            createdAt = OffsetDateTime.parse("2023-02-22T08:01:15.253502+01:00"),
            createdBy = ContributorId("d5416c16-1a45-4aee-8069-be1b6097478b")
        )
    )
): PublishedContentType = PublishedContentType(rootId, subgraph)
