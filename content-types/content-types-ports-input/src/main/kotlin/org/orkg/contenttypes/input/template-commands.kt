package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.LiteralTemplateProperty
import org.orkg.contenttypes.domain.NumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.ResourceTemplateProperty
import org.orkg.contenttypes.domain.StringLiteralTemplateProperty
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.UntypedTemplateProperty
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.FormattedLabel

interface CreateTemplateUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val contributorId: ContributorId,
        val label: String,
        val description: String?,
        val formattedLabel: FormattedLabel?,
        val targetClass: ThingId,
        val relations: TemplateRelationsDefinition,
        val properties: List<TemplatePropertyDefinition>,
        val isClosed: Boolean,
        val observatories: List<ObservatoryId>,
        val organizations: List<OrganizationId>,
        val extractionMethod: ExtractionMethod
    )
}

interface CreateTemplatePropertyUseCase {
    fun createTemplateProperty(command: CreateCommand): ThingId

    sealed interface CreateCommand : TemplatePropertyDefinition {
        val contributorId: ContributorId
        val templateId: ThingId
    }

    data class CreateUntypedPropertyCommand(
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val path: ThingId
    ) : CreateCommand, TemplatePropertyDefinition

    data class CreateStringLiteralPropertyCommand(
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val pattern: String?,
        override val path: ThingId,
        override val datatype: ThingId
    ) : CreateCommand, StringLiteralTemplatePropertyDefinition

    data class CreateNumberLiteralPropertyCommand<T : Number>(
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val minInclusive: T?,
        override val maxInclusive: T?,
        override val path: ThingId,
        override val datatype: ThingId
    ) : CreateCommand, NumberLiteralTemplatePropertyDefinition<T>

    data class CreateOtherLiteralPropertyCommand(
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val path: ThingId,
        override val datatype: ThingId
    ) : CreateCommand, LiteralTemplatePropertyDefinition

    data class CreateResourcePropertyCommand(
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val path: ThingId,
        override val `class`: ThingId
    ) : CreateCommand, ResourceTemplatePropertyDefinition
}

interface UpdateTemplateUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val templateId: ThingId,
        val contributorId: ContributorId,
        val label: String?,
        val description: String?,
        val formattedLabel: FormattedLabel?,
        val targetClass: ThingId?,
        val relations: TemplateRelationsDefinition?,
        val properties: List<TemplatePropertyDefinition>?,
        val isClosed: Boolean?,
        val observatories: List<ObservatoryId>?,
        val organizations: List<OrganizationId>?,
        val extractionMethod: ExtractionMethod?
    )
}

interface UpdateTemplatePropertyUseCase {
    fun updateTemplateProperty(command: UpdateCommand)

    sealed interface UpdateCommand : TemplatePropertyDefinition {
        val templatePropertyId: ThingId
        val contributorId: ContributorId
        val templateId: ThingId
    }

    data class UpdateUntypedPropertyCommand(
        override val templatePropertyId: ThingId,
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val path: ThingId,
    ) : UpdateCommand, TemplatePropertyDefinition

    data class UpdateStringLiteralPropertyCommand(
        override val templatePropertyId: ThingId,
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val pattern: String?,
        override val path: ThingId,
        override val datatype: ThingId
    ) : UpdateCommand, StringLiteralTemplatePropertyDefinition

    data class UpdateNumberLiteralPropertyCommand<T : Number>(
        override val templatePropertyId: ThingId,
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val minInclusive: T?,
        override val maxInclusive: T?,
        override val path: ThingId,
        override val datatype: ThingId
    ) : UpdateCommand, NumberLiteralTemplatePropertyDefinition<T>

    data class UpdateOtherLiteralPropertyCommand(
        override val templatePropertyId: ThingId,
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val path: ThingId,
        override val datatype: ThingId
    ) : UpdateCommand, LiteralTemplatePropertyDefinition

    data class UpdateResourcePropertyCommand(
        override val templatePropertyId: ThingId,
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val path: ThingId,
        override val `class`: ThingId
    ) : UpdateCommand, ResourceTemplatePropertyDefinition
}

sealed interface TemplatePropertyDefinition {
    val label: String
    val placeholder: String?
    val description: String?
    val minCount: Int?
    val maxCount: Int?
    val path: ThingId

    fun matchesProperty(property: TemplateProperty): Boolean =
        label == property.label && placeholder == property.placeholder && description == property.description &&
            minCount == property.minCount && maxCount == property.maxCount && path == property.path.id
}

sealed interface LiteralTemplatePropertyDefinition : TemplatePropertyDefinition {
    val datatype: ThingId

    override fun matchesProperty(property: TemplateProperty): Boolean =
        property is LiteralTemplateProperty && datatype == property.datatype.id && super.matchesProperty(property)
}

sealed interface StringLiteralTemplatePropertyDefinition : LiteralTemplatePropertyDefinition {
    val pattern: String?

    override fun matchesProperty(property: TemplateProperty): Boolean =
        property is StringLiteralTemplateProperty && pattern == property.pattern && super.matchesProperty(property)
}

sealed interface NumberLiteralTemplatePropertyDefinition<T : Number> : LiteralTemplatePropertyDefinition {
    val minInclusive: T?
    val maxInclusive: T?

    override fun matchesProperty(property: TemplateProperty): Boolean =
        property is NumberLiteralTemplateProperty<*> && minInclusive == property.minInclusive &&
            maxInclusive == property.maxInclusive && super.matchesProperty(property)
}

sealed interface ResourceTemplatePropertyDefinition : TemplatePropertyDefinition {
    val `class`: ThingId

    override fun matchesProperty(property: TemplateProperty): Boolean =
        property is ResourceTemplateProperty && `class` == property.`class`.id && super.matchesProperty(property)
}

data class UntypedPropertyDefinition(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val path: ThingId,
) : TemplatePropertyDefinition {
    override fun matchesProperty(property: TemplateProperty): Boolean {
        return property is UntypedTemplateProperty && super.matchesProperty(property)
    }
}

data class StringLiteralPropertyDefinition(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val pattern: String?,
    override val path: ThingId,
    override val datatype: ThingId
) : StringLiteralTemplatePropertyDefinition

data class NumberLiteralPropertyDefinition<T : Number>(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val minInclusive: T?,
    override val maxInclusive: T?,
    override val path: ThingId,
    override val datatype: ThingId
) : NumberLiteralTemplatePropertyDefinition<T>

data class OtherLiteralPropertyDefinition(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val path: ThingId,
    override val datatype: ThingId
) : LiteralTemplatePropertyDefinition

data class ResourcePropertyDefinition(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val path: ThingId,
    override val `class`: ThingId
) : ResourceTemplatePropertyDefinition

data class TemplateRelationsDefinition(
    val researchFields: List<ThingId> = emptyList(),
    val researchProblems: List<ThingId> = emptyList(),
    val predicate: ThingId? = null
)
