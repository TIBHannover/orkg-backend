package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility

data class RosettaStoneTemplate(
    val id: ThingId,
    val label: String,
    val description: String?,
    val formattedLabel: FormattedLabel?,
    val targetClass: ThingId,
    val properties: List<TemplateProperty>,
    val createdBy: ContributorId,
    val createdAt: OffsetDateTime,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val visibility: Visibility,
    val unlistedBy: ContributorId? = null
) {
    companion object {
        fun from(resource: Resource, statements: Map<ThingId, List<GeneralStatement>>): RosettaStoneTemplate {
            val directStatements = statements[resource.id]!!
            return RosettaStoneTemplate(
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
                    .single()
                    .`object`.id,
                properties = directStatements
                    .wherePredicate(Predicates.shProperty)
                    .filter { it.`object` is Resource && Classes.propertyShape in (it.`object` as Resource).classes }
                    .mapNotNull { TemplateProperty.from(it.`object` as Resource, statements[it.`object`.id].orEmpty()) }
                    .sortedBy { it.order },
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
