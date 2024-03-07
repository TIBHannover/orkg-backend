package org.orkg.graph.domain

import kotlin.collections.List
import org.orkg.common.ThingId

typealias PredicatePath = List<ThingId>

data class SearchFilter(
    val path: PredicatePath,
    val range: ThingId,
    val values: Set<Value>,
    val exact: Boolean
) {
    data class Value(
        val op: Operator,
        val value: String
    )

    enum class Operator {
        EQ, NE, LT, GT, LE, GE
    }
}
