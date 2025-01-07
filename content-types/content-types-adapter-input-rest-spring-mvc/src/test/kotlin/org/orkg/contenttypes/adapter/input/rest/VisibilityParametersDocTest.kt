package org.orkg.contenttypes.adapter.input.rest

import org.junit.jupiter.api.Test
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.testing.asciidoc.visibilityFilterRequestParameter
import org.orkg.graph.testing.asciidoc.legacyVisibilityFilterRequestParameters
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@ContextConfiguration(classes = [VisibilityParametersDocTest.VisibilityRequestParametersDummyController::class])
@WebMvcTest(controllers = [VisibilityParametersDocTest.VisibilityRequestParametersDummyController::class])
internal class VisibilityParametersDocTest : RestDocsTest("visibility") {
    @Test
    fun legacyRequestParams() {
        documentedGetRequestTo("/test/visibility/legacy")
            .param("featured", "true")
            .param("unlisted", "false")
            .perform()
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    queryParameters(
                        *legacyVisibilityFilterRequestParameters(),
                        // Intentionally ignore the "visibility" parameter, so it does not appear in the snippet
                        parameterWithName("visibility").optional().ignored(),
                    )
                )
            )
    }

    @Test
    fun requestParams() {
        documentedGetRequestTo("/test/visibility")
            .param("visibility", "ALL_LISTED")
            .perform()
            .andExpect(status().isNoContent)
            .andDo(documentationHandler.document(queryParameters(visibilityFilterRequestParameter())))
    }

    @RestController
    internal class VisibilityRequestParametersDummyController {
        @GetMapping("/test/visibility/legacy")
        fun get(
            @RequestParam("featured", required = false, defaultValue = "false")
            featured: Boolean,
            @RequestParam("unlisted", required = false, defaultValue = "false")
            unlisted: Boolean,
            @RequestParam("visibility", required = false)
            visibility: VisibilityFilter?,
        ): ResponseEntity<Unit> = ResponseEntity<Unit>(HttpStatus.NO_CONTENT)

        @GetMapping("/test/visibility")
        fun get(
            @RequestParam("visibility", required = false)
            visibility: VisibilityFilter?,
        ): ResponseEntity<Unit> = ResponseEntity<Unit>(HttpStatus.NO_CONTENT)
    }
}
