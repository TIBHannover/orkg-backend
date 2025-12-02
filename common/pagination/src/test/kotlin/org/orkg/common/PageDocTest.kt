package org.orkg.common

import org.junit.jupiter.api.Test
import org.orkg.testing.andExpectPage
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [PageDocTest.FakePageController::class])
internal class PageDocTest : MockMvcBaseTest("paged") {
    @Test
    fun page() {
        documentedGetRequestTo("/page")
            .param("page", "0")
            .param("size", "10")
            .param("sort", "label", "desc")
            .perform()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpectPage()
            .andDocument {
                pagedQueryParameters()
                pagedResponseFields<Any>(emptyList())
            }
    }

    @TestComponent
    @RestController
    internal class FakePageController {
        @GetMapping("/page")
        fun dummyPage(pageable: Pageable): Page<String> {
            val content = listOf("foo", "bar", "baz").sorted()
            return PageImpl(content, pageable, content.size.toLong())
        }
    }
}
