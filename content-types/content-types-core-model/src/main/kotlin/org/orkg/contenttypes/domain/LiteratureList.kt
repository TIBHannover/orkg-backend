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
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val extractionMethod: ExtractionMethod,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId,
    val visibility: Visibility,
    val unlistedBy: ContributorId? = null,
    val published: Boolean,
    val sections: List<LiteratureListSection>
)

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
