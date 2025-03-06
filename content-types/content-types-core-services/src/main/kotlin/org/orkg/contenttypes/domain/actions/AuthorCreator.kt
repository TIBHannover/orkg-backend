package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.identifiers.Identifiers
import org.orkg.contenttypes.domain.identifiers.parse
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

abstract class AuthorCreator(
    protected val unsafeResourceUseCases: UnsafeResourceUseCases,
    protected val unsafeStatementUseCases: UnsafeStatementUseCases,
    protected val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    protected val listService: ListUseCases,
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
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = subjectId,
                predicateId = Predicates.hasAuthors,
                objectId = authorList
            )
        )
    }

    private fun createLiteralAuthor(author: Author, contributorId: ContributorId): ThingId =
        unsafeLiteralUseCases.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = contributorId,
                label = author.name
            )
        )

    private fun createResourceAuthor(author: Author, contributorId: ContributorId): ThingId {
        val authorId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = author.name,
                classes = setOf(Classes.author)
            )
        )

        if (!author.identifiers.isNullOrEmpty()) {
            createIdentifiers(authorId, author.identifiers!!, contributorId)
        }

        if (author.homepage != null) {
            val homepage = unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = author.homepage.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = authorId,
                    predicateId = Predicates.hasWebsite,
                    objectId = homepage
                )
            )
        }

        return authorId
    }

    private fun createIdentifiers(
        authorId: ThingId,
        missingIdentifiers: Map<String, List<String>>,
        contributorId: ContributorId,
    ) {
        val identifiers = Identifiers.author.parse(missingIdentifiers, validate = false)
        identifiers.forEach { (identifier, values) ->
            values.forEach { value ->
                val literalId = unsafeLiteralUseCases.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = contributorId,
                        label = value
                    )
                )
                unsafeStatementUseCases.create(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = contributorId,
                        subjectId = authorId,
                        predicateId = identifier.predicateId,
                        objectId = literalId
                    )
                )
            }
        }
    }
}
