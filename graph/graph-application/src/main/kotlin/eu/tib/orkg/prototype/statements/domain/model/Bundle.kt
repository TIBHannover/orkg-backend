package eu.tib.orkg.prototype.statements.domain.model

import org.springframework.data.domain.Sort

data class Bundle(
    val rootId: ThingId,
    var bundle: MutableList<GeneralStatement> = mutableListOf()
) {
    private fun addStatement(statement: GeneralStatement) {
        bundle.add(statement)
    }

    operator fun contains(statement: GeneralStatement): Boolean {
        return this.bundle.any { it.id == statement.id }
    }

    fun merge(other: Bundle, sort: Sort): Bundle {
        val newBundle = this.copy()
        other.bundle
            .filter { it !in this }
            .forEach { newBundle.addStatement(it) }
        if (sort.isUnsorted) {
            newBundle.bundle.sortByDescending { it.createdAt }
        } else {
            newBundle.bundle.sortWith(sort.comparator)
        }
        return newBundle
    }

    private val Sort.comparator: Comparator<GeneralStatement>
        get() = Comparator { a, b ->
            var result = 0
            for (order in this) {
                result = when (order.property) {
                    "created_at" -> order.compare(a.createdAt, b.createdAt)
                    "created_by" -> order.compare(a.createdBy.value, b.createdBy.value)
                    "index" -> order.compare(a.index, b.index)
                    else -> 0 // TODO: Throw exception?
                }
                if (result != 0) {
                    break
                }
            }
            result
        }

    private fun <T : Comparable<T>> Sort.Order.compare(a : T?, b: T?): Int {
        val result = when {
            a == null && b == null -> 0
            a == null -> 1
            b == null -> -1
            else -> a.compareTo(b)
        }
        return if (isAscending) result else -result
    }
}
