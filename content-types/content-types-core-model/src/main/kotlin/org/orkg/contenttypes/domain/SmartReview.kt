package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility

data class SmartReview(
    val id: ThingId,
    val title: String,
    val researchFields: List<ObjectIdAndLabel>,
    val identifiers: Map<String, List<String>>,
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
    val sections: List<SmartReviewSection>,
    val references: List<String>,
    val acknowledgements: Map<ContributorId, Double>
) : ContentType {
    companion object {
        fun from(resource: Resource, root: ThingId, statements: Map<ThingId, List<GeneralStatement>>): SmartReview {
            val directStatements = statements[root].orEmpty()
            val contributionStatements = directStatements.singleOrNull {
                it.predicate.id == Predicates.hasContribution && it.`object` is Resource &&
                    Classes.contributionSmartReview in (it.`object` as Resource).classes
            }
                ?.let { statements[it.`object`.id] }
                .orEmpty()
            val versions = VersionInfo(
                head = HeadVersion(directStatements.firstOrNull()?.subject ?: resource),
                published = directStatements.wherePredicate(Predicates.hasPublishedVersion)
                    .sortedByDescending { it.createdAt }
                    .objects()
                    .map { PublishedVersion(it, statements[it.id]?.wherePredicate(Predicates.description)?.firstObjectLabel()) }
            )
            val sections = contributionStatements.wherePredicate(Predicates.hasSection)
                .filter { it.`object` is Resource }
                .sortedBy { it.createdAt }
                .map { it.`object` as Resource }
            val contributors = listOf(
                versions.head.createdBy,
                *versions.published.map { it.createdBy }.toTypedArray(),
                *sections.flatMap { SmartReviewSection.contributors(it, statements) }.toTypedArray()
            )
            return SmartReview(
                id = resource.id,
                title = resource.label,
                researchFields = directStatements.wherePredicate(Predicates.hasResearchField)
                    .objectIdsAndLabel()
                    .sortedBy { it.id },
                identifiers = directStatements.associateIdentifiers(Identifiers.smartReview),
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
                published = Classes.smartReviewPublished in resource.classes,
                sections = sections.map { SmartReviewSection.from(it, statements) },
                references = contributionStatements.wherePredicate(Predicates.hasReference)
                    .filter { it.`object` is Literal }
                    .sortedBy { it.createdAt }
                    .map { it.`object`.label },
                acknowledgements = contributors.groupingBy { it }
                    .eachCount()
                    .mapValues { (_, value) -> value.toDouble() / contributors.size }
            )
        }
    }
}

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

        fun contributors(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): List<ContributorId> =
            when {
                Classes.comparisonSection in root.classes -> SmartReviewComparisonSection.contributors(root, statements)
                Classes.visualizationSection in root.classes -> SmartReviewVisualizationSection.contributors(root, statements)
                Classes.resourceSection in root.classes -> SmartReviewResourceSection.contributors(root, statements)
                Classes.propertySection in root.classes -> SmartReviewPredicateSection.contributors(root, statements)
                Classes.ontologySection in root.classes -> SmartReviewOntologySection.contributors(root, statements)
                Classes.section in root.classes -> SmartReviewTextSection.contributors(root, statements)
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

        fun contributors(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): List<ContributorId> =
            listOfNotNull(root.createdBy, statements[root.id]?.hasLinkStatementTo<Resource>()?.`object`?.createdBy)
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

        fun contributors(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): List<ContributorId> =
            listOfNotNull(root.createdBy, statements[root.id]?.hasLinkStatementTo<Resource>()?.`object`?.createdBy)
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

        fun contributors(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): List<ContributorId> =
            listOfNotNull(root.createdBy, statements[root.id]?.hasLinkStatementTo<Resource>()?.`object`?.createdBy)
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

        fun contributors(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): List<ContributorId> =
            listOfNotNull(root.createdBy, statements[root.id]?.hasLinkStatementTo<Resource>()?.`object`?.createdBy)
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
                    ?.map { ThingReference.from(it.`object`) }
                    .orEmpty(),
                predicates = statements[root.id]?.wherePredicate(Predicates.showProperty)
                    ?.filter { it.`object` is Predicate }
                    ?.sortedBy { it.createdAt }
                    ?.map { PredicateReference(it.`object` as Predicate) }
                    .orEmpty()
            )

        fun contributors(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): List<ContributorId> =
            listOf(root.createdBy) +
                statements[root.id]?.wherePredicate(Predicates.hasEntity)
                    ?.filter { it.`object` is Predicate || it.`object` is Resource }
                    ?.map { it.`object`.createdBy }
                    .orEmpty() +
                statements[root.id]?.wherePredicate(Predicates.showProperty)
                    ?.filter { it.`object` is Predicate }
                    ?.map { it.`object`.createdBy }
                    .orEmpty()
    }
}

data class SmartReviewTextSection(
    override val id: ThingId,
    override val heading: String,
    val classes: Set<ThingId>,
    val text: String
) : SmartReviewSection {
    companion object {
        val types = setOf(
            Classes.acknowledgements,
            Classes.background,
            Classes.caption,
            Classes.conclusion,
            Classes.contribution,
            Classes.data,
            Classes.datasetDescription,
            Classes.discussion,
            Classes.epilogue,
            Classes.evaluation,
            Classes.externalResourceDescription,
            Classes.futureWork,
            Classes.introduction,
            Classes.legend,
            Classes.materials,
            Classes.methods,
            Classes.model,
            Classes.motivation,
            Classes.postscript,
            Classes.problemStatement,
            Classes.prologue,
            Classes.relatedWork,
            Classes.results,
            Classes.scenario,
            Classes.supplementaryInformationDescription
        )

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

        fun contributors(root: Resource, statements: Map<ThingId, List<GeneralStatement>>): List<ContributorId> =
            listOf(root.createdBy)
    }
}

private inline fun <reified T : Thing> List<GeneralStatement>.hasLinkStatementTo(): GeneralStatement? =
    singleOrNull { it.predicate.id == Predicates.hasLink && it.`object` is T }
