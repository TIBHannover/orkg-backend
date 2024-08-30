package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.LiteratureListListSection
import org.orkg.contenttypes.domain.LiteratureListSection
import org.orkg.contenttypes.domain.LiteratureListTextSection
import org.orkg.graph.domain.ExtractionMethod

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
        val sections: List<LiteratureListSectionDefinition>
    )
}

interface CreateLiteratureListSectionUseCase {
    fun createSection(command: CreateCommand): ThingId

    sealed interface CreateCommand {
        val contributorId: ContributorId
        val literatureListId: ThingId
        val index: Int?
    }

    data class CreateListSectionCommand(
        override val contributorId: ContributorId,
        override val literatureListId: ThingId,
        override val index: Int?,
        override val entries: List<LiteratureListListSectionDefinition.Entry>
    ) : CreateCommand, LiteratureListListSectionDefinition

    data class CreateTextSectionCommand(
        override val contributorId: ContributorId,
        override val literatureListId: ThingId,
        override val index: Int?,
        override val heading: String,
        override val headingSize: Int,
        override val text: String
    ) : CreateCommand, LiteratureListTextSectionDefinition
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
        val sections: List<LiteratureListSectionDefinition>?
    )
}

interface UpdateLiteratureListSectionUseCase {
    fun updateSection(command: UpdateCommand)

    sealed interface UpdateCommand {
        val literatureListSectionId: ThingId
        val contributorId: ContributorId
        val literatureListId: ThingId
    }

    data class UpdateListSectionCommand(
        override val literatureListSectionId: ThingId,
        override val contributorId: ContributorId,
        override val literatureListId: ThingId,
        override val entries: List<LiteratureListListSectionDefinition.Entry>
    ) : UpdateCommand, LiteratureListListSectionDefinition

    data class UpdateTextSectionCommand(
        override val literatureListSectionId: ThingId,
        override val contributorId: ContributorId,
        override val literatureListId: ThingId,
        override val heading: String,
        override val headingSize: Int,
        override val text: String
    ) : UpdateCommand, LiteratureListTextSectionDefinition
}

interface DeleteLiteratureListSectionUseCase {
    fun deleteSection(command: DeleteCommand)

    data class DeleteCommand(
        val literatureListId: ThingId,
        val sectionId: ThingId,
        val contributorId: ContributorId
    )
}

interface PublishLiteratureListUseCase {
    fun publish(command: PublishCommand): ThingId

    data class PublishCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val changelog: String
    )
}

sealed interface LiteratureListSectionDefinition {
    fun matchesLiteratureListSection(section: LiteratureListSection): Boolean
}

interface LiteratureListListSectionDefinition : LiteratureListSectionDefinition {
    val entries: List<Entry>

    data class Entry(
        val id: ThingId,
        val description: String? = null
    )

    override fun matchesLiteratureListSection(section: LiteratureListSection): Boolean =
        section is LiteratureListListSection && section.entries.map { Entry(it.value.id, it.description) } == entries
}

interface LiteratureListTextSectionDefinition : LiteratureListSectionDefinition {
    val heading: String
    val headingSize: Int
    val text: String

    override fun matchesLiteratureListSection(section: LiteratureListSection): Boolean =
        section is LiteratureListTextSection && section.heading == heading && section.headingSize == headingSize &&
            section.text == text
}

sealed interface LiteratureListSectionCommand

data class LiteratureListListSectionCommand(
    override val entries: List<LiteratureListListSectionDefinition.Entry>
) : LiteratureListSectionCommand, LiteratureListListSectionDefinition

data class LiteratureListTextSectionCommand(
    override val heading: String,
    override val headingSize: Int,
    override val text: String
) : LiteratureListSectionCommand, LiteratureListTextSectionDefinition
