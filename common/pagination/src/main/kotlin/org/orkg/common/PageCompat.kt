package org.orkg.common

import org.springframework.beans.BeanUtils.getPropertyDescriptor
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

fun <T : Any> List<T>.paged(pageable: Pageable): Page<T> =
    PageImpl(
        sortedWith(pageable.sort.toComparator())
            .drop(pageable.pageNumber * pageable.pageSize)
            .take<T>(pageable.pageSize),
        PageRequest.of(pageable.pageNumber, pageable.pageSize),
        size.toLong()
    )

internal fun <T : Any> Sort.toComparator(): Comparator<T?> =
    Comparator.nullsFirst(
        stream().map { it.toComparator<T, Comparable<Any>>() }
            .reduce { a, b -> a.thenComparing(b) }
            .map { Comparator.nullsFirst(it) }
            .orElseGet { Comparator<T?> { _, _ -> 0 } }
    )

internal fun <T : Any, U : Comparable<U>> Sort.Order.toComparator(): Comparator<T> {
    val keyExtractor: (T) -> U? = keyExtractor(property.split('.'))
    val comparator = Comparator.comparing(keyExtractor, nullSafeComparator())
    return if (isAscending) comparator else comparator.reversed()
}

@Suppress("UNCHECKED_CAST")
private fun <T : Any, U> keyExtractor(path: List<String>): (T) -> U? =
    lambda@{ entity ->
        var innerValue: Any? = entity
        for (propertyName in path) {
            innerValue = innerValue!![propertyName] ?: return@lambda null
        }
        innerValue as U?
    }

private fun <T : Comparable<T>> nullSafeComparator(): Comparator<T?> =
    Comparator.nullsFirst<T>(Comparator.naturalOrder<T>())

private operator fun Any.get(propertyName: String): Any? =
    getPropertyDescriptor(javaClass, propertyName)?.let {
        try {
            it.getReadMethod()(this)
        } catch (_: Throwable) {
            null
        }
    }
