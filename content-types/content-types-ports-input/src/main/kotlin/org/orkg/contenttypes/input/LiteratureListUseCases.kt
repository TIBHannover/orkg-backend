package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.LiteratureList
import org.orkg.contenttypes.domain.LiteratureListListSection
import org.orkg.contenttypes.domain.LiteratureListSection
import org.orkg.contenttypes.domain.LiteratureListTextSection
import org.orkg.contenttypes.domain.Paper
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface LiteratureListUseCases :
    RetrieveLiteratureListUseCase,
    CreateLiteratureListUseCase,
    CreateLiteratureListSectionUseCase,
    UpdateLiteratureListUseCase,
    UpdateLiteratureListSectionUseCase,
    DeleteLiteratureListSectionUseCase,
    PublishLiteratureListUseCase

interface RetrieveLiteratureListUseCase {
    fun findById(id: ThingId): Optional<LiteratureList>

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
    ): Page<LiteratureList>

    fun findPublishedContentById(
        literatureListId: ThingId,
        contentId: ThingId,
    ): Either<Paper, Resource>
}

interface CreateLiteratureListUseCase {
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
        val sections: List<AbstractLiteratureListSectionCommand>,
    )
}

interface CreateLiteratureListSectionUseCase {
    fun create(command: CreateCommand): ThingId

    sealed interface CreateCommand {
        val contributorId: ContributorId
        val literatureListId: ThingId
        val index: Int?
    }

    data class CreateListSectionCommand(
        override val contributorId: ContributorId,
        override val literatureListId: ThingId,
        override val index: Int?,
        override val entries: List<AbstractLiteratureListListSectionCommand.Entry>,
    ) : CreateCommand,
        AbstractLiteratureListListSectionCommand

    data class CreateTextSectionCommand(
        override val contributorId: ContributorId,
        override val literatureListId: ThingId,
        override val index: Int?,
        override val heading: String,
        override val headingSize: Int,
        override val text: String,
    ) : CreateCommand,
        AbstractLiteratureListTextSectionCommand
}

interface UpdateLiteratureListUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val literatureListId: ThingId,
        val contributorId: ContributorId,
        val title: String?,
        val researchFields: List<ThingId>?,
        val authors: List<Author>?,
        val sustainableDevelopmentGoals: Set<ThingId>?,
        val observatories: List<ObservatoryId>?,
        val organizations: List<OrganizationId>?,
        val extractionMethod: ExtractionMethod?,
        val sections: List<AbstractLiteratureListSectionCommand>?,
        val visibility: Visibility?,
    )
}

interface UpdateLiteratureListSectionUseCase {
    fun update(command: UpdateCommand)

    sealed interface UpdateCommand {
        val literatureListSectionId: ThingId
        val contributorId: ContributorId
        val literatureListId: ThingId
    }

    data class UpdateListSectionCommand(
        override val literatureListSectionId: ThingId,
        override val contributorId: ContributorId,
        override val literatureListId: ThingId,
        override val entries: List<AbstractLiteratureListListSectionCommand.Entry>,
    ) : UpdateCommand,
        AbstractLiteratureListListSectionCommand

    data class UpdateTextSectionCommand(
        override val literatureListSectionId: ThingId,
        override val contributorId: ContributorId,
        override val literatureListId: ThingId,
        override val heading: String,
        override val headingSize: Int,
        override val text: String,
    ) : UpdateCommand,
        AbstractLiteratureListTextSectionCommand
}

interface DeleteLiteratureListSectionUseCase {
    fun delete(command: DeleteCommand)

    data class DeleteCommand(
        val literatureListId: ThingId,
        val sectionId: ThingId,
        val contributorId: ContributorId,
    )
}

interface PublishLiteratureListUseCase {
    fun publish(command: PublishCommand): ThingId

    data class PublishCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val changelog: String,
    )
}

sealed interface AbstractLiteratureListSectionCommand {
    fun matchesLiteratureListSection(section: LiteratureListSection): Boolean
}

interface AbstractLiteratureListListSectionCommand : AbstractLiteratureListSectionCommand {
    val entries: List<Entry>

    data class Entry(
        val id: ThingId,
        val description: String? = null,
    )

    override fun matchesLiteratureListSection(section: LiteratureListSection): Boolean =
        section is LiteratureListListSection && section.entries.map { Entry(it.value.id, it.description) } == entries
}

interface AbstractLiteratureListTextSectionCommand : AbstractLiteratureListSectionCommand {
    val heading: String
    val headingSize: Int
    val text: String

    override fun matchesLiteratureListSection(section: LiteratureListSection): Boolean =
        section is LiteratureListTextSection &&
            section.heading == heading &&
            section.headingSize == headingSize &&
            section.text == text
}

data class LiteratureListListSectionCommand(
    override val entries: List<AbstractLiteratureListListSectionCommand.Entry>,
) : AbstractLiteratureListListSectionCommand

data class LiteratureListTextSectionCommand(
    override val heading: String,
    override val headingSize: Int,
    override val text: String,
) : AbstractLiteratureListTextSectionCommand
