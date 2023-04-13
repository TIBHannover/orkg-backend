package eu.tib.orkg.prototype.content_types.services

import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.stream.Collectors
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl

internal fun <T, R> Page<T>.pmap(action: (T) -> R): Page<R> =
    PageImpl(content.parallelStream().map(action).collect(Collectors.toList()), pageable, totalElements)

internal fun List<GeneralStatement>.wherePredicate(predicateId: ThingId) =
    filter { it.predicate.id == predicateId }

internal fun List<GeneralStatement>.objectIds() =
    map { it.`object`.thingId }

internal fun List<GeneralStatement>.firstObjectLabel(): String? =
    firstOrNull()?.`object`?.label
