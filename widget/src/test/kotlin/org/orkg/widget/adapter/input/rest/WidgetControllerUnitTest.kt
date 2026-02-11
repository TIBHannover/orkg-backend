package org.orkg.widget.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.MissingParameter
import org.orkg.common.exceptions.TooManyParameters
import org.orkg.graph.domain.PUBLISHABLE_CLASSES
import org.orkg.graph.testing.asciidoc.Asciidoc
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectDetail
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorResponse
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectTitle
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.enumValues
import org.orkg.testing.spring.restdocs.type
import org.orkg.widget.adapter.input.rest.testing.fixtures.WidgetControllerUnitTestConfiguration
import org.orkg.widget.input.ResolveDOIUseCase
import org.orkg.widget.input.ResolveDOIUseCase.WidgetInfo
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/** An example DOI. Will resolve to the DOI handbook. */
const val EXAMPLE_DOI = "10.1000/182"

@ContextConfiguration(classes = [WidgetController::class, WidgetControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [WidgetController::class])
internal class WidgetControllerUnitTest : MockMvcBaseTest("widget") {
    @MockkBean
    private lateinit var resolveDOIUseCase: ResolveDOIUseCase

    @Test
    @DisplayName("Given a doi, when resolved successfully, it returns the widget info")
    fun findByDoiOrTitle() {
        every { resolveDOIUseCase.resolveDOI(EXAMPLE_DOI, null) } returns WidgetInfo(
            id = ThingId("R1234"),
            doi = EXAMPLE_DOI,
            title = "Some very interesting title",
            numberOfStatements = 23,
            `class` = "Paper",
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
            .andDocument {
                summary("Fetching widget info")
                description(
                    """
                    WARNING: This API is intended for internal use of the https://gitlab.com/TIBHannover/orkg/orkg-frontend/-/blob/master/widget/README.md[ORKG widget], and not meant to be consumed by clients.
                      It is documented here for completeness.
                      Client authors can ignore it.

                    The widget can obtain information via a `GET` request to `/api/widgets/` by providing one of the request parameters.
                    All request parameters are mutually exclusive.
                    Providing none or more than one will respond with status code 400 (Bad Request).
                    """
                )
                queryParameters(
                    parameterWithName("doi").description("The DOI of the resource to search.").optional(),
                    parameterWithName("title").description("The title of the resource to search.").optional(),
                )
                responseFields<WidgetInfo>(
                    fieldWithPath("id").description("The identifier of the resource."),
                    fieldWithPath("doi").description("The DOI of the resource. May be `null` if the resource does not have a DOI.").optional(),
                    fieldWithPath("title").description("The title of the resource."),
                    fieldWithPath("class").description("The class of the resource. Always one of ${Asciidoc.formatPublishableClasses()}.").type("enum").enumValues(PUBLISHABLE_CLASSES.map(ThingId::value)),
                    fieldWithPath("num_statements").description("The number of statements connected to the resource if the class is `Paper`, or 0 in all other cases.").type<Long>(),
                )
            }

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
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_parameters")
            .andExpectTitle("Bad Request")
            .andExpectDetail("Too many parameters: At most one out of \"doi\", \"title\" is allowed.")
            .andExpectErrorResponse("/api/widgets")

        verify(exactly = 1) { resolveDOIUseCase.resolveDOI(EXAMPLE_DOI, "some title") }
    }

    @Test
    @DisplayName("when no request parameter is given, it fails with 400 Bad Request")
    fun noRequestParameter() {
        // TODO: this is not ideal, as it re-implements service logic.
        every { resolveDOIUseCase.resolveDOI(null, null) } throws MissingParameter.requiresAtLeastOneOf("doi", "title")

        get("/api/widgets")
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_parameter")
            .andExpectTitle("Bad Request")
            .andExpectDetail("Missing parameter: At least one parameter out of \"doi\", \"title\" is required.")
            .andExpectErrorResponse("/api/widgets")

        verify(exactly = 1) { resolveDOIUseCase.resolveDOI(null, null) }
    }
}
