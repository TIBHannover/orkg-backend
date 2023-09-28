package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ExactSearchString
import eu.tib.orkg.prototype.statements.domain.model.FuzzySearchString
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import org.springframework.data.domain.Sort

internal fun String.matches(searchString: SearchString): Boolean = when (searchString) {
    is ExactSearchString -> equals(searchString.input, ignoreCase = true)
    is FuzzySearchString -> {
        val searchWords = searchString.input
            .replace(Regex("""(\w)-(\w)"""), """$1 $2""")
            .split(" ")
        val words = split(" ", "-")
        searchWords.all { searchWord ->
            when {
                searchWord.startsWith("-") -> words.all {
                    !it.startsWith(searchWord.substring(1))
                }
                searchWord.startsWith("+") -> words.any {
                    it.startsWith(searchWord.substring(1))
                }
                else -> words.any { it.startsWith(searchWord) }
            }
        }
    }
}

internal val Sort.resourceComparator: Comparator<Resource>
    get() = Comparator { a, b ->
        var result = 0
        for (order in this) {
            result = when (order.property) {
                "created_at" -> order.compare(a.createdAt, b.createdAt)
                "created_by" -> order.compare(a.createdBy.value.toString(), b.createdBy.value.toString())
                "label" -> order.compare(a.label, b.label)
                "id" -> order.compare(a.id, b.id)
                else -> 0
            }
            if (result != 0) {
                break
            }
        }
        result
    }

internal val Sort.classComparator: Comparator<Class>
    get() = Comparator { a, b ->
        var result = 0
        for (order in this) {
            result = when (order.property) {
                "created_at" -> order.compare(a.createdAt, b.createdAt)
                "created_by" -> order.compare(a.createdBy.value.toString(), b.createdBy.value.toString())
                "label" -> order.compare(a.label, b.label)
                "id" -> order.compare(a.id, b.id)
                else -> 0
            }
            if (result != 0) {
                break
            }
        }
        result
    }

internal val Sort.predicateComparator: Comparator<Predicate>
    get() = Comparator { a, b ->
        var result = 0
        for (order in this) {
            result = when (order.property) {
                "created_at" -> order.compare(a.createdAt, b.createdAt)
                "created_by" -> order.compare(a.createdBy.value.toString(), b.createdBy.value.toString())
                "label" -> order.compare(a.label, b.label)
                "id" -> order.compare(a.id, b.id)
                else -> 0
            }
            if (result != 0) {
                break
            }
        }
        result
    }

internal val Sort.literalComparator: Comparator<Literal>
    get() = Comparator { a, b ->
        var result = 0
        for (order in this) {
            result = when (order.property) {
                "created_at" -> order.compare(a.createdAt, b.createdAt)
                "created_by" -> order.compare(a.createdBy.value.toString(), b.createdBy.value.toString())
                "label" -> order.compare(a.label, b.label)
                "id" -> order.compare(a.id, b.id)
                else -> 0
            }
            if (result != 0) {
                break
            }
        }
        result
    }

internal fun <T : Comparable<T>> Sort.Order.compare(a : T?, b: T?): Int {
    val result = when {
        a == null && b == null -> 0
        a == null -> 1
        b == null -> -1
        else -> a.compareTo(b)
    }
    return if (isAscending) result else -result
}
