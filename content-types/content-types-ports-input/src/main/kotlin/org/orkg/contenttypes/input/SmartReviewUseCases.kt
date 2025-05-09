package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.ContentType
import org.orkg.contenttypes.domain.SmartReview
import org.orkg.contenttypes.domain.SmartReviewComparisonSection
import org.orkg.contenttypes.domain.SmartReviewOntologySection
import org.orkg.contenttypes.domain.SmartReviewPredicateSection
import org.orkg.contenttypes.domain.SmartReviewResourceSection
import org.orkg.contenttypes.domain.SmartReviewSection
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.SmartReviewVisualizationSection
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface SmartReviewUseCases :
    RetrieveSmartReviewUseCase,
    CreateSmartReviewUseCase,
    CreateSmartReviewSectionUseCase,
    UpdateSmartReviewUseCase,
    UpdateSmartReviewSectionUseCase,
    DeleteSmartReviewSectionUseCase,
    PublishSmartReviewUseCase

interface RetrieveSmartReviewUseCase {
    fun findById(id: ThingId): Optional<SmartReview>

    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
        researchField: ThingId? = null,
        includeSubfields: Boolean = false,
        published: Boolean? = null,
        sustainableDevelopmentGoal: ThingId? = null,
    ): Page<SmartReview>

    fun findPublishedContentById(
        smartReviewId: ThingId,
        contentId: ThingId,
    ): Either<ContentType, List<GeneralStatement>>
}

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
        val sections: List<AbstractSmartReviewSectionCommand>,
        val references: List<String>,
    )
}

interface CreateSmartReviewSectionUseCase {
    fun create(command: CreateCommand): ThingId

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
        override val comparison: ThingId?,
    ) : CreateCommand,
        AbstractSmartReviewComparisonSectionCommand

    data class CreateVisualizationSectionCommand(
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val index: Int?,
        override val heading: String,
        override val visualization: ThingId?,
    ) : CreateCommand,
        AbstractSmartReviewVisualizationSectionCommand

    data class CreateResourceSectionCommand(
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val index: Int?,
        override val heading: String,
        override val resource: ThingId?,
    ) : CreateCommand,
        AbstractSmartReviewResourceSectionCommand

    data class CreatePredicateSectionCommand(
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val index: Int?,
        override val heading: String,
        override val predicate: ThingId?,
    ) : CreateCommand,
        AbstractSmartReviewPredicateSectionCommand

    data class CreateOntologySectionCommand(
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val index: Int?,
        override val heading: String,
        override val entities: List<ThingId>,
        override val predicates: List<ThingId>,
    ) : CreateCommand,
        AbstractSmartReviewOntologySectionCommand

    data class CreateTextSectionCommand(
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val index: Int?,
        override val heading: String,
        override val `class`: ThingId?,
        override val text: String,
    ) : CreateCommand,
        AbstractSmartReviewTextSectionCommand
}

interface UpdateSmartReviewUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val smartReviewId: ThingId,
        val contributorId: ContributorId,
        val title: String?,
        val researchFields: List<ThingId>?,
        val authors: List<Author>?,
        val sustainableDevelopmentGoals: Set<ThingId>?,
        val observatories: List<ObservatoryId>?,
        val organizations: List<OrganizationId>?,
        val extractionMethod: ExtractionMethod?,
        val sections: List<AbstractSmartReviewSectionCommand>?,
        val references: List<String>?,
        val visibility: Visibility?,
    )
}

interface UpdateSmartReviewSectionUseCase {
    fun update(command: UpdateCommand)

    sealed interface UpdateCommand {
        val smartReviewSectionId: ThingId
        val contributorId: ContributorId
        val smartReviewId: ThingId
    }

    data class UpdateComparisonSectionCommand(
        override val smartReviewSectionId: ThingId,
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val heading: String,
        override val comparison: ThingId?,
    ) : UpdateCommand,
        AbstractSmartReviewComparisonSectionCommand

    data class UpdateVisualizationSectionCommand(
        override val smartReviewSectionId: ThingId,
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val heading: String,
        override val visualization: ThingId?,
    ) : UpdateCommand,
        AbstractSmartReviewVisualizationSectionCommand

    data class UpdateResourceSectionCommand(
        override val smartReviewSectionId: ThingId,
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val heading: String,
        override val resource: ThingId?,
    ) : UpdateCommand,
        AbstractSmartReviewResourceSectionCommand

    data class UpdatePredicateSectionCommand(
        override val smartReviewSectionId: ThingId,
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val heading: String,
        override val predicate: ThingId?,
    ) : UpdateCommand,
        AbstractSmartReviewPredicateSectionCommand

