package org.orkg.contenttypes.domain.testing.fixtures

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Contribution
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility

fun createDummyContribution() = Contribution(
    id = ThingId("R15634"),
    label = "Contribution",
    classes = setOf(ThingId("C123")),
    properties = mapOf(
        ThingId("R456") to listOf(
            ThingId("R789"),
            ThingId("R147")
        )
    ),
    extractionMethod = ExtractionMethod.MANUAL,
    createdAt = OffsetDateTime.parse("2023-11-02T10:25:05.959539600+01:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    visibility = Visibility.DEFAULT
)
