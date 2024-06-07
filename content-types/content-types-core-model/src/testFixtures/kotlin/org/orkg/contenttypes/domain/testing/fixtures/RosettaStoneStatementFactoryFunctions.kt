package org.orkg.contenttypes.domain.testing.fixtures

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Certainty
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.domain.RosettaStoneStatementVersion
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Visibility
import org.orkg.graph.testing.fixtures.createResource

fun createDummyRosettaStoneStatement() = RosettaStoneStatement(
    id = ThingId("R123"),
    contextId = ThingId("R789"),
    templateId = ThingId("R456"),
    templateTargetClassId = ThingId("R321"),
    label = "Dummy Rosetta Stone Statement Label",
    versions = listOf(
        createDummyRosettaStoneStatementVersion()
    ),
    observatories = listOf(
        ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
    ),
    organizations = listOf(
        OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
    ),
    extractionMethod = ExtractionMethod.MANUAL,
    visibility = Visibility.DEFAULT,
    modifiable = true,
    unlistedBy = null
)

fun createDummyRosettaStoneStatementVersion() = RosettaStoneStatementVersion(
    id = ThingId("R147"),
    formattedLabel = FormattedLabel.of("{0} {1} {2}"),
    subjects = listOf(
        createResource(id = ThingId("R258")),
        createResource(id = ThingId("R369"))
    ),
    objects = listOf(
        listOf(
            createResource(id = ThingId("R987")),
            createResource(id = ThingId("R654"))
        ),
        listOf(
            createResource(id = ThingId("R321")),
            createResource(id = ThingId("R741"))
        )
    ),
    createdAt = OffsetDateTime.parse("2024-04-30T16:22:58.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    certainty = Certainty.HIGH,
    negated = false,
    observatories = listOf(
        ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
    ),
    organizations = listOf(
        OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
    ),
    extractionMethod = ExtractionMethod.MANUAL,
    visibility = Visibility.DEFAULT,
    modifiable = true,
    unlistedBy = null
)
