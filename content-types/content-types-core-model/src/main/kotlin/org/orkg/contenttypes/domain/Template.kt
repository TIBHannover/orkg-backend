package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility

data class Template(
    val id: ThingId,
    val label: String,
    val description: String?,
    val formattedLabel: FormattedLabel?,
    val targetClass: ClassReference,
    val relations: TemplateRelations,
    val properties: List<TemplateProperty>,
    val isClosed: Boolean,
    val createdBy: ContributorId,
    val createdAt: OffsetDateTime,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val visibility: Visibility,
    val unlistedBy: ContributorId? = null
) : ContentType {
    companion object {
        fun from(resource: Resource, statements: Map<ThingId, List<GeneralStatement>>): Template {
            val directStatements = statements[resource.id]!!
            return Template(
                id = resource.id,
                label = resource.label,
                description = directStatements
                    .wherePredicate(Predicates.description)
                    .singleOrNull()?.`object`?.label,
                formattedLabel = directStatements
                    .wherePredicate(Predicates.templateLabelFormat)
                    .singleOrNull()
                    ?.let { FormattedLabel.of(it.`object`.label) },
                targetClass = directStatements
                    .wherePredicate(Predicates.shTargetClass)
                    .single { it.`object` is Class }
                    .let { ClassReference(it.`object` as Class) },
                relations = TemplateRelations.from(resource, statements),
                properties = directStatements
                    .wherePredicate(Predicates.shProperty)
                    .filter { it.`object` is Resource && Classes.propertyShape in (it.`object` as Resource).classes }
                    .mapNotNull { TemplateProperty.from(it.`object` as Resource, statements[it.`object`.id].orEmpty()) }
                    .sortedBy { it.order },
                isClosed = directStatements
                    .wherePredicate(Predicates.shClosed)
                    .singleOrNull()
                    .let { it?.`object`?.label.toBoolean() },
                createdAt = resource.createdAt,
                createdBy = resource.createdBy,
                organizations = listOf(resource.organizationId),
                observatories = listOf(resource.observatoryId),
                visibility = resource.visibility,
                unlistedBy = resource.unlistedBy
            )
        }
    }
}

data class TemplateRelations(
    val researchFields: List<ObjectIdAndLabel> = emptyList(),
    val researchProblems: List<ObjectIdAndLabel> = emptyList(),
    val predicate: ObjectIdAndLabel? = null,
) {
    companion object {
        fun from(resource: Resource, statements: Map<ThingId, List<GeneralStatement>>): TemplateRelations {
            val directStatements = statements[resource.id]!!
            return TemplateRelations(
                researchFields = directStatements
                    .wherePredicate(Predicates.templateOfResearchField)
                    .objectIdsAndLabel(),
                researchProblems = directStatements
                    .wherePredicate(Predicates.templateOfResearchProblem)
                    .objectIdsAndLabel(),
                predicate = directStatements
                    .wherePredicate(Predicates.templateOfPredicate)
                    .singleOrNull()
                    ?.objectIdAndLabel()
            )
        }
    }
}

sealed interface TemplateProperty {
    val id: ThingId
    val label: String
    val placeholder: String?
    val description: String?
    val order: Long
    val minCount: Int?
    val maxCount: Int?
    val path: ObjectIdAndLabel
    val createdBy: ContributorId
    val createdAt: OffsetDateTime

