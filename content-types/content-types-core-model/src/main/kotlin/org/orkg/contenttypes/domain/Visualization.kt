package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility

data class Visualization(
    override val id: ThingId,
    val title: String,
    val description: String?,
    val authors: List<Author>,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    override val extractionMethod: ExtractionMethod,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    override val visibility: Visibility,
    override val unlistedBy: ContributorId? = null
) : ContentType {
    companion object {
        fun from(resource: Resource, statements: Map<ThingId, List<GeneralStatement>>): Visualization {
            val directStatements = statements[resource.id].orEmpty()
            return Visualization(
                id = resource.id,
                title = resource.label,
                description = directStatements.wherePredicate(Predicates.description).firstObjectLabel(),
                authors = statements.authors(resource.id),
                observatories = listOf(resource.observatoryId),
                organizations = listOf(resource.organizationId),
                extractionMethod = resource.extractionMethod,
                createdAt = resource.createdAt,
                createdBy = resource.createdBy,
                visibility = resource.visibility,
                unlistedBy = resource.unlistedBy
            )
        }
    }
}
