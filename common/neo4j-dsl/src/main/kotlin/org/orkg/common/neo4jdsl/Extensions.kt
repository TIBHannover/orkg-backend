package org.orkg.common.neo4jdsl

import org.springframework.data.domain.Sort
import java.util.stream.Collectors

fun String.sortedWith(sort: Sort, index: Int = lastIndexOf("SKIP")): String =
    if (sort.isUnsorted) {
        this
    } else {
        StringBuilder(this)
            .insert(index, "ORDER BY ${sort.toNeo4jSnippet()} ")
            .toString()
    }

fun Sort.toNeo4jSnippet(): String =
    stream().map { it.toNeo4jSnippet() }.collect(Collectors.joining(", "))

fun Sort.Order.toNeo4jSnippet(): String = buildString {
    if (isIgnoreCase) {
        append("toLower(").append(property).append(")")
    } else {
        append(property)
    }
    append(" ").append(direction)
}
