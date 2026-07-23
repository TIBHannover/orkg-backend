package org.orkg.graph.adapter.output.neo4j

import org.orkg.common.ThingId

class ApocConfig(
    val minLevel: Int? = null,
    val maxLevel: Int? = null,
    val uniqueness: Uniqueness? = Uniqueness.RELATIONSHIP_GLOBAL,
    val labelFilter: LabelFilter? = null,
    val relationshipFilter: RelationshipFilter? = CompoundRelationshipFilter.RELATED_OUTGOING,
    val bfs: Boolean? = true,
) {
    fun toMap(): Map<String, Any> = buildMap {
        minLevel?.also { this["minLevel"] = it }
        maxLevel?.also { this["maxLevel"] = it }
        uniqueness?.also { this["uniqueness"] = it.toString() }
        labelFilter?.asString()?.also { this["labelFilter"] = it }
        relationshipFilter?.asString()?.also { this["relationshipFilter"] = it }
        bfs?.also { this["bfs"] = it }
    }

    data class LabelFilter(
        val denyLabels: Set<ThingId> = emptySet(),
        val allowLabels: Set<ThingId> = emptySet(),
        val terminationLabels: Set<ThingId> = emptySet(),
        val endLabels: Set<ThingId> = emptySet(),
    ) {
        fun asString() = setOf(
            *denyLabels.map { "-$it" }.toTypedArray(),
            *allowLabels.map { "+$it" }.toTypedArray(),
            *terminationLabels.map { "/$it" }.toTypedArray(),
            *endLabels.map { ">$it" }.toTypedArray(),
        ).let {
            if (it.isEmpty()) null else it.joinToString("|")
        }
    }

    sealed interface RelationshipFilter {
        fun asString(): String?
    }

    @Suppress("unused")
    object AllIncomingRelationShipFilter : RelationshipFilter {
        override fun asString() = "<"
    }

    @Suppress("unused")
    object AllOutgoingRelationShipFilter : RelationshipFilter {
        override fun asString() = ">"
    }

    data class CompoundRelationshipFilter(
        val incomingLabels: Set<ThingId> = emptySet(),
        val outgoingLabels: Set<ThingId> = emptySet(),
        val undirectedLabels: Set<ThingId> = emptySet(),
    ) : RelationshipFilter {
        override fun asString() = setOf(
            *incomingLabels.map { "<$it" }.toTypedArray(),
            *outgoingLabels.map { "$it>" }.toTypedArray(),
            *undirectedLabels.toTypedArray(),
        ).let {
            if (it.isEmpty()) null else it.joinToString("|")
        }

        companion object {
            val RELATED_OUTGOING = CompoundRelationshipFilter(outgoingLabels = setOf(ThingId("RELATED")))
        }
    }

    @Suppress("unused")
    enum class Uniqueness {
        RELATIONSHIP_PATH,
        NODE_GLOBAL,
        NODE_LEVEL,
        NODE_PATH,
        NODE_RECENT,
        RELATIONSHIP_GLOBAL,
        RELATIONSHIP_LEVEL,
        RELATIONSHIP_RECENT,
        NONE,
    }
}
