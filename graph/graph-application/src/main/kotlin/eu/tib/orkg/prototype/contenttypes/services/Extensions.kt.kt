package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.contenttypes.domain.model.ObjectIdAndLabel
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.ThingId
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

internal fun List<GeneralStatement>.objects() = map { it.`object` }

internal fun List<GeneralStatement>.firstObjectLabel(): String? = firstOrNull()?.`object`?.label

internal fun List<GeneralStatement>.mapIdentifiers(identifiers: Map<ThingId, String>) =
    filter { it.predicate.id in identifiers.keys }
        .associate { identifiers[it.predicate.id]!! to it.`object`.label }

internal fun List<GeneralStatement>.withoutObjectsWithBlankLabels(): List<GeneralStatement> =
    filter { it.`object`.label.isNotBlank() }
