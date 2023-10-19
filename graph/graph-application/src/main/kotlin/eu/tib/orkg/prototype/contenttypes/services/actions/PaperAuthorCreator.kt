package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.contenttypes.api.Identifiers
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.identifiers.domain.parse
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.CreateListUseCase
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.api.ListUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.Literals
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId

class PaperAuthorCreator(
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val listService: ListUseCases
) : PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        val authorIds = state.authors.map { author ->
            when {
                author.id != null -> author.id
                author.homepage == null && author.identifiers.isNullOrEmpty() -> {
                    createLiteralAuthor(author, command.contributorId)
                }
                else -> createResourceAuthor(author, command.contributorId)
            }
        }
        val authorList = listService.create(
            CreateListUseCase.CreateCommand(
                label = "authors list",
                elements = authorIds,
                contributorId = command.contributorId
            )
        )
        statementService.add(
            userId = command.contributorId,
            subject = state.paperId!!,
            predicate = Predicates.hasAuthors,
            `object` = authorList
        )
        return state
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
            val identifiers = Identifiers.author.parse(author.identifiers, validate = false)
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
