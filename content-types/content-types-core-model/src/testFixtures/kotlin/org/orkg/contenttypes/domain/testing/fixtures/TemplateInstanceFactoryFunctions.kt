package org.orkg.contenttypes.domain.testing.fixtures

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.EmbeddedStatement
import org.orkg.contenttypes.domain.TemplateInstance
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createResource

fun createDummyTemplateInstance() = TemplateInstance(
    root = createResource(ThingId("R54631"), classes = setOf(ThingId("targetClass"))),
    statements = mapOf(
        Predicates.field to listOf(
            EmbeddedStatement(
                thing = createLiteral(),
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
