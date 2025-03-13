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
        val relations: TemplateRelationsCommand,
        val properties: List<TemplatePropertyCommand>,
        val isClosed: Boolean,
        val observatories: List<ObservatoryId>,
        val organizations: List<OrganizationId>,
        val extractionMethod: ExtractionMethod,
    )
}

interface CreateTemplatePropertyUseCase {
    fun create(command: CreateCommand): ThingId

    sealed interface CreateCommand : TemplatePropertyCommand {
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
        TemplatePropertyCommand

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
        StringLiteralTemplatePropertyCommand

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
        NumberLiteralTemplatePropertyCommand

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
        LiteralTemplatePropertyCommand

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
        ResourceTemplatePropertyCommand
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
        val relations: TemplateRelationsCommand?,
        val properties: List<TemplatePropertyCommand>?,
        val isClosed: Boolean?,
        val observatories: List<ObservatoryId>?,
        val organizations: List<OrganizationId>?,
        val extractionMethod: ExtractionMethod?,
        val visibility: Visibility?,
    )
}

interface UpdateTemplatePropertyUseCase {
    fun update(command: UpdateCommand)

    sealed interface UpdateCommand : TemplatePropertyCommand {
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
        TemplatePropertyCommand

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
        StringLiteralTemplatePropertyCommand

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
        NumberLiteralTemplatePropertyCommand

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
        LiteralTemplatePropertyCommand

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
        ResourceTemplatePropertyCommand
}

sealed interface TemplatePropertyCommand {
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

sealed interface LiteralTemplatePropertyCommand : TemplatePropertyCommand {
    val datatype: ThingId

    override fun matchesProperty(property: TemplateProperty): Boolean =
        property is LiteralTemplateProperty && datatype == property.datatype.id && super.matchesProperty(property)
}

sealed interface StringLiteralTemplatePropertyCommand : LiteralTemplatePropertyCommand {
    val pattern: String?

    override fun matchesProperty(property: TemplateProperty): Boolean =
        property is StringLiteralTemplateProperty && pattern == property.pattern && super.matchesProperty(property)
}

sealed interface NumberLiteralTemplatePropertyCommand : LiteralTemplatePropertyCommand {
    val minInclusive: RealNumber?
    val maxInclusive: RealNumber?

    override fun matchesProperty(property: TemplateProperty): Boolean =
        property is NumberLiteralTemplateProperty &&
            minInclusive == property.minInclusive &&
            maxInclusive == property.maxInclusive &&
            super.matchesProperty(property)
}

sealed interface ResourceTemplatePropertyCommand : TemplatePropertyCommand {
    val `class`: ThingId

    override fun matchesProperty(property: TemplateProperty): Boolean =
        property is ResourceTemplateProperty && `class` == property.`class`.id && super.matchesProperty(property)
}

data class UntypedPropertyCommand(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val path: ThingId,
) : TemplatePropertyCommand {
    override fun matchesProperty(property: TemplateProperty): Boolean = property is UntypedTemplateProperty && super.matchesProperty(property)
}

data class StringLiteralPropertyCommand(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val pattern: String?,
    override val path: ThingId,
    override val datatype: ThingId,
) : StringLiteralTemplatePropertyCommand

data class NumberLiteralPropertyCommand(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val minInclusive: RealNumber?,
    override val maxInclusive: RealNumber?,
    override val path: ThingId,
    override val datatype: ThingId,
) : NumberLiteralTemplatePropertyCommand

data class OtherLiteralPropertyCommand(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val path: ThingId,
    override val datatype: ThingId,
) : LiteralTemplatePropertyCommand

data class ResourcePropertyCommand(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val path: ThingId,
    override val `class`: ThingId,
) : ResourceTemplatePropertyCommand

data class TemplateRelationsCommand(
    val researchFields: List<ThingId> = emptyList(),
    val researchProblems: List<ThingId> = emptyList(),
    val predicate: ThingId? = null,
)
