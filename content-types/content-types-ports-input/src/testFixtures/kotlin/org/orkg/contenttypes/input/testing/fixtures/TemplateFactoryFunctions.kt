package org.orkg.contenttypes.input.testing.fixtures

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.RealNumber
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.NumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.OtherLiteralTemplateProperty
import org.orkg.contenttypes.domain.ResourceTemplateProperty
import org.orkg.contenttypes.domain.StringLiteralTemplateProperty
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.UntypedTemplateProperty
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase
import org.orkg.contenttypes.input.CreateTemplateUseCase
import org.orkg.contenttypes.input.NumberLiteralPropertyDefinition
import org.orkg.contenttypes.input.OtherLiteralPropertyDefinition
import org.orkg.contenttypes.input.ResourcePropertyDefinition
import org.orkg.contenttypes.input.StringLiteralPropertyDefinition
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.contenttypes.input.TemplateRelationsDefinition
import org.orkg.contenttypes.input.UntypedPropertyDefinition
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase
import org.orkg.contenttypes.input.UpdateTemplateUseCase
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility

fun createTemplateCommand() = CreateTemplateUseCase.CreateCommand(
    contributorId = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    label = "Dummy Template Label",
    description = "Some description about the template",
    formattedLabel = FormattedLabel.of("{P32}"),
    targetClass = ThingId("targetClass"),
    relations = createTemplateRelations(),
    properties = listOf(
        createUntypedTemplatePropertyCommand(),
        createStringLiteralTemplatePropertyCommand(),
        createNumberLiteralTemplatePropertyCommand(),
        createOtherLiteralTemplatePropertyCommand(),
        createResourceTemplatePropertyCommand()
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
    extractionMethod = ExtractionMethod.MANUAL
)

fun updateTemplateCommand() = UpdateTemplateUseCase.UpdateCommand(
    templateId = ThingId("R123"),
    contributorId = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    label = "Updated dummy Template Label",
    description = "Updated description about the template",
    formattedLabel = FormattedLabel.of("{P34}"),
    targetClass = ThingId("otherClass"),
    relations = createTemplateRelations(
        researchFields = listOf(ThingId("R24")),
        researchProblems = listOf(ThingId("R29")),
        predicate = ThingId("P23")
    ),
    properties = listOf(
        updateUntypedTemplatePropertyCommand(),
        updateStringLiteralTemplatePropertyCommand(),
        updateNumberLiteralTemplatePropertyCommand(),
        updateOtherLiteralTemplatePropertyCommand(),
        updateResourceTemplatePropertyCommand()
    ),
    isClosed = true,
    observatories = listOf(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")),
    organizations = listOf(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")),
    extractionMethod = ExtractionMethod.MANUAL,
    visibility = Visibility.DEFAULT
)

fun createUntypedTemplatePropertyCommand() = CreateTemplatePropertyUseCase.CreateUntypedPropertyCommand(
    contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
    templateId = ThingId("R123"),
    label = "property label",
    placeholder = "property placeholder",
    description = "property description",
    minCount = 1,
    maxCount = 2,
    path = Predicates.field
)

fun createStringLiteralTemplatePropertyCommand() = CreateTemplatePropertyUseCase.CreateStringLiteralPropertyCommand(
    contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
    templateId = ThingId("R123"),
    label = "literal property label",
    placeholder = "literal property placeholder",
    description = "literal property description",
    minCount = 1,
    maxCount = 2,
    pattern = """\d+""",
    path = Predicates.field,
    datatype = Classes.string,
)

fun createNumberLiteralTemplatePropertyCommand() = CreateTemplatePropertyUseCase.CreateNumberLiteralPropertyCommand(
    contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
    templateId = ThingId("R123"),
    label = "number literal property label",
    placeholder = "number literal property placeholder",
    description = "number literal property description",
    minCount = 1,
    maxCount = 2,
    minInclusive = RealNumber(-1),
    maxInclusive = RealNumber(10),
    path = Predicates.field,
    datatype = Classes.integer,
)

fun createOtherLiteralTemplatePropertyCommand() = CreateTemplatePropertyUseCase.CreateOtherLiteralPropertyCommand(
    contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
    templateId = ThingId("R123"),
    label = "literal property label",
    placeholder = "literal property placeholder",
    description = "literal property description",
    minCount = 1,
    maxCount = 2,
    path = Predicates.field,
    datatype = ThingId("C25"),
)

fun createResourceTemplatePropertyCommand() = CreateTemplatePropertyUseCase.CreateResourcePropertyCommand(
    contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
    templateId = ThingId("R123"),
    label = "resource property label",
    placeholder = "resource property placeholder",
    description = "resource property description",
    minCount = 3,
    maxCount = 4,
    path = Predicates.hasAuthor,
    `class` = ThingId("C28")
)

fun updateUntypedTemplatePropertyCommand() = UpdateTemplatePropertyUseCase.UpdateUntypedPropertyCommand(
    templatePropertyId = ThingId("R23"),
    contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
    templateId = ThingId("R123"),
    label = "updated property label",
    placeholder = "updated property placeholder",
    description = "updated property description",
    minCount = 0,
    maxCount = 1,
    path = Predicates.description
)

fun updateStringLiteralTemplatePropertyCommand() = UpdateTemplatePropertyUseCase.UpdateStringLiteralPropertyCommand(
    templatePropertyId = ThingId("R24"),
    contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
    templateId = ThingId("R123"),
    label = "updated string literal property label",
    placeholder = "updated string literal property placeholder",
    description = "updated string literal property description",
    minCount = 0,
    maxCount = 1,
    pattern = """\w+""",
    path = Predicates.description,
    datatype = Classes.string,
)

fun updateNumberLiteralTemplatePropertyCommand() = UpdateTemplatePropertyUseCase.UpdateNumberLiteralPropertyCommand(
    templatePropertyId = ThingId("R25"),
    contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
    templateId = ThingId("R123"),
    label = "updated literal property label",
    placeholder = "updated literal property placeholder",
    description = "updated literal property description",
    minCount = 0,
    maxCount = 1,
    minInclusive = RealNumber(2),
    maxInclusive = RealNumber(5),
    path = Predicates.description,
    datatype = Classes.decimal,
)

fun updateOtherLiteralTemplatePropertyCommand() = UpdateTemplatePropertyUseCase.UpdateOtherLiteralPropertyCommand(
    templatePropertyId = ThingId("R26"),
    contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
    templateId = ThingId("R123"),
    label = "updated literal property label",
    placeholder = "updated literal property placeholder",
    description = "updated literal property description",
    minCount = 0,
    maxCount = 1,
    path = Predicates.description,
    datatype = Classes.string,
)

fun updateResourceTemplatePropertyCommand() = UpdateTemplatePropertyUseCase.UpdateResourcePropertyCommand(
    templatePropertyId = ThingId("R27"),
    contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
    templateId = ThingId("R123"),
    label = "updated resource property label",
    placeholder = "updated resource property placeholder",
    description = "updated resource property description",
    minCount = 2,
    maxCount = 3,
    path = Predicates.hasPaper,
    `class` = Classes.paper
)

fun createTemplateRelations(
    researchFields: List<ThingId> = listOf(ThingId("R20")),
    researchProblems: List<ThingId> = listOf(ThingId("R21")),
    predicate: ThingId? = ThingId("P22")
): TemplateRelationsDefinition =
    TemplateRelationsDefinition(researchFields, researchProblems, predicate)

fun TemplateProperty.toTemplatePropertyDefinition(): TemplatePropertyDefinition =
    when (this) {
        is UntypedTemplateProperty -> toUntypedTemplatePropertyDefinition()
        is StringLiteralTemplateProperty -> toStringLiteralTemplatePropertyDefinition()
        is NumberLiteralTemplateProperty -> toNumberLiteralTemplatePropertyDefinition()
        is OtherLiteralTemplateProperty -> toOtherLiteralTemplatePropertyDefinition()
        is ResourceTemplateProperty -> toResourceTemplatePropertyDefinition()
    }

fun UntypedTemplateProperty.toUntypedTemplatePropertyDefinition(): UntypedPropertyDefinition =
    UntypedPropertyDefinition(label, placeholder, description, minCount, maxCount, path.id)

fun StringLiteralTemplateProperty.toStringLiteralTemplatePropertyDefinition(): StringLiteralPropertyDefinition =
    StringLiteralPropertyDefinition(label, placeholder, description, minCount, maxCount, pattern, path.id, datatype.id)

fun NumberLiteralTemplateProperty.toNumberLiteralTemplatePropertyDefinition(): NumberLiteralPropertyDefinition =
    NumberLiteralPropertyDefinition(label, placeholder, description, minCount, maxCount, minInclusive, maxInclusive, path.id, datatype.id)

fun OtherLiteralTemplateProperty.toOtherLiteralTemplatePropertyDefinition(): OtherLiteralPropertyDefinition =
    OtherLiteralPropertyDefinition(label, placeholder, description, minCount, maxCount, path.id, datatype.id)

fun ResourceTemplateProperty.toResourceTemplatePropertyDefinition(): ResourcePropertyDefinition =
    ResourcePropertyDefinition(label, placeholder, description, minCount, maxCount, path.id, `class`.id)
