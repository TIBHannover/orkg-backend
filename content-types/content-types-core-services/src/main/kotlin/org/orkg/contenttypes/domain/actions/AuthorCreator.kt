package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.Identifiers
import org.orkg.contenttypes.domain.identifiers.parse
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

abstract class AuthorCreator(
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val listService: ListUseCases
) {
    internal fun create(contributorId: ContributorId, authors: List<Author>, subjectId: ThingId) {
        val authorIds = authors.map { author ->
            when {
                author.id != null -> author.id!!
                author.homepage == null && author.identifiers.isNullOrEmpty() -> {
                    createLiteralAuthor(author, contributorId)
                }
                else -> createResourceAuthor(author, contributorId)
            }
        }
        val authorList = listService.create(
            CreateListUseCase.CreateCommand(
                label = "authors list",
                elements = authorIds,
                contributorId = contributorId
            )
        )
        statementService.add(
            userId = contributorId,
            subject = subjectId,
            predicate = Predicates.hasAuthors,
            `object` = authorList
        )
    }

    private fun createLiteralAuthor(author: Author, contributorId: ContributorId): ThingId =
        literalService.create(
            userId = contributorId,
            label = author.name,
            datatype = Literals.XSD.STRING.prefixedUri
        ).id

    private fun createResourceAuthor(author: Author, contributorId: ContributorId): ThingId {
        val authorId = resourceService.create(
            CreateResourceUseCase.CreateCommand(
                label = author.name,
                classes = setOf(Classes.author),
                contributorId = contributorId
            )
        )

        // After validation, author identifiers only contain identifiers that need to be created
        if (!author.identifiers.isNullOrEmpty()) {
            val identifiers = Identifiers.author.parse(author.identifiers!!, validate = false)
            identifiers.forEach { (identifier, value) ->
                val literalId = literalService.create(
                    userId = contributorId,
                    label = value,
                    datatype = Literals.XSD.STRING.prefixedUri
                ).id
                statementService.add(
                    userId = contributorId,
                    subject = authorId,
                    predicate = identifier.predicateId,
                    `object` = literalId
                )
            }
        }

        if (author.homepage != null) {
            val homepage = literalService.create(
                userId = contributorId,
                label = author.homepage.toString(),
                datatype = Literals.XSD.URI.prefixedUri // TODO: is this correct?
            ).id
            statementService.add(
                userId = contributorId,
                subject = authorId,
                predicate = Predicates.hasWebsite,
                `object` = homepage
            )
        }

        return authorId
    }
}
