package org.orkg.common

import org.junit.jupiter.api.Test
import org.orkg.testing.andExpectPage
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.pageableDetailedFieldParameters
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [PageDocTest.FakePageController::class])
internal class PageDocTest : RestDocsTest("paged") {
    @Test
    fun page() {
        documentedGetRequestTo("/page")
            .param("page", "0")
            .param("size", "10")
            .param("sort", "label", "desc")
            .perform()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(MockMvcResultHandlers.print())
            .andExpectPage()
            .andDo(
                documentationHandler.document(
                    queryParameters(
                        parameterWithName("page").description("The page number requested, 0-indexed.").optional(),
                        parameterWithName("size")
                            .description("The number of elements per page. May be lowered if it exceeds the limit.")
                            .optional(),
                        parameterWithName("sort")
                            .description(
                                """
                                | A string in the form "\{property},\{direction}".
                                | Sortable properties are dependent on the endpoint.
                                | Direction can be "asc" or "desc". Parameter can be repeated multiple times.
                                | The sorting is order-dependent.
                                """.trimMargin().replace("\n", "")
                            )
                            .optional(),
                    ),
                    responseFields(pageableDetailedFieldParameters())
                )
            )
            .andDo(generateDefaultDocSnippets())
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
