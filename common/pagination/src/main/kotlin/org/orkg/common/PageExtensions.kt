package org.orkg.common

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import java.util.stream.Collectors

fun <T, R> Page<T>.pmap(transform: (T) -> R): Page<R> =
    PageImpl(content.pmap(transform), pageable, totalElements)

fun <T, R> Collection<T>.pmap(transform: (T) -> R): List<R> =
    parallelStream().map(transform).collect(Collectors.toList())