    data class UpdateOntologySectionCommand(
        override val smartReviewSectionId: ThingId,
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val heading: String,
        override val entities: List<ThingId>,
        override val predicates: List<ThingId>,
    ) : UpdateCommand,
        AbstractSmartReviewOntologySectionCommand

    data class UpdateTextSectionCommand(
        override val smartReviewSectionId: ThingId,
        override val contributorId: ContributorId,
        override val smartReviewId: ThingId,
        override val heading: String,
        override val `class`: ThingId?,
        override val text: String,
    ) : UpdateCommand,
        AbstractSmartReviewTextSectionCommand
}

interface DeleteSmartReviewSectionUseCase {
    fun delete(command: DeleteCommand)

    data class DeleteCommand(
        val smartReviewId: ThingId,
        val sectionId: ThingId,
        val contributorId: ContributorId,
    )
}

interface PublishSmartReviewUseCase {
    fun publish(command: PublishCommand): ThingId

    data class PublishCommand(
        val smartReviewId: ThingId,
        val contributorId: ContributorId,
        val changelog: String,
        val assignDOI: Boolean,
        val description: String?,
    ) {
        init {
            require(!assignDOI || assignDOI && !description.isNullOrBlank()) { "Description must not be blank when assigning a DOI." }
        }
    }
}

sealed interface AbstractSmartReviewSectionCommand {
    val heading: String

    fun matchesSmartReviewSection(section: SmartReviewSection): Boolean =
        heading == section.heading
}

interface AbstractSmartReviewComparisonSectionCommand : AbstractSmartReviewSectionCommand {
    override val heading: String
    val comparison: ThingId?

    override fun matchesSmartReviewSection(section: SmartReviewSection): Boolean =
        super.matchesSmartReviewSection(section) &&
            section is SmartReviewComparisonSection &&
            comparison == section.comparison?.id
}

interface AbstractSmartReviewVisualizationSectionCommand : AbstractSmartReviewSectionCommand {
    override val heading: String
    val visualization: ThingId?

    override fun matchesSmartReviewSection(section: SmartReviewSection): Boolean =
        super.matchesSmartReviewSection(section) &&
            section is SmartReviewVisualizationSection &&
            visualization == section.visualization?.id
}

interface AbstractSmartReviewResourceSectionCommand : AbstractSmartReviewSectionCommand {
    override val heading: String
    val resource: ThingId?

    override fun matchesSmartReviewSection(section: SmartReviewSection): Boolean =
        super.matchesSmartReviewSection(section) &&
            section is SmartReviewResourceSection &&
            resource == section.resource?.id
}

interface AbstractSmartReviewPredicateSectionCommand : AbstractSmartReviewSectionCommand {
    override val heading: String
    val predicate: ThingId?

    override fun matchesSmartReviewSection(section: SmartReviewSection): Boolean =
        super.matchesSmartReviewSection(section) &&
            section is SmartReviewPredicateSection &&
            predicate == section.predicate?.id
}

interface AbstractSmartReviewOntologySectionCommand : AbstractSmartReviewSectionCommand {
    override val heading: String
    val entities: List<ThingId>
    val predicates: List<ThingId>

    override fun matchesSmartReviewSection(section: SmartReviewSection): Boolean =
        super.matchesSmartReviewSection(section) &&
            section is SmartReviewOntologySection &&
            entities == section.entities.map { it.id } &&
            predicates == section.predicates.map { it.id }
}

interface AbstractSmartReviewTextSectionCommand : AbstractSmartReviewSectionCommand {
    override val heading: String
    val `class`: ThingId?
    val text: String

    override fun matchesSmartReviewSection(section: SmartReviewSection): Boolean =
        super.matchesSmartReviewSection(section) &&
            section is SmartReviewTextSection &&
            (`class` == null || `class` in section.classes) &&
            text == section.text
}

data class SmartReviewComparisonSectionCommand(
    override val heading: String,
    override val comparison: ThingId?,
) : AbstractSmartReviewComparisonSectionCommand

data class SmartReviewVisualizationSectionCommand(
    override val heading: String,
    override val visualization: ThingId?,
) : AbstractSmartReviewVisualizationSectionCommand

data class SmartReviewResourceSectionCommand(
    override val heading: String,
    override val resource: ThingId?,
) : AbstractSmartReviewResourceSectionCommand

data class SmartReviewPredicateSectionCommand(
    override val heading: String,
    override val predicate: ThingId?,
) : AbstractSmartReviewPredicateSectionCommand

data class SmartReviewOntologySectionCommand(
    override val heading: String,
    override val entities: List<ThingId>,
    override val predicates: List<ThingId>,
) : AbstractSmartReviewOntologySectionCommand

data class SmartReviewTextSectionCommand(
    override val heading: String,
    override val `class`: ThingId?,
    override val text: String,
) : AbstractSmartReviewTextSectionCommand
