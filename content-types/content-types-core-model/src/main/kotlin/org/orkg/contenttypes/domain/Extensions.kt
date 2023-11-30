package org.orkg.contenttypes.domain

import java.util.stream.Collectors
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.identifiers.Identifier
import org.orkg.graph.domain.GeneralStatement
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl

fun <T, R> Page<T>.pmap(transform: (T) -> R): Page<R> =
    PageImpl(content.pmap(transform), pageable, totalElements)

fun <T, R> Collection<T>.pmap(transform: (T) -> R): List<R> =
    parallelStream().map(transform).collect(Collectors.toList())

fun Iterable<GeneralStatement>.wherePredicate(predicateId: ThingId) =
    filter { it.predicate.id == predicateId }

fun List<GeneralStatement>.objectIdsAndLabel(): List<ObjectIdAndLabel> =
    map { ObjectIdAndLabel(it.`object`.id, it.`object`.label) }
        .sortedBy { it.id }

fun GeneralStatement?.objectIdAndLabel(): ObjectIdAndLabel? =
    this?.let { ObjectIdAndLabel(it.`object`.id, it.`object`.label) }

fun List<GeneralStatement>.objects() = map { it.`object` }

fun List<GeneralStatement>.firstObjectLabel(): String? = firstOrNull()?.`object`?.label

fun List<GeneralStatement>.associateIdentifiers(identifiers: Set<Identifier>): Map<String, String> =
    mapNotNull { statement ->
        identifiers.firstOrNull { statement.predicate.id == it.predicateId }
            ?.let { identifier -> identifier.id to statement.`object`.label }
    }.toMap()

fun List<GeneralStatement>.firstObjectId(): ThingId? = firstOrNull()?.`object`?.id

fun List<GeneralStatement>.withoutObjectsWithBlankLabels(): List<GeneralStatement> =
    filter { it.`object`.label.isNotBlank() }

