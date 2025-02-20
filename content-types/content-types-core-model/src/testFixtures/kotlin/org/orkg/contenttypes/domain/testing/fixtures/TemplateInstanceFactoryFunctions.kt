package org.orkg.contenttypes.domain.testing.fixtures

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.EmbeddedStatement
import org.orkg.contenttypes.domain.TemplateInstance
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createResource
import java.time.OffsetDateTime

fun createTemplateInstance() = TemplateInstance(
    root = createResource(ThingId("R54631"), classes = setOf(ThingId("targetClass"))),
    statements = mapOf(
        Predicates.field to listOf(
            EmbeddedStatement(
                thing = createLiteral(label = "untyped"),
                createdAt = OffsetDateTime.parse("2023-10-03T14:31:17.365491+01:00"),
                createdBy = ContributorId("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1"),
                statements = emptyMap()
            )
        ),
        Predicates.description to listOf(
            EmbeddedStatement(
                thing = createLiteral(label = "description"),
                createdAt = OffsetDateTime.parse("2023-10-03T14:31:17.365491+01:00"),
                createdBy = ContributorId("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1"),
                statements = emptyMap()
            )
        ),
        Predicates.hasHeadingLevel to listOf(
            EmbeddedStatement(
                thing = createLiteral(label = "5", datatype = Literals.XSD.INT.prefixedUri),
                createdAt = OffsetDateTime.parse("2023-10-03T14:31:17.365491+01:00"),
                createdBy = ContributorId("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1"),
                statements = emptyMap()
            )
        ),
        Predicates.hasWikidataId to listOf(
            EmbeddedStatement(
                thing = createLiteral(label = "5465463368674669679837"),
                createdAt = OffsetDateTime.parse("2023-10-03T14:31:17.365491+01:00"),
                createdBy = ContributorId("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1"),
                statements = emptyMap()
            )
        ),
        Predicates.hasAuthor to listOf(
            EmbeddedStatement(
                thing = createResource(classes = setOf(ThingId("R28"))),
                createdAt = OffsetDateTime.parse("2023-10-03T14:31:17.365491+01:00"),
                createdBy = ContributorId("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1"),
                statements = emptyMap()
            )
        )
    )
)

fun createNestedTemplateInstance() = TemplateInstance(
    root = createResource(ThingId("R54631"), classes = setOf(ThingId("targetClass"))),
    statements = mapOf(
        Predicates.field to listOf(
            EmbeddedStatement(
                thing = createLiteral(label = "untyped"),
                createdAt = OffsetDateTime.parse("2023-10-03T14:31:17.365491+01:00"),
                createdBy = ContributorId("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1"),
                statements = emptyMap()
            )
        ),
        Predicates.description to listOf(
            EmbeddedStatement(
                thing = createLiteral(label = "description"),
                createdAt = OffsetDateTime.parse("2023-10-03T14:31:17.365491+01:00"),
                createdBy = ContributorId("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1"),
                statements = emptyMap()
            )
        ),
        Predicates.hasHeadingLevel to listOf(
            EmbeddedStatement(
                thing = createLiteral(label = "5", datatype = Literals.XSD.INT.prefixedUri),
                createdAt = OffsetDateTime.parse("2023-10-03T14:31:17.365491+01:00"),
                createdBy = ContributorId("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1"),
                statements = emptyMap()
            )
        ),
        Predicates.hasWikidataId to listOf(
            EmbeddedStatement(
                thing = createLiteral(label = "5465463368674669679837"),
                createdAt = OffsetDateTime.parse("2023-10-03T14:31:17.365491+01:00"),
                createdBy = ContributorId("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1"),
                statements = emptyMap()
            )
        ),
        Predicates.hasAuthor to listOf(
            EmbeddedStatement(
                thing = createResource(classes = setOf(ThingId("C28"))),
                createdAt = OffsetDateTime.parse("2023-10-03T14:31:17.365491+01:00"),
                createdBy = ContributorId("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1"),
                statements = mapOf(
                    Predicates.hasDOI to listOf(
                        EmbeddedStatement(
                            thing = createLiteral(label = "10.20/123"),
                            createdAt = OffsetDateTime.parse("2023-10-03T14:31:17.365491+01:00"),
                            createdBy = ContributorId("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1"),
                            statements = emptyMap()
                        )
                    ),
                    Predicates.hasWikidataId to emptyList()
                )
            )
        )
    )
)
