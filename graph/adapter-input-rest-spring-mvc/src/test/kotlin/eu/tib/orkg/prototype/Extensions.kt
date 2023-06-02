package eu.tib.orkg.prototype.statements.application

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

internal fun <T> pageOf(vararg values: T): Page<T> = pageOf(Pageable.unpaged(), *values)

internal fun <T> pageOf(pageable: Pageable, vararg values: T): Page<T> {
    val list = listOf(*values)
    return PageImpl(list, pageable, list.size.toLong())
}
