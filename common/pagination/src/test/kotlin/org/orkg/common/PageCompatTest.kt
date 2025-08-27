package org.orkg.common

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

internal class PageCompatTest {
    @Test
    fun `Given a list and a pageable, when taking a page from the list, and no sorting property is provided, it returns the correct result`() {
        val list = createList(1, 20)
        val pageable = PageRequest.of(1, 5)
        val expected = createList(6, 10)

        val actual = list.paged(pageable)
        actual.totalElements shouldBe 20
        actual.totalPages shouldBe 4
        actual.size shouldBe 5
        actual.number shouldBe 1
        actual.numberOfElements shouldBe 5
        actual.content shouldBe expected
    }

    @Test
    fun `Given a list and a pageable, when taking a page from the list, and sorting property exists, it returns the correct result`() {
        val list = createList(1, 20)
        val sort = Sort.by("nested.property").descending()
        val pageable = PageRequest.of(1, 5, sort)
        val expected = createList(11, 15).reversed()

        val actual = list.paged(pageable)
        actual.totalElements shouldBe 20
        actual.totalPages shouldBe 4
        actual.size shouldBe 5
        actual.number shouldBe 1
        actual.numberOfElements shouldBe 5
        actual.content shouldBe expected
    }

    @Test
    fun `Given a list and a pageable, when taking a page from the list, and sorting property does not exist, it returns the correct result`() {
        val list = createList(1, 20)
        val sort = Sort.by("does.not.exist").descending()
        val pageable = PageRequest.of(1, 5, sort)
        val expected = createList(6, 10)

        val actual = list.paged(pageable)
        actual.totalElements shouldBe 20
        actual.totalPages shouldBe 4
        actual.size shouldBe 5
        actual.number shouldBe 1
        actual.numberOfElements shouldBe 5
        actual.content shouldBe expected
    }

    private fun createList(start: Int, endIncluise: Int): List<NestedValueWrapper> =
        IntRange(start, endIncluise).map(::ValueWrapper).map(::NestedValueWrapper)

    data class ValueWrapper(
        val property: Int,
    )

    data class NestedValueWrapper(
        val nested: ValueWrapper,
    )
}
