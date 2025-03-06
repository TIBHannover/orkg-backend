package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.LiteratureListListSectionDefinition
import org.orkg.contenttypes.input.LiteratureListSectionDefinition
import org.orkg.contenttypes.input.LiteratureListTextSectionDefinition
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
        section: LiteratureListSectionDefinition,
    ): ThingId =
        when (section) {
            is LiteratureListListSectionDefinition -> createListSection(contributorId, section)
            is LiteratureListTextSectionDefinition -> createTextSection(contributorId, section)
        }

    internal fun createListSectionEntry(
        contributorId: ContributorId,
        entry: LiteratureListListSectionDefinition.Entry,
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
        section: LiteratureListListSectionDefinition,
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
        section: LiteratureListTextSectionDefinition,
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
