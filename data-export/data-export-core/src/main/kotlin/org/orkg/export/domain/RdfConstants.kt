package org.orkg.export.domain

object RdfConstants {
    @Suppress("HttpUrlsUsage")
    const val VOCAB_PREFIX = "http://orkg.org/orkg"

    const val RESOURCE_NS = "$VOCAB_PREFIX/resource/"
    const val PREDICATE_NS = "$VOCAB_PREFIX/predicate/"
    const val CLASS_NS = "$VOCAB_PREFIX/class/"
}
