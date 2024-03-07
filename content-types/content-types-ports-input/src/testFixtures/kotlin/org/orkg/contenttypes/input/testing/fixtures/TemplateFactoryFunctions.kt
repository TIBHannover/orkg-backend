package org.orkg.contenttypes.input.testing.fixtures

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase
import org.orkg.contenttypes.input.CreateTemplateUseCase
import org.orkg.graph.domain.FormattedLabel

fun dummyCreateTemplateCommand() = CreateTemplateUseCase.CreateCommand(
    contributorId = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    label = "Dummy Template Label",
    description = "Some description about the template",
    formattedLabel = FormattedLabel.of("{P32}"),
    targetClass = ThingId("targetClass"),
    relations = CreateTemplateUseCase.CreateCommand.Relations(
        researchFields = listOf(ThingId("R20")),
        researchProblems = listOf(ThingId("R21")),
        predicate = ThingId("P22")
    ),
    properties = listOf(
        dummyCreateLiteralTemplatePropertyCommand(),
        dummyCreateResourceTemplatePropertyCommand()
    ),
    isClosed = true,
    observatories = listOf(
        ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece"),
        ObservatoryId("73b2e081-9b50-4d55-b464-22d94e8a25f6")
    ),
    organizations = listOf(
        OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f"),
        OrganizationId("1f63b1da-3c70-4492-82e0-770ca94287ea")
    )
)

fun dummyCreateLiteralTemplatePropertyCommand() = CreateTemplatePropertyUseCase.CreateLiteralPropertyCommand(
    contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
    templateId = ThingId("R123"),
    label = "literal property label",
    placeholder = "literal property placeholder",
    description = "literal property description",
    minCount = 1,
    maxCount = 2,
    pattern = """\d+""",
    path = ThingId("P24"),
    datatype = ThingId("C25"),
)

fun dummyCreateResourceTemplatePropertyCommand() = CreateTemplatePropertyUseCase.CreateResourcePropertyCommand(
    contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
    templateId = ThingId("R123"),
    label = "resource property label",
    placeholder = "resource property placeholder",
    description = "resource property description",
    minCount = 3,
    maxCount = 4,
    pattern = """\w+""",
    path = ThingId("P27"),
    `class` = ThingId("C28")
)
