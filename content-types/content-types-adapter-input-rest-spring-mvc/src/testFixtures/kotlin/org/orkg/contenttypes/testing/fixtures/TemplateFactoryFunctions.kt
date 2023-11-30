package org.orkg.contenttypes.testing.fixtures

import org.orkg.contenttypes.domain.LiteralTemplateProperty
import org.orkg.contenttypes.domain.ResourceTemplateProperty
import org.orkg.contenttypes.domain.Template
import org.orkg.contenttypes.domain.TemplateRelation
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Visibility

fun createDummyTemplate() = Template(
    id = ThingId("R54631"),
    label = "Dummy Template Label",
    description = "Some description about the template",
    formattedLabel = FormattedLabel.of("{P32}"),
    targetClass = ThingId("targetClass"),
    relations = TemplateRelation(
        researchFields = listOf(ObjectIdAndLabel(ThingId("R20"), "Research Field 1")),
        researchProblems = listOf(ObjectIdAndLabel(ThingId("R21"), "Research Problem 1")),
        predicate = ObjectIdAndLabel(ThingId("P22"), "Predicate label")
    ),
    properties = listOf(
        createDummyLiteralTemplateProperty(),
        createDummyResourceTemplateProperty()
    ),
    isClosed = true,
    observatories = listOf(
        ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece"),
        ObservatoryId("73b2e081-9b50-4d55-b464-22d94e8a25f6")
    ),
    organizations = listOf(
        OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f"),
        OrganizationId("1f63b1da-3c70-4492-82e0-770ca94287ea")
    ),
    createdAt = OffsetDateTime.parse("2023-04-12T16:05:05.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    visibility = Visibility.DEFAULT
)

fun createDummyLiteralTemplateProperty() = LiteralTemplateProperty(
    id = ThingId("R23"),
    label = "property label",
    order = 1,
    minCount = 1,
    maxCount = 2,
    pattern = """\d+""",
    path = ObjectIdAndLabel(ThingId("R24"), "literal property path label"),
    createdAt = OffsetDateTime.parse("2023-11-02T14:57:05.959539600+01:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    datatype = ObjectIdAndLabel(ThingId("R25"), "literal property class label")
)

fun createDummyResourceTemplateProperty() = ResourceTemplateProperty(
    id = ThingId("R26"),
    label = "property label",
    order = 2,
    minCount = 3,
    maxCount = 4,
    pattern = """\w+""",
    path = ObjectIdAndLabel(ThingId("R27"), "resource property path label"),
    createdAt = OffsetDateTime.parse("2023-11-02T15:57:25.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    `class` = ObjectIdAndLabel(ThingId("R28"), "resource property class label")
)
