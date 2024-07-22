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
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class AbstractLiteratureListSectionCreator(
    private val statementService: StatementUseCases,
    private val resourceService: ResourceUseCases,
    private val literalService: LiteralUseCases
) {
    internal fun create(
        contributorId: ContributorId,
        section: LiteratureListSectionDefinition
    ): ThingId =
        when (section) {
            is LiteratureListListSectionDefinition -> createListSection(contributorId, section)
            is LiteratureListTextSectionDefinition -> createTextSection(contributorId, section)
        }

    internal fun createListSectionEntry(
        contributorId: ContributorId,
        entry: LiteratureListListSectionDefinition.Entry
    ): ThingId {
        val entryId = resourceService.createUnsafe(
            CreateResourceUseCase.CreateCommand(
                label = "Entry",
                classes = setOf(Classes.listSection),
                contributorId = contributorId
            )
        )
        statementService.add(
            userId = contributorId,
            subject = entryId,
            predicate = Predicates.hasLink,
            `object` = entry.id
        )
        entry.description?.let { description ->
            val descriptionLiteral = literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = description
                )
            )
            statementService.add(
                userId = contributorId,
                subject = entryId,
                predicate = Predicates.description,
                `object` = descriptionLiteral
            )
        }
        return entryId
    }

    private fun createListSection(
        contributorId: ContributorId,
        section: LiteratureListListSectionDefinition
    ): ThingId {
        val sectionId = resourceService.createUnsafe(
            CreateResourceUseCase.CreateCommand(
                label = "",
                classes = setOf(Classes.listSection),
                contributorId = contributorId
            )
        )
        section.entries.forEach {
            val entryId = createListSectionEntry(contributorId, it)
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasEntry,
                `object` = entryId
            )
        }
        return sectionId
    }

    private fun createTextSection(
        contributorId: ContributorId,
        section: LiteratureListTextSectionDefinition
    ): ThingId {
        val sectionId = resourceService.createUnsafe(
            CreateResourceUseCase.CreateCommand(
                label = section.heading,
                classes = setOf(Classes.textSection),
                contributorId = contributorId
            )
        )
        val headingSizeId = literalService.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.headingSize.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        )
        statementService.add(
            userId = contributorId,
            subject = sectionId,
            predicate = Predicates.hasHeadingLevel,
            `object` = headingSizeId
        )
        val textId = literalService.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = contributorId,
                label = section.text
            )
        )
        statementService.add(
            userId = contributorId,
            subject = sectionId,
            predicate = Predicates.hasContent,
            `object` = textId
        )
        return sectionId
    }
}
