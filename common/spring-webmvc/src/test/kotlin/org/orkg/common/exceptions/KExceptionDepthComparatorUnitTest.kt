package org.orkg.common.exceptions

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class KExceptionDepthComparatorUnitTest {
    @Test
    fun `Given a list of exceptions, it sorts the list by exception depth`() {
        /**
         * Class hierarchy:
         * Throwable <- Exception <- RuntimeException <- IllegalStateException
         */
        val list = listOf(Exception(), RuntimeException(), IllegalStateException(), Throwable())
        val comparator = KExceptionDepthComparator(RuntimeException::class)
        val expected = listOf(RuntimeException(), Exception(), Throwable(), IllegalStateException())
        list.sortedWith { a, b -> comparator.compare(a::class, b::class) } shouldBe expected
    }
}
