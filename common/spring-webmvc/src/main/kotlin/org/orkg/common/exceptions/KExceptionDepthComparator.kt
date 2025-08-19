package org.orkg.common.exceptions

import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.full.superclasses

/**
 * This Comparator sorts classes based on the distance in the class hierarchy,
 * given a starting class, which must be a subclass of [Throwable].
 *
 * See also [org.springframework.core.ExceptionDepthComparator]
 */
class KExceptionDepthComparator(
    private val targetException: KClass<out Throwable>,
) : Comparator<KClassifier> {
    override fun compare(a: KClassifier, b: KClassifier): Int {
        val depth1 = getDepth(a, targetException, 0)
        val depth2 = getDepth(b, targetException, 0)
        return depth1 - depth2
    }

    private fun getDepth(declaredException: KClassifier, exceptionToMatch: KClass<*>, depth: Int): Int =
        when (exceptionToMatch) {
            declaredException -> depth
            Throwable::class -> Int.MAX_VALUE
            else -> exceptionToMatch.superclasses.minOf { getDepth(declaredException, it, depth + 1) }
        }
}
