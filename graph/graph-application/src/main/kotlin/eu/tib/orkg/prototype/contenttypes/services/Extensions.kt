package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.contenttypes.api.Identifiers
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.domain.model.ObjectIdAndLabel
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.util.stream.Collectors
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl

internal fun <T, R> Page<T>.pmap(transform: (T) -> R): Page<R> =
    PageImpl(content.pmap(transform), pageable, totalElements)

internal fun <T, R> Collection<T>.pmap(transform: (T) -> R): List<R> =
    parallelStream().map(transform).collect(Collectors.toList())

internal fun Iterable<GeneralStatement>.wherePredicate(predicateId: ThingId) =
    filter { it.predicate.id == predicateId }

internal fun List<GeneralStatement>.objectIdsAndLabel(): List<ObjectIdAndLabel> =
    map { ObjectIdAndLabel(it.`object`.id, it.`object`.label) }
        .sortedBy { it.id }

internal fun List<GeneralStatement>.objects() = map { it.`object` }

internal fun List<GeneralStatement>.firstObjectLabel(): String? = firstOrNull()?.`object`?.label

internal fun List<GeneralStatement>.firstObjectId(): ThingId? = firstOrNull()?.`object`?.id

internal fun List<GeneralStatement>.mapIdentifiers(identifiers: Map<ThingId, String>) =
    filter { it.predicate.id in identifiers.keys }
        .associate { identifiers[it.predicate.id]!! to it.`object`.label }

internal fun List<GeneralStatement>.withoutObjectsWithBlankLabels(): List<GeneralStatement> =
    filter { it.`object`.label.isNotBlank() }

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
        identifiers = statements.mapIdentifiers(Identifiers.author),
        homepage = statements.wherePredicate(Predicates.hasWebsite).firstObjectLabel()
    )
}

internal fun Literal.toAuthor() = Author(
    id = null,
    name = label,
    identifiers = emptyMap(),
    homepage = null
)

internal fun List<GeneralStatement>.authors(statementRepository: StatementRepository): List<Author> =
    wherePredicate(Predicates.hasAuthors).firstOrNull()?.let { hasAuthors ->
        statementRepository.findAllBySubjectAndPredicate(hasAuthors.`object`.id, Predicates.hasListElement, PageRequests.ALL)
            .content
            .sortedBy { it.index }
            .objects()
            .filter { it is Resource || it is Literal }
            .pmap { it.toAuthor(statementRepository) }
    }.orEmpty()
