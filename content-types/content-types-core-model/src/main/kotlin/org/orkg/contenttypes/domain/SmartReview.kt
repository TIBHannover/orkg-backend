package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility

data class SmartReview(
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
    val sections: List<SmartReviewSection>,
    val references: List<String>
)

sealed interface SmartReviewSection {
    val id: ThingId
    val heading: String

    companion object {
        fun from(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): SmartReviewSection =
            when {
                Classes.comparisonSection in root.classes -> SmartReviewComparisonSection.from(root, statements)
                Classes.visualizationSection in root.classes -> SmartReviewVisualizationSection.from(root, statements)
                Classes.resourceSection in root.classes -> SmartReviewResourceSection.from(root, statements)
                Classes.propertySection in root.classes -> SmartReviewPredicateSection.from(root, statements)
                Classes.ontologySection in root.classes -> SmartReviewOntologySection.from(root, statements)
                Classes.section in root.classes -> SmartReviewTextSection.from(root, statements)
                else -> throw IllegalStateException("Cannot convert section ${root.id} to smart review section. This is a bug.")
            }
    }
}

data class SmartReviewComparisonSection(
    override val id: ThingId,
    override val heading: String,
    val comparison: ResourceReference?
) : SmartReviewSection {
    companion object {
        fun from(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): SmartReviewComparisonSection =
            SmartReviewComparisonSection(
                id = root.id,
                heading = root.label,
                comparison = statements[root.id]?.hasLinkStatementTo<Resource>()
                    ?.let { ResourceReference(it.`object` as Resource) }
            )
    }
}

data class SmartReviewVisualizationSection(
    override val id: ThingId,
    override val heading: String,
    val visualization: ResourceReference?
) : SmartReviewSection {
    companion object {
        fun from(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): SmartReviewVisualizationSection =
            SmartReviewVisualizationSection(
                id = root.id,
                heading = root.label,
                visualization = statements[root.id]?.hasLinkStatementTo<Resource>()
                    ?.let { ResourceReference(it.`object` as Resource) }
            )
    }
}

data class SmartReviewResourceSection(
    override val id: ThingId,
    override val heading: String,
    val resource: ResourceReference?
) : SmartReviewSection {
    companion object {
        fun from(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): SmartReviewResourceSection =
            SmartReviewResourceSection(
                id = root.id,
                heading = root.label,
                resource = statements[root.id]?.hasLinkStatementTo<Resource>()
                    ?.let { ResourceReference(it.`object` as Resource) }
            )
    }
}

data class SmartReviewPredicateSection(
    override val id: ThingId,
    override val heading: String,
    val predicate: PredicateReference?
) : SmartReviewSection {
    companion object {
        fun from(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): SmartReviewPredicateSection =
            SmartReviewPredicateSection(
                id = root.id,
                heading = root.label,
                predicate = statements[root.id]?.hasLinkStatementTo<Predicate>()
                    ?.let { PredicateReference(it.`object` as Predicate) }
            )
    }
}

data class SmartReviewOntologySection(
    override val id: ThingId,
    override val heading: String,
    val entities: List<ThingReference>,
    val predicates: List<PredicateReference>
) : SmartReviewSection {
    companion object {
        fun from(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): SmartReviewOntologySection =
            SmartReviewOntologySection(
                id = root.id,
                heading = root.label,
                entities = statements[root.id]?.wherePredicate(Predicates.hasEntity)
                    ?.filter { it.`object` is Predicate || it.`object` is Resource }
                    ?.sortedBy { it.createdAt }
                    ?.map {
                        when (val `object` = it.`object`) {
                            is Resource -> ResourceReference(`object`)
                            is Predicate -> PredicateReference(`object`)
                            else -> throw IllegalStateException("Cannot convert section ${`object`.id} to smart review ontology section entity. This is a bug.")
                        }
                    }
                    .orEmpty(),
                predicates = statements[root.id]?.wherePredicate(Predicates.showProperty)
                    ?.filter { it.`object` is Predicate }
                    ?.sortedBy { it.createdAt }
                    ?.map { PredicateReference(it.`object` as Predicate) }
                    .orEmpty()
            )
    }
}

data class SmartReviewTextSection(
    override val id: ThingId,
    override val heading: String,
    val classes: Set<ThingId>,
    val text: String
) : SmartReviewSection {
    companion object {
        fun from(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): SmartReviewTextSection =
            SmartReviewTextSection(
                id = root.id,
                heading = root.label,
                classes = root.classes - Classes.section,
                text = statements[root.id]
                    ?.wherePredicate(Predicates.hasContent)
                    ?.singleObjectLabel()
                    .orEmpty()
            )
    }
}

private inline fun <reified T : Thing> List<GeneralStatement>.hasLinkStatementTo(): GeneralStatement? =
    singleOrNull { it.predicate.id == Predicates.hasLink && it.`object` is T }
