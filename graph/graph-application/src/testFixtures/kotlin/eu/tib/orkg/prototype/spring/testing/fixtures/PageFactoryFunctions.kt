package eu.tib.orkg.prototype.spring.testing.fixtures

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

fun <T> pageOf(vararg values: T, pageable: Pageable = Pageable.unpaged()): Page<T> =
    pageOf(listOf(*values), pageable)

fun <T> pageOf(values: List<T>, pageable: Pageable = Pageable.unpaged()): Page<T> =
    PageImpl(values, pageable, values.size.toLong())
