package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.AbstractLiteratureListListSectionCommand
import org.orkg.contenttypes.input.AbstractLiteratureListSectionCommand
import org.orkg.contenttypes.input.AbstractLiteratureListTextSectionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class AbstractLiteratureListSectionCreator(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
) {
    internal fun create(
        contributorId: ContributorId,
        section: AbstractLiteratureListSectionCommand,
    ): ThingId =
        when (section) {
            is AbstractLiteratureListListSectionCommand -> createListSection(contributorId, section)
            is AbstractLiteratureListTextSectionCommand -> createTextSection(contributorId, section)
        }

    internal fun createListSectionEntry(
        contributorId: ContributorId,
        entry: AbstractLiteratureListListSectionCommand.Entry,
    ): ThingId {
        val entryId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = "Entry",
                classes = setOf(Classes.listSection)
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = entryId,
                predicateId = Predicates.hasLink,
                objectId = entry.id
            )
        )
        entry.description?.let { description ->
            val descriptionLiteral = unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = description
                )
            )
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = entryId,
                    predicateId = Predicates.description,
                    objectId = descriptionLiteral
                )
            )
        }
        return entryId
    }

    private fun createListSection(
        contributorId: ContributorId,
        section: AbstractLiteratureListListSectionCommand,
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = "",
                classes = setOf(Classes.listSection)
            )
        )
        section.entries.forEach {
            val entryId = createListSectionEntry(contributorId, it)
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasEntry,
                    objectId = entryId
                )
            )
        }
        return sectionId
    }

    private fun createTextSection(
        contributorId: ContributorId,
        section: AbstractLiteratureListTextSectionCommand,
    ): ThingId {
        val sectionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.heading,
                classes = setOf(Classes.textSection)
            )
        )
        val headingSizeId = unsafeLiteralUseCases.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.headingSize.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = sectionId,
                predicateId = Predicates.hasHeadingLevel,
                objectId = headingSizeId
            )
        )
        val textId = unsafeLiteralUseCases.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.text
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = sectionId,
                predicateId = Predicates.hasContent,
                objectId = textId
            )
        )
        return sectionId
    }
}
