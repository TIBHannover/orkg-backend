package org.orkg.common

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.stream.Collectors

fun <T : Any, R : Any> Page<T>.pmap(transform: (T) -> R): Page<R> =
    PageImpl(content.pmap(transform), pageable, totalElements)

fun <T : Any, R : Any> Collection<T>.pmap(transform: (T) -> R): List<R> =
    parallelStream().map(transform).collect(Collectors.toList())

inline fun Pageable.withDefaultSort(sort: () -> Sort): Pageable =
    if (this.sort.isSorted) this else PageRequest.of(pageNumber, pageSize, sort())
