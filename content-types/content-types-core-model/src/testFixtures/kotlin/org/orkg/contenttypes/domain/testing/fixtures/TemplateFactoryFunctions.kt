package org.orkg.contenttypes.domain.testing.fixtures

import java.net.URI
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ClassReference
import org.orkg.contenttypes.domain.NumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.OtherLiteralTemplateProperty
import org.orkg.contenttypes.domain.ResourceTemplateProperty
import org.orkg.contenttypes.domain.StringLiteralTemplateProperty
import org.orkg.contenttypes.domain.Template
import org.orkg.contenttypes.domain.TemplateRelations
import org.orkg.contenttypes.domain.UntypedTemplateProperty
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility

fun createDummyTemplate() = Template(
    id = ThingId("R54631"),
    label = "Dummy Template Label",
    description = "Some description about the template",
    formattedLabel = FormattedLabel.of("{P32}"),
    targetClass = ClassReference(
        id = ThingId("targetClass"),
        label = "Target Class",
        uri = URI("https://orkg.org/class/targetClass")
    ),
    relations = TemplateRelations(
        researchFields = listOf(ObjectIdAndLabel(ThingId("R20"), "Research Field 1")),
        researchProblems = listOf(ObjectIdAndLabel(ThingId("R21"), "Research Problem 1")),
        predicate = ObjectIdAndLabel(ThingId("P22"), "Predicate label")
    ),
    properties = listOf(
        createDummyUntypedTemplateProperty(),
        createDummyStringLiteralTemplateProperty(),
        createDummyNumberLiteralTemplateProperty(),
        createDummyOtherLiteralTemplateProperty(),
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

fun createDummyUntypedTemplateProperty() = UntypedTemplateProperty(
    id = ThingId("R23"),
    label = "property label",
    placeholder = "property placeholder",
    description = "property description",
    order = 1,
    minCount = 1,
    maxCount = 2,
    createdAt = OffsetDateTime.parse("2023-11-02T14:57:05.959539600+01:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    path = ObjectIdAndLabel(Predicates.field, "property path label"),
)

fun createDummyStringLiteralTemplateProperty() = StringLiteralTemplateProperty(
    id = ThingId("R24"),
    label = "string property label",
    placeholder = "string literal placeholder",
    description = "string literal property description",
    order = 2,
    minCount = 1,
    maxCount = 2,
    pattern = """\d+""",
    createdAt = OffsetDateTime.parse("2023-11-02T14:57:05.959539600+01:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    path = ObjectIdAndLabel(Predicates.description, "string literal property path label"),
    datatype = ObjectIdAndLabel(Classes.string, "string literal property class label")
)

fun createDummyNumberLiteralTemplateProperty() = NumberLiteralTemplateProperty(
    id = ThingId("R25"),
    label = "number property label",
    placeholder = "number literal placeholder",
    description = "number literal property description",
    order = 3,
    minCount = 1,
    maxCount = 2,
    minInclusive = -1,
    maxInclusive = 10,
    createdAt = OffsetDateTime.parse("2023-11-02T14:57:05.959539600+01:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    path = ObjectIdAndLabel(Predicates.hasHeadingLevel, "number literal property path label"),
    datatype = ObjectIdAndLabel(Classes.integer, "number literal property class label")
)

fun createDummyFloatLiteralTemplateProperty() = NumberLiteralTemplateProperty(
    id = ThingId("R25"),
    label = "float property label",
    placeholder = "float literal placeholder",
    description = "float literal property description",
    order = 3,
    minCount = 1,
    maxCount = 2,
    minInclusive = -1.0F,
    maxInclusive = 10.0F,
    createdAt = OffsetDateTime.parse("2023-11-02T14:57:05.959539600+01:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    path = ObjectIdAndLabel(Predicates.hasHeadingLevel, "float literal property path label"),
    datatype = ObjectIdAndLabel(Classes.float, "float literal property class label")
)

fun createDummyDecimalLiteralTemplateProperty(): NumberLiteralTemplateProperty<Double> = NumberLiteralTemplateProperty(
    id = ThingId("R25"),
    label = "decimal property label",
    placeholder = "decimal literal placeholder",
    description = "decimal literal property description",
    order = 3,
    minCount = 1,
    maxCount = 2,
    minInclusive = -1.0,
    maxInclusive = 10.0,
    createdAt = OffsetDateTime.parse("2023-11-02T14:57:05.959539600+01:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    path = ObjectIdAndLabel(Predicates.hasHeadingLevel, "decimal literal property path label"),
    datatype = ObjectIdAndLabel(Classes.decimal, "decimal literal property class label")
)

fun createDummyOtherLiteralTemplateProperty() = OtherLiteralTemplateProperty(
    id = ThingId("R26"),
    label = "property label",
    placeholder = "literal placeholder",
    description = "literal property description",
    order = 4,
    minCount = 1,
    maxCount = 2,
    createdAt = OffsetDateTime.parse("2023-11-02T14:57:05.959539600+01:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    path = ObjectIdAndLabel(Predicates.hasWikidataId, "literal property path label"),
    datatype = ObjectIdAndLabel(Classes.integer, "literal property class label")
)

fun createDummyResourceTemplateProperty() = ResourceTemplateProperty(
    id = ThingId("R27"),
    label = "resource property label",
    placeholder = "resource placeholder",
    description = "resource property description",
    order = 5,
    minCount = 3,
    maxCount = 4,
    path = ObjectIdAndLabel(Predicates.hasAuthor, "resource property path label"),
    createdAt = OffsetDateTime.parse("2023-11-02T15:57:25.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    `class` = ObjectIdAndLabel(ThingId("C28"), "resource property class label")
)
