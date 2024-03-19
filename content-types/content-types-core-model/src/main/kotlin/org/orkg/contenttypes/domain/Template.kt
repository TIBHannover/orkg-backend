package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
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
    val targetClass: ThingId,
    val relations: TemplateRelation,
    val properties: List<TemplateProperty>,
    val isClosed: Boolean,
    val createdBy: ContributorId,
    val createdAt: OffsetDateTime,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val visibility: Visibility,
    val unlistedBy: ContributorId? = null
)

data class TemplateRelation(
    val researchFields: List<ObjectIdAndLabel>,
    val researchProblems: List<ObjectIdAndLabel>,
    val predicate: ObjectIdAndLabel?,
)

sealed interface TemplateProperty {
    val id: ThingId
    val label: String
    val placeholder: String?
    val description: String?
    val order: Long
    val minCount: Int?
    val maxCount: Int?
    val pattern: String?
    val path: ObjectIdAndLabel
    val createdBy: ContributorId
    val createdAt: OffsetDateTime

    companion object {
        fun from(resource: Resource, statements: Iterable<GeneralStatement>): TemplateProperty? {
            val placeholder = statements.wherePredicate(Predicates.placeholder).singleOrNull()?.`object`?.label
            val description = statements.wherePredicate(Predicates.description).singleOrNull()?.`object`?.label
            val order = statements.wherePredicate(Predicates.shOrder).single().`object`.label.toLong()
            val minCount = statements.wherePredicate(Predicates.shMinCount).singleOrNull()?.`object`?.label?.toInt()
            val maxCount = statements.wherePredicate(Predicates.shMaxCount).singleOrNull()?.`object`?.label?.toInt()
            val pattern = statements.wherePredicate(Predicates.shPattern).singleOrNull()?.`object`?.label
            val path = statements.wherePredicate(Predicates.shPath).single().objectIdAndLabel()!!
            val datatype = statements.wherePredicate(Predicates.shDatatype).singleOrNull().objectIdAndLabel()
            val `class` = statements.wherePredicate(Predicates.shClass).singleOrNull().objectIdAndLabel()
            return when {
                datatype != null -> LiteralTemplateProperty(
                    id = resource.id,
                    label = resource.label,
                    placeholder = placeholder,
                    description = description,
                    order = order,
                    minCount = minCount,
                    maxCount = maxCount,
                    pattern = pattern,
                    path = path,
                    createdBy = resource.createdBy,
                    createdAt = resource.createdAt,
                    datatype = datatype
                )
                `class` != null -> ResourceTemplateProperty(
                    id = resource.id,
                    label = resource.label,
                    placeholder = placeholder,
                    description = description,
                    order = order,
                    minCount = minCount,
                    maxCount = maxCount,
                    pattern = pattern,
                    path = path,
                    createdBy = resource.createdBy,
                    createdAt = resource.createdAt,
                    `class` = `class`
                )
                else -> null
            }
        }
    }
}

data class LiteralTemplateProperty(
    override val id: ThingId,
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val pattern: String?,
    override val path: ObjectIdAndLabel,
    override val createdBy: ContributorId,
    override val createdAt: OffsetDateTime,
    val datatype: ObjectIdAndLabel
) : TemplateProperty

data class ResourceTemplateProperty(
    override val id: ThingId,
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val pattern: String?,
    override val path: ObjectIdAndLabel,
    override val createdBy: ContributorId,
    override val createdAt: OffsetDateTime,
    val `class`: ObjectIdAndLabel
) : TemplateProperty
