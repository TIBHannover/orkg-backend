package org.orkg.graph.application

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.MissingParameter
import org.orkg.common.exceptions.TooManyParameters
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.input.ResolveDOIUseCase
import org.orkg.graph.testing.asciidoc.Asciidoc
import org.orkg.testing.annotations.UsesMocking
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.widget.adapter.input.rest.WidgetController
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/** An example DOI. Will resolve to the DOI handbook. */
const val EXAMPLE_DOI = "10.1000/182"

@ContextConfiguration(classes = [WidgetController::class, ExceptionHandler::class, CommonJacksonModule::class])
@WebMvcTest(controllers = [WidgetController::class])
@UsesMocking
class WidgetControllerUnitTest : RestDocsTest("widget") {

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

        mockMvc.perform(documentedGetRequestTo("/api/widgets/?doi={doi}", EXAMPLE_DOI))
            .andExpect(status().isOk)
            // Explicitly test all properties of the representation. This works as a serialization test.
            .andExpect(jsonPath("$.id", `is`("R1234")))
            .andExpect(jsonPath("$.doi", `is`(EXAMPLE_DOI)))
            .andExpect(jsonPath("$.title", `is`("Some very interesting title")))
            .andExpect(jsonPath("$.num_statements", `is`(23)))
            .andDo(
                documentationHandler.document(
                    requestParameters(
                        parameterWithName("doi").description("The DOI of the resource to search."),
                        parameterWithName("title").description("The title of the resource to search.").optional(),
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the resource."),
                        fieldWithPath("doi").description("The DOI of the resource. May be `null` if the resource does not have a DOI."),
                        fieldWithPath("title").description("The title of the resource."),
                        fieldWithPath("class").description("The class of the resource. Always one of ${Asciidoc.formatPublishableClasses()}."),
                        fieldWithPath("num_statements").description("The number of statements connected to the resource if the class is `Paper`, or 0 in all other cases."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun bothRequestParametersGivenShouldFail() {
        // TODO: this is not ideal, as it re-implements service logic.
        every { resolveDOIUseCase.resolveDOI(EXAMPLE_DOI, "some title") } throws TooManyParameters.atMostOneOf(
            "doi",
            "title"
        )

        mockMvc.perform(documentedGetRequestTo("/api/widgets/?doi={doi}&title={title}", EXAMPLE_DOI, "some title"))
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath(
                    "$.message",
                    `is`("Too many parameters: At most one out of \"doi\", \"title\" is allowed.")
                )
            )
    }

    @Test
    fun noRequestParameterGivenShouldFail() {
        // TODO: this is not ideal, as it re-implements service logic.
        every { resolveDOIUseCase.resolveDOI(null, null) } throws MissingParameter.requiresAtLeastOneOf("doi", "title")

        mockMvc.perform(documentedGetRequestTo("/api/widgets/"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath(
                    "$.message",
                    `is`("Missing parameter: At least one parameter out of \"doi\", \"title\" is required.")
                )
            )
    }
}
