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
    val sections: List<LiteratureListSection>,
    val acknowledgements: Map<ContributorId, Double>
) : ContentType {
    companion object {
        fun from(resource: Resource, root: ThingId, statements: Map<ThingId, List<GeneralStatement>>): LiteratureList {
            val directStatements = statements[root].orEmpty()
            val versions = VersionInfo(
                head = HeadVersion(directStatements.firstOrNull()?.subject ?: resource),
                published = directStatements.wherePredicate(Predicates.hasPublishedVersion)
                    .sortedByDescending { it.createdAt }
                    .objects()
                    .map { PublishedVersion(it, statements[it.id]?.wherePredicate(Predicates.description)?.firstObjectLabel()) }
            )
            val sections = directStatements.wherePredicate(Predicates.hasSection)
                .filter { it.`object` is Resource }
                .sortedBy { it.createdAt }
                .map { it.`object` as Resource }
            val contributors = listOf(
                versions.head.createdBy,
                *versions.published.map { it.createdBy }.toTypedArray(),
                *sections.flatMap { LiteratureListSection.contributors(it, statements) }.toTypedArray()
            )
            return LiteratureList(
                id = resource.id,
                title = resource.label,
                researchFields = directStatements.wherePredicate(Predicates.hasResearchField)
                    .objectIdsAndLabel()
                    .sortedBy { it.id },
                authors = statements.authors(root).ifEmpty { statements.legacyAuthors(root) },
                versions = versions,
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
                sections = sections.map { LiteratureListSection.from(it, statements) },
                acknowledgements = contributors.groupingBy { it }
                    .eachCount()
                    .mapValues { (_, value) -> value.toDouble() / contributors.size }
            )
        }
    }
}

sealed interface LiteratureListSection {
    val id: ThingId

    companion object {
        fun from(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): LiteratureListSection =
            when {
                Classes.listSection in root.classes -> LiteratureListListSection.from(root, statements)
                Classes.textSection in root.classes -> LiteratureListTextSection.from(root, statements)
                else -> throw IllegalStateException("Cannot convert section ${root.id} to literature list section. This is a bug.")
            }

        fun contributors(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): List<ContributorId> =
            when {
                Classes.listSection in root.classes -> LiteratureListListSection.contributors(root, statements)
                Classes.textSection in root.classes -> LiteratureListTextSection.contributors(root)
                else -> throw IllegalStateException("Cannot convert section ${root.id} to literature list section. This is a bug.")
            }
    }
}

data class LiteratureListListSection(
    override val id: ThingId,
    val entries: List<Entry>
) : LiteratureListSection {
    data class Entry(
        val value: ResourceReference,
        val description: String? = null
    )

    companion object {
        fun from(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): LiteratureListListSection =
            LiteratureListListSection(
                id = root.id,
                entries = statements[root.id]
                    ?.wherePredicate(Predicates.hasEntry)
                    ?.sortedBy { it.createdAt }
                    ?.mapNotNull { hasEntry ->
                        val entryStatements = statements[hasEntry.`object`.id]
                        val hasLink = entryStatements?.singleOrNull {
                            (it.predicate.id == Predicates.hasLink || it.predicate.id == Predicates.hasPaper) &&
                                it.`object` is Resource
                        }
                        hasLink?.let {
                            Entry(
                                ResourceReference(it.`object` as Resource),
                                entryStatements.wherePredicate(Predicates.description).singleObjectLabel()
                            )
                        }
                    }.orEmpty()
            )

        fun contributors(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): List<ContributorId> =
            statements[root.id].orEmpty()
                .wherePredicate(Predicates.hasEntry)
                .map { hasEntry ->
                    val entryStatements = statements[hasEntry.`object`.id]
                    val hasLink = entryStatements?.singleOrNull {
                        (it.predicate.id == Predicates.hasLink || it.predicate.id == Predicates.hasPaper) &&
                            it.`object` is Resource
                    }
                    listOfNotNull(root.createdBy, hasLink?.`object`?.createdBy)
                }
                .flatten()
    }
}

data class LiteratureListTextSection(
    override val id: ThingId,
    val heading: String,
    val headingSize: Int,
    val text: String
) : LiteratureListSection {
    companion object {
        fun from(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): LiteratureListTextSection =
            LiteratureListTextSection(
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

        fun contributors(root: Resource): List<ContributorId> =
            listOf(root.createdBy)
    }
}
