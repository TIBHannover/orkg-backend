package org.orkg.graph.adapter.output.neo4j

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.ApocConfig.CompoundRelationshipFilter
import org.orkg.graph.adapter.output.neo4j.ApocConfig.LabelFilter
import org.orkg.graph.adapter.output.neo4j.ApocConfig.Uniqueness

internal class ApocConfigTest {
    @Test
    fun `Given an apoc config, when serializing to map, it returns the correct result`() {
        val config = ApocConfig(
            minLevel = 1,
            maxLevel = 10,
            uniqueness = Uniqueness.RELATIONSHIP_LEVEL,
            labelFilter = LabelFilter(
                denyLabels = setOf(ThingId("DENY"), ThingId("DENY_TOO")),
                allowLabels = setOf(ThingId("ALLOW"), ThingId("ALLOW_TOO")),
                terminationLabels = setOf(ThingId("TERMINATE"), ThingId("TERMINATE_TOO")),
                endLabels = setOf(ThingId("END"), ThingId("END_TOO")),
            ),
            relationshipFilter = CompoundRelationshipFilter(
                incomingLabels = setOf(ThingId("IN"), ThingId("IN_TOO")),
                outgoingLabels = setOf(ThingId("OUT"), ThingId("OUT_TOO")),
                undirectedLabels = setOf(ThingId("UNDIRECTED"), ThingId("UNIDIRECTED_TOO")),
            ),
            bfs = true,
        )
        val expected = mapOf<String, Any>(
            "minLevel" to 1,
            "maxLevel" to 10,
            "uniqueness" to "RELATIONSHIP_LEVEL",
            "labelFilter" to "-DENY|-DENY_TOO|+ALLOW|+ALLOW_TOO|/TERMINATE|/TERMINATE_TOO|>END|>END_TOO",
            "relationshipFilter" to "<IN|<IN_TOO|OUT>|OUT_TOO>|UNDIRECTED|UNIDIRECTED_TOO",
            "bfs" to true,
        )
        config.toMap() shouldBe expected
    }

    @Test
    fun `Given an empty apoc config, when serializing to map, it returns the correct result`() {
        val config = ApocConfig(
            minLevel = null,
            maxLevel = null,
            uniqueness = null,
            labelFilter = null,
            relationshipFilter = null,
            bfs = null,
        )
        config.toMap() shouldBe emptyMap()
    }
}
