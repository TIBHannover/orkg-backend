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
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

abstract class AuthorCreator(
    protected val resourceService: ResourceUseCases,
    protected val statementService: StatementUseCases,
    protected val literalService: LiteralUseCases,
    protected val listService: ListUseCases
) {
    internal fun create(contributorId: ContributorId, authors: List<Author>, subjectId: ThingId) {
        val authorIds = authors.map { author ->
            when {
                author.id != null -> {
                    // After validation, author identifiers only contain identifiers that need to be created
                    if (!author.identifiers.isNullOrEmpty()) {
                        createIdentifiers(author.id!!, author.identifiers!!, contributorId)
                    }
                    author.id!!
                }

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
            CreateCommand(
                contributorId = contributorId,
                label = author.name
            )
        )

    private fun createResourceAuthor(author: Author, contributorId: ContributorId): ThingId {
        val authorId = resourceService.createUnsafe(
            CreateResourceUseCase.CreateCommand(
                label = author.name,
                classes = setOf(Classes.author),
                contributorId = contributorId
            )
        )

        if (!author.identifiers.isNullOrEmpty()) {
            createIdentifiers(authorId, author.identifiers!!, contributorId)
        }

        if (author.homepage != null) {
            val homepage = literalService.create(
                CreateCommand(
                    contributorId = contributorId,
                    label = author.homepage.toString(),
                    datatype = Literals.XSD.URI.prefixedUri // TODO: is this correct?
                )
            )
            statementService.add(
                userId = contributorId,
                subject = authorId,
                predicate = Predicates.hasWebsite,
                `object` = homepage
            )
        }

        return authorId
    }

    private fun createIdentifiers(
        authorId: ThingId,
        missingIdentifiers: Map<String, List<String>>,
        contributorId: ContributorId
    ) {
        val identifiers = Identifiers.author.parse(missingIdentifiers, validate = false)
        identifiers.forEach { (identifier, values) ->
            values.forEach { value ->
                val literalId = literalService.create(
                    CreateCommand(
                        contributorId = contributorId,
                        label = value
                    )
                )
                statementService.add(
                    userId = contributorId,
                    subject = authorId,
                    predicate = identifier.predicateId,
                    `object` = literalId
                )
            }
        }
    }
}
