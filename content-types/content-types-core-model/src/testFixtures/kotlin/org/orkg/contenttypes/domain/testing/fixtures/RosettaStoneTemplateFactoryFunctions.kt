package org.orkg.contenttypes.domain.testing.fixtures

import java.net.URI
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ClassReference
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.ResourceTemplateProperty
import org.orkg.contenttypes.domain.RosettaStoneTemplate
import org.orkg.contenttypes.domain.StringLiteralTemplateProperty
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility

fun createDummyRosettaStoneTemplate() = RosettaStoneTemplate(
    id = ThingId("R21325"),
    label = "Dummy Rosetta Stone Template Label",
    description = "Some description about the rosetta stone template",
    formattedLabel = FormattedLabel.of("{P32}"),
    targetClass = ThingId("targetClass"),
    properties = listOf(
        createDummySubjectPositionTemplateProperty(),
        createDummyStringLiteralObjectPositionTemplateProperty()
    ),
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

fun createDummySubjectPositionTemplateProperty() = ResourceTemplateProperty(
    id = ThingId("R27"),
    label = "resource property label",
    placeholder = "resource placeholder",
    description = "resource property description",
    order = 1,
    minCount = 1,
    maxCount = 1,
    path = ObjectIdAndLabel(Predicates.hasSubjectPosition, "has subject position"),
    createdAt = OffsetDateTime.parse("2023-11-02T15:57:25.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    `class` = ObjectIdAndLabel(ThingId("C28"), "resource property class label")
)

fun createDummyStringLiteralObjectPositionTemplateProperty() = StringLiteralTemplateProperty(
    id = ThingId("R24"),
    label = "string property label",
    placeholder = "string literal placeholder",
    description = "string literal property description",
    order = 2,
    minCount = 1,
    maxCount = 0,
    pattern = """\d+""",
    createdAt = OffsetDateTime.parse("2023-11-02T14:57:05.959539600+01:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    path = ObjectIdAndLabel(Predicates.hasObjectPosition, "has object position"),
    datatype = ClassReference(Classes.string, "string literal property class label", URI.create(Literals.XSD.STRING.uri))
)
