package org.orkg.contenttypes.domain.actions

import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.AmbiguousAuthor
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.AuthorNotFound
import org.orkg.contenttypes.domain.Identifiers
import org.orkg.contenttypes.domain.identifiers.parse
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

abstract class AuthorValidator(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository
) {
    internal fun validate(authors: List<Author>): List<Author> {
        val validatedAuthors = authors.distinct().map { author ->
            val resources: MutableSet<ThingId> = mutableSetOf()
            val missingIdentifiers = mutableMapOf<String, MutableSet<String>>()
            if (author.id != null) {
                resources += resourceRepository.findById(author.id!!)
                    .filter { thing -> Classes.author in thing.classes }
                    .orElseThrow { AuthorNotFound(author.id!!) }
                    .id
            }
            val identifiers = Identifiers.author.parse(author.identifiers.orEmpty())
            identifiers.forEach { (identifier, values) ->
                values.forEach { value ->
                    val authorsWithIdentifier = statementRepository.findAll(
                        subjectClasses = setOf(Classes.author),
                        predicateId = identifier.predicateId,
                        objectClasses = setOf(Classes.literal),
                        objectLabel = value,
                        pageable = PageRequests.ALL
                    )
                    if (authorsWithIdentifier.isEmpty) {
                        missingIdentifiers.computeIfAbsent(identifier.id) { mutableSetOf() } += value
                    } else {
                        authorsWithIdentifier.forEach {
                            if (resources.isNotEmpty() && it.subject.id !in resources) {
                                throw AmbiguousAuthor(author)
                            }
                            resources += it.subject.id
                        }
                    }
                }
            }
            author.copy(
                id = resources.singleOrNull(),
                identifiers = missingIdentifiers
                    .mapValues { (_, value) -> value.toList() }
                    .ifEmpty { null }
            )
        }
        return validatedAuthors
    }
}
