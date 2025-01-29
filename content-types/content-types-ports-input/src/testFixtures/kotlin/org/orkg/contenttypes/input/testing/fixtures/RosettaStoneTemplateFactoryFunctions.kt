package org.orkg.contenttypes.input.testing.fixtures

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.RealNumber
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.CreateRosettaStoneTemplateUseCase
import org.orkg.contenttypes.input.NumberLiteralPropertyDefinition
import org.orkg.contenttypes.input.OtherLiteralPropertyDefinition
import org.orkg.contenttypes.input.ResourcePropertyDefinition
import org.orkg.contenttypes.input.StringLiteralPropertyDefinition
import org.orkg.contenttypes.input.UntypedPropertyDefinition
import org.orkg.contenttypes.input.UpdateRosettaStoneTemplateUseCase
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Predicates

fun createRosettaStoneTemplateCommand() = CreateRosettaStoneTemplateUseCase.CreateCommand(
    contributorId = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    label = "Dummy Rosetta Stone Template Label",
    description = "Some description about the rosetta stone template",
    formattedLabel = FormattedLabel.of("{0} {1} {2} {3} {4} {5}"),
    exampleUsage = "example sentence of the statement",
    properties = listOf(
        createSubjectPositionTemplatePropertyCommand(),
        createUntypedObjectPositionTemplatePropertyCommand(),
        createStringLiteralObjectPositionTemplatePropertyCommand(),
        createNumberLiteralObjectPositionTemplatePropertyCommand(),
        createOtherLiteralObjectPositionTemplatePropertyCommand(),
        createResourceObjectPositionTemplatePropertyCommand()
    ),
    observatories = listOf(
        ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece"),
        ObservatoryId("73b2e081-9b50-4d55-b464-22d94e8a25f6")
    ),
    organizations = listOf(
        OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f"),
        OrganizationId("1f63b1da-3c70-4492-82e0-770ca94287ea")
    )
)

fun updateRosettaStoneTemplateCommand() = UpdateRosettaStoneTemplateUseCase.UpdateCommand(
    templateId = ThingId("R123"),
    contributorId = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    label = "Updated dummy Rosetta Stone Template Label",
    description = "Updated description about the rosetta stone template",
    formattedLabel = FormattedLabel.of("Updated {0} {1} {2} {3} {4} {5}"),
    exampleUsage = "updated example sentence of the statement",
    properties = listOf(
        createSubjectPositionTemplatePropertyCommand(),
        createUntypedObjectPositionTemplatePropertyCommand(),
        createStringLiteralObjectPositionTemplatePropertyCommand(),
        createNumberLiteralObjectPositionTemplatePropertyCommand(),
        createOtherLiteralObjectPositionTemplatePropertyCommand(),
        createResourceObjectPositionTemplatePropertyCommand()
    ),
    observatories = listOf(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")),
    organizations = listOf(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc"))
)

fun createSubjectPositionTemplatePropertyCommand() = ResourcePropertyDefinition(
    label = "subject",
    placeholder = "subject",
    description = "subject resource property description",
    minCount = 1,
    maxCount = 0,
    path = Predicates.hasSubjectPosition,
    `class` = ThingId("C28")
)

fun createUntypedObjectPositionTemplatePropertyCommand() = UntypedPropertyDefinition(
    label = "property label",
    placeholder = "property placeholder",
    description = "property description",
    minCount = 1,
    maxCount = 2,
    path = Predicates.hasObjectPosition
)

fun createStringLiteralObjectPositionTemplatePropertyCommand() = StringLiteralPropertyDefinition(
    label = "literal property label",
    placeholder = "literal property placeholder",
    description = "literal property description",
    minCount = 1,
    maxCount = 2,
    pattern = """\d+""",
    path = Predicates.hasObjectPosition,
    datatype = Classes.string,
)

fun createNumberLiteralObjectPositionTemplatePropertyCommand() = NumberLiteralPropertyDefinition(
    label = "number literal property label",
    placeholder = "number literal property placeholder",
    description = "number literal property description",
    minCount = 1,
    maxCount = 2,
    minInclusive = RealNumber(-1),
    maxInclusive = RealNumber(10),
    path = Predicates.hasObjectPosition,
    datatype = Classes.integer,
)

fun createOtherLiteralObjectPositionTemplatePropertyCommand() = OtherLiteralPropertyDefinition(
    label = "literal property label",
    placeholder = "literal property placeholder",
    description = "literal property description",
    minCount = 1,
    maxCount = 2,
    path = Predicates.hasObjectPosition,
    datatype = ThingId("C25"),
)

fun createResourceObjectPositionTemplatePropertyCommand() = ResourcePropertyDefinition(
    label = "resource property label",
    placeholder = "resource property placeholder",
    description = "resource property description",
    minCount = 3,
    maxCount = 4,
    path = Predicates.hasObjectPosition,
    `class` = ThingId("C28")
)
