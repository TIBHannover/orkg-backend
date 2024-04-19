package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility

data class LiteratureList(
    val id: ThingId,
    val title: String,
    val researchFields: List<ObjectIdAndLabel>,
    val authors: List<Author>,
    val versions: VersionInfo,
    val sustainableDevelopmentGoals: Set<ObjectIdAndLabel>,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val extractionMethod: ExtractionMethod,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId,
    val visibility: Visibility,
    val unlistedBy: ContributorId? = null,
    val published: Boolean,
    val sections: List<LiteratureListSection>
) : ContentType {
    companion object {
        fun from(resource: Resource, root: ThingId, statements: Map<ThingId, List<GeneralStatement>>): LiteratureList {
            val directStatements = statements[root].orEmpty()
            return LiteratureList(
                id = resource.id,
                title = resource.label,
                researchFields = directStatements.wherePredicate(Predicates.hasResearchField)
                    .objectIdsAndLabel()
                    .sortedBy { it.id },
                authors = statements.authors(root).ifEmpty { statements.legacyAuthors(root) },
                versions = VersionInfo(
                    head = HeadVersion(directStatements.first().subject as Resource),
                    published = directStatements.wherePredicate(Predicates.hasPublishedVersion)
                        .sortedByDescending { it.createdAt }
                        .objects()
                        .map { PublishedVersion(it, statements[it.id]?.wherePredicate(Predicates.description)?.firstObjectLabel()) }
                ),
                sustainableDevelopmentGoals = directStatements.wherePredicate(Predicates.sustainableDevelopmentGoal)
                    .objectIdsAndLabel()
                    .sortedBy { it.id }
                    .toSet(),
                observatories = listOf(resource.observatoryId),
                organizations = listOf(resource.organizationId),
                extractionMethod = resource.extractionMethod,
                createdAt = resource.createdAt,
                createdBy = resource.createdBy,
                visibility = resource.visibility,
                unlistedBy = resource.unlistedBy,
                published = Classes.literatureListPublished in resource.classes,
                sections = directStatements.wherePredicate(Predicates.hasSection)
                    .filter { it.`object` is Resource }
                    .sortedBy { it.createdAt }
                    .map { LiteratureListSection.from(it.`object` as Resource, statements) }
            )
        }
    }
}

sealed interface LiteratureListSection {
    val id: ThingId

    companion object {
        fun from(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): LiteratureListSection =
            when {
                Classes.listSection in root.classes -> ListSection.from(root, statements)
                Classes.textSection in root.classes -> TextSection.from(root, statements)
                else -> throw IllegalStateException("Cannot convert section ${root.id} to literature list section. This is a bug.")
            }
    }
}

data class ListSection(
    override val id: ThingId,
    val entries: List<ResourceReference>
) : LiteratureListSection {
    companion object {
        fun from(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): ListSection =
            ListSection(
                id = root.id,
                entries = statements[root.id]
                    ?.wherePredicate(Predicates.hasEntry)
                    ?.sortedBy { it.createdAt }
                    ?.mapNotNull { hasEntry ->
                        statements[hasEntry.`object`.id]
                            ?.singleOrNull {
                                (it.predicate.id == Predicates.hasLink || it.predicate.id == Predicates.hasPaper) &&
                                    it.`object` is Resource
                            }
                            ?.let { ResourceReference(it.`object` as Resource) }
                    }.orEmpty()
            )
    }
}

data class TextSection(
    override val id: ThingId,
    val heading: String,
    val headingSize: Int,
    val text: String
) : LiteratureListSection {
    companion object {
        fun from(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): TextSection =
            TextSection(
                id = root.id,
                heading = root.label,
                headingSize = statements[root.id]
                    ?.wherePredicate(Predicates.hasHeadingLevel)
                    ?.singleObjectLabel()
                    ?.toIntOrNull() ?: 2,
                text = statements[root.id]
                    ?.wherePredicate(Predicates.hasContent)
                    ?.singleObjectLabel()
                    .orEmpty()
            )
    }
}
