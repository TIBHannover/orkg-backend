package org.orkg.common.neo4jdsl

import java.time.OffsetDateTime
import java.util.*
import java.util.stream.Collectors
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

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

fun StringBuilder.appendOrderByOptimizations(pageable: Pageable, createdAt: OffsetDateTime?, createdBy: UUID?) {
    val properties = pageable.sort.map { it.property }

    if (createdAt == null && "created_at" in properties) {
        append(" AND n.created_at IS NOT NULL")
    }
    if (createdBy == null && "created_by" in properties) {
        append(" AND n.created_by IS NOT NULL")
    }
    if ("id" in properties) {
        append(" AND n.id IS NOT NULL")
    }
    if ("label" in properties) {
        append(" AND n.label IS NOT NULL")
    }
}
