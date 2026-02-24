package org.orkg.contenttypes.domain

import org.orkg.common.ThingId

sealed interface ComparisonPath<T : ComparisonPath<T>> {
    val id: ThingId
    val type: Type
    val children: List<T>

    enum class Type {
        PREDICATE,
        ROSETTA_STONE_STATEMENT,
        ROSETTA_STONE_STATEMENT_VALUE,
    }

    companion object {
        fun matches(a: ComparisonPath<*>, b: ComparisonPath<*>): Boolean = a.id == b.id &&
            a.type == b.type &&
            a.children.size == b.children.size &&
            a.children.zip(b.children).all { (ac, bc) -> matches(ac, bc) }

        fun matches(a: List<ComparisonPath<*>>, b: List<ComparisonPath<*>>): Boolean =
            a.size == b.size && a.zip(b).all { (ac, bc) -> matches(ac, bc) }
    }
}
