package org.orkg.graph.output

import java.net.URI
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.Identifiers
import org.orkg.contenttypes.domain.associateIdentifiers
import org.orkg.contenttypes.domain.firstObjectLabel
import org.orkg.contenttypes.domain.objects
import org.orkg.contenttypes.domain.pmap
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.contenttypes.domain.withoutObjectsWithBlankLabels
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing

@Deprecated("To be replaced", replaceWith = ReplaceWith("Thing.toAuthor(statements: List<GeneralStatement>): Author"))
internal fun Thing.toAuthor(statementRepository: StatementRepository): Author {
    return when (this) {
        is Resource -> toAuthor(statementRepository)
        is Literal -> toAuthor()
        else -> throw IllegalStateException("""Cannot convert "$id" to author. This is a bug!""")
    }
}

internal fun Thing.toAuthor(statements: List<GeneralStatement>): Author {
    return when (this) {
        is Resource -> toAuthor(statements.withoutObjectsWithBlankLabels())
        is Literal -> toAuthor()
        else -> throw IllegalStateException("""Cannot convert "$id" to author. This is a bug!""")
    }
}

@Deprecated("To be replaced", replaceWith = ReplaceWith("Resource.toAuthor(statements: List<GeneralStatement>): Author"))
internal fun Resource.toAuthor(statementRepository: StatementRepository): Author {
    val statements = statementRepository.findAllBySubject(this.id, PageRequests.ALL).content
        .withoutObjectsWithBlankLabels()
    return Author(
        id = id,
        name = label,
        identifiers = statements.associateIdentifiers(Identifiers.author),
        homepage = statements.wherePredicate(Predicates.hasWebsite).firstObjectLabel()?.let { URI.create(it) }
    )
}

internal fun Resource.toAuthor(statements: List<GeneralStatement>): Author {
    return Author(
        id = id,
        name = label,
        identifiers = statements.associateIdentifiers(Identifiers.author),
        homepage = statements.wherePredicate(Predicates.hasWebsite).firstObjectLabel()?.let { URI.create(it) }
    )
}

internal fun Literal.toAuthor() = Author(
    id = null,
    name = label,
    identifiers = emptyMap(),
    homepage = null
)

@Deprecated("To be replaced", replaceWith = ReplaceWith("Map<ThingId, List<GeneralStatement>>.authors(subjectId: ThingId)"))
fun List<GeneralStatement>.authors(statementRepository: StatementRepository): List<Author> =
    wherePredicate(Predicates.hasAuthors).firstOrNull()?.let { hasAuthors ->
        statementRepository.findAllBySubjectAndPredicate(hasAuthors.`object`.id, Predicates.hasListElement, PageRequests.ALL)
            .content
            .sortedBy { it.index }
            .objects()
            .filter { it is Resource || it is Literal }
            .pmap { it.toAuthor(statementRepository) }
    }.orEmpty()

fun Map<ThingId, List<GeneralStatement>>.authors(subjectId: ThingId): List<Author> =
    this[subjectId]
        ?.singleOrNull { it.predicate.id == Predicates.hasAuthors }
        ?.let { this[it.`object`.id] }
        ?.filter { it.predicate.id == Predicates.hasListElement }
        ?.sortedBy { it.index }
        ?.objects()
        ?.filter { it is Resource || it is Literal }
        ?.map { it.toAuthor(this[it.id].orEmpty()) }
        .orEmpty()
