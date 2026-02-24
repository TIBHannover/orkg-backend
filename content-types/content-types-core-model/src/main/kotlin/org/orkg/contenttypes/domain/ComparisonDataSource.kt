package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicates

data class ComparisonDataSource(
    val id: ThingId,
    val type: Type,
) {
    enum class Type(val predicateId: ThingId) {
        THING(Predicates.comparesContribution),
        ROSETTA_STONE_STATEMENT(Predicates.comparesRosettaStoneContribution),
    }
}
