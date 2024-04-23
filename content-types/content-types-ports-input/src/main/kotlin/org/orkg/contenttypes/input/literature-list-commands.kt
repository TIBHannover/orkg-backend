package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.ListSection
import org.orkg.contenttypes.domain.LiteratureListSection
import org.orkg.contenttypes.domain.TextSection
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
    }

    data class CreateListSectionCommand(
        override val contributorId: ContributorId,
        override val literatureListId: ThingId,
        override val entries: List<ThingId>
    ) : CreateCommand, ListSectionDefinition

    data class CreateTextSectionCommand(
        override val contributorId: ContributorId,
        override val literatureListId: ThingId,
        override val heading: String,
        override val headingSize: Int,
        override val text: String
    ) : CreateCommand, TextSectionDefinition
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
        override val entries: List<ThingId>
    ) : UpdateCommand, ListSectionDefinition

    data class UpdateTextSectionCommand(
        override val literatureListSectionId: ThingId,
        override val contributorId: ContributorId,
        override val literatureListId: ThingId,
        override val heading: String,
        override val headingSize: Int,
        override val text: String
    ) : UpdateCommand, TextSectionDefinition
}

sealed interface LiteratureListSectionDefinition {
    fun matchesListSection(section: LiteratureListSection): Boolean
}

interface ListSectionDefinition : LiteratureListSectionDefinition {
    val entries: List<ThingId>

    override fun matchesListSection(section: LiteratureListSection): Boolean =
        section is ListSection && section.entries.map { it.id } == entries
}

interface TextSectionDefinition : LiteratureListSectionDefinition {
    val heading: String
    val headingSize: Int
    val text: String

    override fun matchesListSection(section: LiteratureListSection): Boolean =
        section is TextSection && section.heading == heading && section.headingSize == headingSize &&
            section.text == text
}

sealed interface LiteratureListSectionCommand

data class ListSectionCommand(
    override val entries: List<ThingId>
) : LiteratureListSectionCommand, ListSectionDefinition

data class TextSectionCommand(
    override val heading: String,
    override val headingSize: Int,
    override val text: String
) : LiteratureListSectionCommand, TextSectionDefinition
