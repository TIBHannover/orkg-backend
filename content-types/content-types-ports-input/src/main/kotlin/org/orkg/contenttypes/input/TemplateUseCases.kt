package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.RealNumber
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.LiteralTemplateProperty
import org.orkg.contenttypes.domain.NumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.ResourceTemplateProperty
import org.orkg.contenttypes.domain.StringLiteralTemplateProperty
import org.orkg.contenttypes.domain.Template
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.UntypedTemplateProperty
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface TemplateUseCases :
    RetrieveTemplateUseCase,
    CreateTemplateUseCase,
    CreateTemplatePropertyUseCase,
    UpdateTemplateUseCase,
    UpdateTemplatePropertyUseCase

interface RetrieveTemplateUseCase {
    fun findById(id: ThingId): Optional<Template>

    fun findAll(
        label: SearchString? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
        researchField: ThingId? = null,
        includeSubfields: Boolean = false,
        researchProblem: ThingId? = null,
        targetClass: ThingId? = null,
        pageable: Pageable,
    ): Page<Template>
}

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
        val extractionMethod: ExtractionMethod,
    )
}

interface CreateTemplatePropertyUseCase {
    fun create(command: CreateCommand): ThingId

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
        override val path: ThingId,
    ) : CreateCommand,
        TemplatePropertyDefinition

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
        override val datatype: ThingId,
    ) : CreateCommand,
        StringLiteralTemplatePropertyDefinition

    data class CreateNumberLiteralPropertyCommand(
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val minInclusive: RealNumber?,
        override val maxInclusive: RealNumber?,
        override val path: ThingId,
        override val datatype: ThingId,
    ) : CreateCommand,
        NumberLiteralTemplatePropertyDefinition

    data class CreateOtherLiteralPropertyCommand(
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val path: ThingId,
        override val datatype: ThingId,
    ) : CreateCommand,
        LiteralTemplatePropertyDefinition

    data class CreateResourcePropertyCommand(
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val path: ThingId,
        override val `class`: ThingId,
    ) : CreateCommand,
        ResourceTemplatePropertyDefinition
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
        val extractionMethod: ExtractionMethod?,
        val visibility: Visibility?,
    )
}

interface UpdateTemplatePropertyUseCase {
    fun update(command: UpdateCommand)

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
    ) : UpdateCommand,
        TemplatePropertyDefinition

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
        override val datatype: ThingId,
    ) : UpdateCommand,
        StringLiteralTemplatePropertyDefinition

    data class UpdateNumberLiteralPropertyCommand(
        override val templatePropertyId: ThingId,
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val minInclusive: RealNumber?,
        override val maxInclusive: RealNumber?,
        override val path: ThingId,
        override val datatype: ThingId,
    ) : UpdateCommand,
        NumberLiteralTemplatePropertyDefinition

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
        override val datatype: ThingId,
    ) : UpdateCommand,
        LiteralTemplatePropertyDefinition

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
        override val `class`: ThingId,
    ) : UpdateCommand,
        ResourceTemplatePropertyDefinition
}

sealed interface TemplatePropertyDefinition {
    val label: String
    val placeholder: String?
    val description: String?
    val minCount: Int?
    val maxCount: Int?
    val path: ThingId

    fun matchesProperty(property: TemplateProperty): Boolean =
        label == property.label &&
            placeholder == property.placeholder &&
            description == property.description &&
            minCount == property.minCount &&
            maxCount == property.maxCount &&
            path == property.path.id
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

sealed interface NumberLiteralTemplatePropertyDefinition : LiteralTemplatePropertyDefinition {
    val minInclusive: RealNumber?
    val maxInclusive: RealNumber?

    override fun matchesProperty(property: TemplateProperty): Boolean =
        property is NumberLiteralTemplateProperty &&
            minInclusive == property.minInclusive &&
            maxInclusive == property.maxInclusive &&
            super.matchesProperty(property)
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
    override fun matchesProperty(property: TemplateProperty): Boolean = property is UntypedTemplateProperty && super.matchesProperty(property)
}

data class StringLiteralPropertyDefinition(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val pattern: String?,
    override val path: ThingId,
    override val datatype: ThingId,
) : StringLiteralTemplatePropertyDefinition

data class NumberLiteralPropertyDefinition(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val minInclusive: RealNumber?,
    override val maxInclusive: RealNumber?,
    override val path: ThingId,
    override val datatype: ThingId,
) : NumberLiteralTemplatePropertyDefinition

data class OtherLiteralPropertyDefinition(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val path: ThingId,
    override val datatype: ThingId,
) : LiteralTemplatePropertyDefinition

data class ResourcePropertyDefinition(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val path: ThingId,
    override val `class`: ThingId,
) : ResourceTemplatePropertyDefinition

data class TemplateRelationsDefinition(
    val researchFields: List<ThingId> = emptyList(),
    val researchProblems: List<ThingId> = emptyList(),
    val predicate: ThingId? = null,
)
