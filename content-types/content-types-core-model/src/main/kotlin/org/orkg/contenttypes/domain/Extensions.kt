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
    map { it.objectIdAndLabel() }.sortedBy { it.id }

fun GeneralStatement.objectIdAndLabel(): ObjectIdAndLabel =
    ObjectIdAndLabel(`object`.id, `object`.label)

fun List<GeneralStatement>.objects() = map { it.`object` }

fun List<GeneralStatement>.firstObjectLabel(): String? = firstOrNull()?.`object`?.label

fun List<GeneralStatement>.singleObjectLabel(): String? = singleOrNull()?.`object`?.label

fun List<GeneralStatement>.associateIdentifiers(identifiers: Set<Identifier>): Map<String, List<String>> =
    mapNotNull { statement ->
        identifiers.firstOrNull { statement.predicate.id == it.predicateId }
            ?.let { identifier -> identifier.id to statement.`object` }
    }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, value) -> value.sortedBy { it.id }.map { it.label }.distinct() }

fun List<GeneralStatement>.withoutObjectsWithBlankLabels(): List<GeneralStatement> =
    filter { it.`object`.label.isNotBlank() }
