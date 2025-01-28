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

data class Paper(
    override val id: ThingId,
    val title: String,
    val researchFields: List<ObjectIdAndLabel>,
    val identifiers: Map<String, List<String>>,
    val publicationInfo: PublicationInfo,
    val authors: List<Author>,
    val contributions: List<ObjectIdAndLabel>,
    val sustainableDevelopmentGoals: Set<ObjectIdAndLabel>,
    val mentionings: Set<ResourceReference>,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    override val extractionMethod: ExtractionMethod,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    val verified: Boolean,
    override val visibility: Visibility,
    val modifiable: Boolean,
    override val unlistedBy: ContributorId? = null
) : ContentType {
    companion object {
        fun from(resource: Resource, statements: Map<ThingId, List<GeneralStatement>>): Paper {
            val directStatements = statements[resource.id].orEmpty()
            return Paper(
                id = resource.id,
                title = resource.label,
                researchFields = directStatements.wherePredicate(Predicates.hasResearchField)
                    .objectIdsAndLabel()
                    .sortedBy { it.id },
                identifiers = directStatements.associateIdentifiers(Identifiers.paper),
                publicationInfo = PublicationInfo.from(directStatements),
                authors = statements.authors(resource.id),
                contributions = directStatements.wherePredicate(Predicates.hasContribution).objectIdsAndLabel(),
                sustainableDevelopmentGoals = directStatements.wherePredicate(Predicates.sustainableDevelopmentGoal)
                    .objectIdsAndLabel()
                    .sortedBy { it.id }
                    .toSet(),
                mentionings = directStatements.wherePredicate(Predicates.mentions)
                    .objects()
                    .filterIsInstance<Resource>()
                    .map(::ResourceReference)
                    .sortedBy { it.label }
                    .toSet(),
                observatories = listOf(resource.observatoryId),
                organizations = listOf(resource.organizationId),
                extractionMethod = resource.extractionMethod,
                createdAt = resource.createdAt,
                createdBy = resource.createdBy,
                verified = resource.verified ?: false,
                visibility = resource.visibility,
                modifiable = resource.modifiable,
                unlistedBy = resource.unlistedBy
            )
        }
    }
}
