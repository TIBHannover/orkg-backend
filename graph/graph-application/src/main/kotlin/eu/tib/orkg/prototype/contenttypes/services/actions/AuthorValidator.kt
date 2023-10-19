package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.api.Identifiers
import eu.tib.orkg.prototype.contenttypes.application.AmbiguousAuthor
import eu.tib.orkg.prototype.contenttypes.application.AuthorNotFound
import eu.tib.orkg.prototype.identifiers.domain.parse
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository

class AuthorValidator(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository
) : PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        val authors = command.authors.distinct().map { author ->
            val resources: MutableSet<ThingId> = mutableSetOf()
            val missingIdentifiers = mutableMapOf<String, String>()
            if (author.id != null) {
                resources += resourceRepository.findById(author.id)
                    .filter { thing -> Classes.author in thing.classes }
                    .orElseThrow { AuthorNotFound(author.id) }
                    .id
            }
            val identifiers = Identifiers.author.parse(author.identifiers.orEmpty())
            identifiers.forEach { (identifier, value) ->
                val authors = statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                    predicateId = identifier.predicateId,
                    literal = value,
                    subjectClass = Classes.author,
                    pageable = PageRequests.ALL
                )
                if (authors.isEmpty) {
                    missingIdentifiers[identifier.id] = value
                } else {
                    authors.forEach {
                        if (resources.isNotEmpty() && it.subject.id !in resources) {
                            throw AmbiguousAuthor(author)
                        }
                        resources += it.subject.id
                    }
                }
            }
            author.copy(
                id = resources.singleOrNull(),
                identifiers = missingIdentifiers.ifEmpty { null }
            )
        }
        return state.copy(authors = authors)
    }
}
