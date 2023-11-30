package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.FormattedLabel
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
    val order: Long
    val minCount: Int?
    val maxCount: Int?
    val pattern: String?
    val path: ObjectIdAndLabel
    val createdBy: ContributorId
    val createdAt: OffsetDateTime
}

data class LiteralTemplateProperty(
    override val id: ThingId,
    override val label: String,
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
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val pattern: String?,
    override val path: ObjectIdAndLabel,
    override val createdBy: ContributorId,
    override val createdAt: OffsetDateTime,
    val `class`: ObjectIdAndLabel
) : TemplateProperty
