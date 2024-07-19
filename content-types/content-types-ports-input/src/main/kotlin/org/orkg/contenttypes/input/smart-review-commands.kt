package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.SmartReviewComparisonSection
import org.orkg.contenttypes.domain.SmartReviewOntologySection
import org.orkg.contenttypes.domain.SmartReviewPredicateSection
import org.orkg.contenttypes.domain.SmartReviewResourceSection
import org.orkg.contenttypes.domain.SmartReviewSection
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.SmartReviewVisualizationSection
import org.orkg.graph.domain.ExtractionMethod

interface CreateSmartReviewUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val contributorId: ContributorId,
        val title: String,
        val researchFields: List<ThingId>,
        val authors: List<Author>,
        val sustainableDevelopmentGoals: Set<ThingId>,
        val observatories: List<ObservatoryId>,
        val organizations: List<OrganizationId>,
        val extractionMethod: ExtractionMethod,
        val sections: List<SmartReviewSectionDefinition>,
        val references: List<String>
    )
}

interface CreateSmartReviewSectionUseCase {
    fun createSection(command: CreateCommand): ThingId

    sealed interface CreateCommand {
        val contributorId: ContributorId
        val smartReviewId: ThingId
        val index: Int?
    }

    data class CreateComparisonSectionCommand(
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val index: Int?,
        override val heading: String,
        override val comparison: ThingId?
    ) : CreateCommand, SmartReviewComparisonSectionDefinition

    data class CreateVisualizationSectionCommand(
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val index: Int?,
        override val heading: String,
        override val visualization: ThingId?
    ) : CreateCommand, SmartReviewVisualizationSectionDefinition

    data class CreateResourceSectionCommand(
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val index: Int?,
        override val heading: String,
        override val resource: ThingId?
    ) : CreateCommand, SmartReviewResourceSectionDefinition

    data class CreatePredicateSectionCommand(
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val index: Int?,
        override val heading: String,
        override val predicate: ThingId?
    ) : CreateCommand, SmartReviewPredicateSectionDefinition

    data class CreateOntologySectionCommand(
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val index: Int?,
        override val heading: String,
        override val entities: List<ThingId>,
        override val predicates: List<ThingId>
    ) : CreateCommand, SmartReviewOntologySectionDefinition

    data class CreateTextSectionCommand(
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val index: Int?,
        override val heading: String,
        override val `class`: ThingId,
        override val text: String
    ) : CreateCommand, SmartReviewTextSectionDefinition
}

sealed interface SmartReviewSectionDefinition {
    val heading: String

    fun matchesSmartReviewSection(section: SmartReviewSection): Boolean =
        heading == section.heading
}

interface SmartReviewComparisonSectionDefinition : SmartReviewSectionDefinition {
    override val heading: String
    val comparison: ThingId?

    override fun matchesSmartReviewSection(section: SmartReviewSection): Boolean =
        super.matchesSmartReviewSection(section) && section is SmartReviewComparisonSection &&
            comparison == section.comparison?.id
}

interface SmartReviewVisualizationSectionDefinition : SmartReviewSectionDefinition {
    override val heading: String
    val visualization: ThingId?

    override fun matchesSmartReviewSection(section: SmartReviewSection): Boolean =
        super.matchesSmartReviewSection(section) && section is SmartReviewVisualizationSection &&
            visualization == section.visualization?.id
}

interface SmartReviewResourceSectionDefinition : SmartReviewSectionDefinition {
    override val heading: String
    val resource: ThingId?

    override fun matchesSmartReviewSection(section: SmartReviewSection): Boolean =
        super.matchesSmartReviewSection(section) && section is SmartReviewResourceSection &&
            resource == section.resource?.id
}

interface SmartReviewPredicateSectionDefinition : SmartReviewSectionDefinition {
    override val heading: String
    val predicate: ThingId?

    override fun matchesSmartReviewSection(section: SmartReviewSection): Boolean =
        super.matchesSmartReviewSection(section) && section is SmartReviewPredicateSection &&
            predicate == section.predicate?.id
}

interface SmartReviewOntologySectionDefinition : SmartReviewSectionDefinition {
    override val heading: String
    val entities: List<ThingId>
    val predicates: List<ThingId>

    override fun matchesSmartReviewSection(section: SmartReviewSection): Boolean =
        super.matchesSmartReviewSection(section) && section is SmartReviewOntologySection &&
            entities == section.entities.map { it.id } && predicates == section.predicates.map { it.id }
}

interface SmartReviewTextSectionDefinition : SmartReviewSectionDefinition {
    override val heading: String
    val `class`: ThingId
    val text: String

    override fun matchesSmartReviewSection(section: SmartReviewSection): Boolean =
        super.matchesSmartReviewSection(section) && section is SmartReviewTextSection &&
            `class` in section.classes && text == section.text
}

sealed interface SmartReviewSectionCommand

data class SmartReviewComparisonSectionCommand(
    override val heading: String,
    override val comparison: ThingId?
) : SmartReviewSectionCommand, SmartReviewComparisonSectionDefinition

data class SmartReviewVisualizationSectionCommand(
    override val heading: String,
    override val visualization: ThingId?
) : SmartReviewSectionCommand, SmartReviewVisualizationSectionDefinition

data class SmartReviewResourceSectionCommand(
    override val heading: String,
    override val resource: ThingId?
) : SmartReviewSectionCommand, SmartReviewResourceSectionDefinition

data class SmartReviewPredicateSectionCommand(
    override val heading: String,
    override val predicate: ThingId?
) : SmartReviewSectionCommand, SmartReviewPredicateSectionDefinition

data class SmartReviewOntologySectionCommand(
    override val heading: String,
    override val entities: List<ThingId>,
    override val predicates: List<ThingId>
) : SmartReviewSectionCommand, SmartReviewOntologySectionDefinition

data class SmartReviewTextSectionCommand(
    override val heading: String,
    override val `class`: ThingId,
    override val text: String
) : SmartReviewSectionCommand, SmartReviewTextSectionDefinition
