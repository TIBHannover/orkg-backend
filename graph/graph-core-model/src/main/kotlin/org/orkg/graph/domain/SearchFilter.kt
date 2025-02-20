package org.orkg.graph.domain

import org.orkg.common.ThingId
import kotlin.collections.List

typealias PredicatePath = List<ThingId>

data class SearchFilter(
    val path: PredicatePath,
    val range: ThingId,
    val values: Set<Value>,
    val exact: Boolean,
) {
    data class Value(
        val op: Operator,
        val value: String,
    )

    enum class Operator {
        EQ,
        NE,
        LT,
        GT,
        LE,
        GE,
    }
}