    companion object {
        fun from(resource: Resource, statements: Iterable<GeneralStatement>): TemplateProperty? {
            val placeholder = statements.wherePredicate(Predicates.placeholder).singleOrNull()?.`object`?.label
            val description = statements.wherePredicate(Predicates.description).singleOrNull()?.`object`?.label
            val order = statements.wherePredicate(Predicates.shOrder).singleOrNull()?.`object`?.label?.toLong()
            val minCount = statements.wherePredicate(Predicates.shMinCount).singleOrNull()?.`object`?.label?.toInt()
            val maxCount = statements.wherePredicate(Predicates.shMaxCount).singleOrNull()?.`object`?.label?.toInt()
            val path = statements.wherePredicate(Predicates.shPath).singleOrNull()?.objectIdAndLabel()
            val datatype = statements.wherePredicate(Predicates.shDatatype).singleOrNull()
                ?.takeIf { it.`object` is Class }
                ?.let { ClassReference(it.`object` as Class) }
            val `class` = statements.wherePredicate(Predicates.shClass).singleOrNull()?.objectIdAndLabel()
            return when {
                order == null || path == null -> null
                datatype != null -> {
                    when (datatype.id) {
                        Classes.string -> StringLiteralTemplateProperty(
                            id = resource.id,
                            label = resource.label,
                            placeholder = placeholder,
                            description = description,
                            order = order,
                            minCount = minCount,
                            maxCount = maxCount,
                            pattern = statements.wherePredicate(Predicates.shPattern).singleOrNull()?.`object`?.label,
                            path = path,
                            createdBy = resource.createdBy,
                            createdAt = resource.createdAt,
                            datatype = datatype
                        )
                        Classes.integer -> NumberLiteralTemplateProperty(
                            id = resource.id,
                            label = resource.label,
                            placeholder = placeholder,
                            description = description,
                            order = order,
                            minCount = minCount,
                            maxCount = maxCount,
                            minInclusive = statements.wherePredicate(Predicates.shMinInclusive)
                                .singleOrNull()?.`object`?.label?.toIntOrNull(),
                            maxInclusive = statements.wherePredicate(Predicates.shMaxInclusive)
                                .singleOrNull()?.`object`?.label?.toIntOrNull(),
                            path = path,
                            createdBy = resource.createdBy,
                            createdAt = resource.createdAt,
                            datatype = datatype
                        )
                        Classes.decimal -> NumberLiteralTemplateProperty(
                            id = resource.id,
                            label = resource.label,
                            placeholder = placeholder,
                            description = description,
                            order = order,
                            minCount = minCount,
                            maxCount = maxCount,
                            minInclusive = statements.wherePredicate(Predicates.shMinInclusive)
                                .singleOrNull()?.`object`?.label?.toDoubleOrNull(),
                            maxInclusive = statements.wherePredicate(Predicates.shMaxInclusive)
                                .singleOrNull()?.`object`?.label?.toDoubleOrNull(),
                            path = path,
                            createdBy = resource.createdBy,
                            createdAt = resource.createdAt,
                            datatype = datatype
                        )
                        Classes.float -> NumberLiteralTemplateProperty(
                            id = resource.id,
                            label = resource.label,
                            placeholder = placeholder,
                            description = description,
                            order = order,
                            minCount = minCount,
                            maxCount = maxCount,
                            minInclusive = statements.wherePredicate(Predicates.shMinInclusive)
                                .singleOrNull()?.`object`?.label?.toFloatOrNull(),
                            maxInclusive = statements.wherePredicate(Predicates.shMaxInclusive)
                                .singleOrNull()?.`object`?.label?.toFloatOrNull(),
                            path = path,
                            createdBy = resource.createdBy,
                            createdAt = resource.createdAt,
                            datatype = datatype
                        )
                        else -> OtherLiteralTemplateProperty(
                            id = resource.id,
                            label = resource.label,
                            placeholder = placeholder,
                            description = description,
                            order = order,
                            minCount = minCount,
                            maxCount = maxCount,
                            path = path,
                            createdBy = resource.createdBy,
                            createdAt = resource.createdAt,
                            datatype = datatype
                        )
                    }
                }
                `class` != null -> ResourceTemplateProperty(
                    id = resource.id,
                    label = resource.label,
                    placeholder = placeholder,
                    description = description,
                    order = order,
                    minCount = minCount,
                    maxCount = maxCount,
                    path = path,
                    createdBy = resource.createdBy,
                    createdAt = resource.createdAt,
                    `class` = `class`
                )
                else -> UntypedTemplateProperty(
                    id = resource.id,
                    label = resource.label,
                    placeholder = placeholder,
                    description = description,
                    order = order,
                    minCount = minCount,
                    maxCount = maxCount,
                    path = path,
                    createdBy = resource.createdBy,
                    createdAt = resource.createdAt
                )
            }
        }
    }
}

data class UntypedTemplateProperty(
    override val id: ThingId,
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val path: ObjectIdAndLabel,
    override val createdBy: ContributorId,
    override val createdAt: OffsetDateTime
) : TemplateProperty

sealed interface LiteralTemplateProperty : TemplateProperty {
    val datatype: ClassReference
}

data class NumberLiteralTemplateProperty<out T : Number>(
    override val id: ThingId,
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    val minInclusive: T?,
    val maxInclusive: T?,
    override val path: ObjectIdAndLabel,
    override val createdBy: ContributorId,
    override val createdAt: OffsetDateTime,
    override val datatype: ClassReference
) : LiteralTemplateProperty

data class StringLiteralTemplateProperty(
    override val id: ThingId,
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val path: ObjectIdAndLabel,
    override val createdBy: ContributorId,
    override val createdAt: OffsetDateTime,
    override val datatype: ClassReference,
    val pattern: String?,
) : LiteralTemplateProperty

data class OtherLiteralTemplateProperty(
    override val id: ThingId,
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val path: ObjectIdAndLabel,
    override val createdBy: ContributorId,
    override val createdAt: OffsetDateTime,
    override val datatype: ClassReference
) : LiteralTemplateProperty

data class ResourceTemplateProperty(
    override val id: ThingId,
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val path: ObjectIdAndLabel,
    override val createdBy: ContributorId,
    override val createdAt: OffsetDateTime,
    val `class`: ObjectIdAndLabel
) : TemplateProperty
