package org.orkg.graph.output

import java.net.URI
import org.orkg.common.PageRequests
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

internal fun Thing.toAuthor(statementRepository: StatementRepository): Author {
    return when (this) {
        is Resource -> toAuthor(statementRepository)
        is Literal -> toAuthor()
        else -> throw IllegalStateException("""Cannot convert "$id" to author. This is a bug!""")
    }
}

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

internal fun Literal.toAuthor() = Author(
    id = null,
    name = label,
    identifiers = emptyMap(),
    homepage = null
)

fun List<GeneralStatement>.authors(statementRepository: StatementRepository): List<Author> =
    wherePredicate(Predicates.hasAuthors).firstOrNull()?.let { hasAuthors ->
        statementRepository.findAllBySubjectAndPredicate(hasAuthors.`object`.id, Predicates.hasListElement, PageRequests.ALL)
            .content
            .sortedBy { it.index }
            .objects()
            .filter { it is Resource || it is Literal }
            .pmap { it.toAuthor(statementRepository) }
    }.orEmpty()
