package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility

data class Comparison(
    val id: ThingId,
    val title: String,
    val description: String?,
    val researchFields: List<ObjectIdAndLabel>,
    val identifiers: Map<String, List<String>>,
    val publicationInfo: PublicationInfo,
    val authors: List<Author>,
    val sustainableDevelopmentGoals: Set<ObjectIdAndLabel>,
    val contributions: List<ObjectIdAndLabel>,
    val visualizations: List<ObjectIdAndLabel>,
    val relatedFigures: List<ObjectIdAndLabel>,
    val relatedResources: List<ObjectIdAndLabel>,
    val references: List<String>,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val extractionMethod: ExtractionMethod,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId,
    val versions: List<HeadVersion>,
    val isAnonymized: Boolean,
    val visibility: Visibility,
    val unlistedBy: ContributorId? = null
) : ContentType {
    companion object {
        fun from(resource: Resource, statements: Map<ThingId, List<GeneralStatement>>, versions: List<HeadVersion>): Comparison {
            val directStatements = statements[resource.id].orEmpty()
            return Comparison(
                id = resource.id,
                title = resource.label,
                description = directStatements.wherePredicate(Predicates.description).firstObjectLabel(),
                researchFields = directStatements.wherePredicate(Predicates.hasSubject).objectIdsAndLabel(),
                identifiers = directStatements.associateIdentifiers(Identifiers.comparison),
                publicationInfo = PublicationInfo.from(directStatements),
                authors = statements.authors(resource.id),
                sustainableDevelopmentGoals = directStatements.wherePredicate(Predicates.sustainableDevelopmentGoal)
                    .objectIdsAndLabel()
                    .sortedBy { it.id }
                    .toSet(),
                contributions = directStatements.wherePredicate(Predicates.comparesContribution).objectIdsAndLabel(),
                visualizations = directStatements.wherePredicate(Predicates.hasVisualization).objectIdsAndLabel(),
                relatedFigures = directStatements.wherePredicate(Predicates.hasRelatedFigure).objectIdsAndLabel(),
                relatedResources = directStatements.wherePredicate(Predicates.hasRelatedResource).objectIdsAndLabel(),
                references = directStatements.wherePredicate(Predicates.reference)
                    .withoutObjectsWithBlankLabels()
                    .objects()
                    .filterIsInstance<Literal>()
                    .sortedBy { it.createdAt }
                    .map { it.label },
                observatories = listOf(resource.observatoryId),
                organizations = listOf(resource.organizationId),
                extractionMethod = resource.extractionMethod,
                createdAt = resource.createdAt,
                createdBy = resource.createdBy,
                versions = versions,
                isAnonymized = directStatements.wherePredicate(Predicates.isAnonymized)
                    .firstOrNull { it.`object` is Literal && (it.`object` as Literal).datatype == Literals.XSD.BOOLEAN.prefixedUri }
                    ?.`object`?.label.toBoolean(),
                visibility = resource.visibility,
                unlistedBy = resource.unlistedBy
            )
        }
    }
}

data class ComparisonRelatedResource(
    val id: ThingId,
    val label: String,
    val image: String?,
    val url: String?,
    val description: String?,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId
)

data class ComparisonRelatedFigure(
    val id: ThingId,
    val label: String,
    val image: String?,
    val description: String?,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId
)
