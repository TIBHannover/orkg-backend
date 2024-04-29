package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ListSectionDefinition
import org.orkg.contenttypes.input.LiteratureListSectionDefinition
import org.orkg.contenttypes.input.TextSectionDefinition
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
            is ListSectionDefinition -> createListSection(contributorId, section)
            is TextSectionDefinition -> createTextSection(contributorId, section)
        }

    private fun createListSection(
        contributorId: ContributorId,
        section: ListSectionDefinition
    ): ThingId {
        val sectionId = resourceService.createUnsafe(
            CreateResourceUseCase.CreateCommand(
                label = "",
                classes = setOf(Classes.listSection),
                contributorId = contributorId
            )
        )
        section.entries.forEach {
            val entryId = resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = "Entry",
                    classes = setOf(Classes.listSection),
                    contributorId = contributorId
                )
            )
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasEntry,
                `object` = entryId
            )
            statementService.add(
                userId = contributorId,
                subject = entryId,
                predicate = Predicates.hasLink,
                `object` = it
            )
        }
        return sectionId
    }

    private fun createTextSection(
        contributorId: ContributorId,
        section: TextSectionDefinition
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
