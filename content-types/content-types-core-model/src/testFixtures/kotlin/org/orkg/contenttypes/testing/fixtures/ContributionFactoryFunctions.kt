package org.orkg.contenttypes.testing.fixtures

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Contribution
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility

fun createDummyContribution() = Contribution(
    id = ThingId("R15634"),
    label = "Contribution",
    classes = setOf(ThingId("C123")),
    properties = mapOf(
        Predicates.hasEvaluation to listOf(ThingId("R123"))
    ),
    visibility = Visibility.DEFAULT
)
