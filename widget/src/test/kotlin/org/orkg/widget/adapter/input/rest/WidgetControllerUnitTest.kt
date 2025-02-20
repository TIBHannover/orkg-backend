package org.orkg.widget.adapter.input.rest

import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.MissingParameter
import org.orkg.common.exceptions.TooManyParameters
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.testing.asciidoc.Asciidoc
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.widget.input.ResolveDOIUseCase
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/** An example DOI. Will resolve to the DOI handbook. */
const val EXAMPLE_DOI = "10.1000/182"

@ContextConfiguration(classes = [WidgetController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [WidgetController::class])
internal class WidgetControllerUnitTest : MockMvcBaseTest("widget") {
    @MockkBean
    private lateinit var resolveDOIUseCase: ResolveDOIUseCase

    @Test
    fun success() {
        every { resolveDOIUseCase.resolveDOI(EXAMPLE_DOI, null) } returns ResolveDOIUseCase.WidgetInfo(
            id = ThingId("R1234"),
            doi = EXAMPLE_DOI,
            title = "Some very interesting title",
            numberOfStatements = 23,
            `class` = "Paper",
        )

        // FIXME: Deduplicate parameter and field specification
        val queryParameters = arrayOf(
            parameterWithName("doi").description("The DOI of the resource to search.").optional(),
            parameterWithName("title").description("The title of the resource to search.").optional(),
        )
        val responseFields = arrayOf(
            // The order here determines the order in the generated table. More relevant items should be up.
            fieldWithPath("id").description("The identifier of the resource."),
            fieldWithPath("doi").description("The DOI of the resource. May be `null` if the resource does not have a DOI."),
            fieldWithPath("title").description("The title of the resource."),
            fieldWithPath("class").description("The class of the resource. Always one of ${Asciidoc.formatPublishableClasses()}."),
            fieldWithPath("num_statements").description("The number of statements connected to the resource if the class is `Paper`, or 0 in all other cases."),
        )

        documentedGetRequestTo("/api/widgets")
            .param("doi", EXAMPLE_DOI)
            .perform()
            .andExpect(status().isOk)
            // Explicitly test all properties of the representation. This works as a serialization test.
            .andExpect(jsonPath("$.id", `is`("R1234")))
            .andExpect(jsonPath("$.doi", `is`(EXAMPLE_DOI)))
            .andExpect(jsonPath("$.title", `is`("Some very interesting title")))
            .andExpect(jsonPath("$.num_statements", `is`(23)))
            .andDo(
                document(
                    identifier,
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(prefix)
                            .summary("Obtain basic information for display in the widget")
                            .description(
                                """
                                **NOTE**: This is an **internal API** for the [ORKG widget](https://gitlab.com/TIBHannover/orkg/orkg-frontend/-/blob/master/widget/README.md) and should not be used directly!
                                
                                The widget can obtain information for resources with one of the following classes: ${Asciidoc.formatPublishableClasses()}.
                                All request parameters are mutually exclusive.
                                """.trimIndent()
                            )
                            .queryParameters(*queryParameters)
                            .responseFields(*responseFields)
                            .build()
                    ),
                    queryParameters(*queryParameters),
                    responseFields(*responseFields)
                )
            )

        verify(exactly = 1) { resolveDOIUseCase.resolveDOI(EXAMPLE_DOI, null) }
    }

    @Test
    fun bothRequestParametersGivenShouldFail() {
        // TODO: this is not ideal, as it re-implements service logic.
        every { resolveDOIUseCase.resolveDOI(EXAMPLE_DOI, "some title") } throws TooManyParameters.atMostOneOf(
            "doi",
            "title"
        )

        get("/api/widgets")
            .param("doi", EXAMPLE_DOI)
            .param("title", "some title")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath(
                    "$.message",
                    `is`("Too many parameters: At most one out of \"doi\", \"title\" is allowed.")
                )
            )

        verify(exactly = 1) { resolveDOIUseCase.resolveDOI(EXAMPLE_DOI, "some title") }
    }

    @Test
    @DisplayName("when no request parameter is given, it fails with 400 Bad Request")
    fun noRequestParameter() {
        // TODO: this is not ideal, as it re-implements service logic.
        every { resolveDOIUseCase.resolveDOI(null, null) } throws MissingParameter.requiresAtLeastOneOf("doi", "title")

        get("/api/widgets")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath(
                    "$.message",
                    `is`("Missing parameter: At least one parameter out of \"doi\", \"title\" is required.")
                )
            )

        verify(exactly = 1) { resolveDOIUseCase.resolveDOI(null, null) }
    }
}
