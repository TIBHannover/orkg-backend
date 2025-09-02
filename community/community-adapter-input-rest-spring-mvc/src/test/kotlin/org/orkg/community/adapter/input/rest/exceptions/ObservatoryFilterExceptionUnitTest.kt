package org.orkg.community.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.community.adapter.input.rest.json.CommunityJacksonModule
import org.orkg.community.domain.InvalidFilterConfig
import org.orkg.community.domain.ObservatoryFilterAlreadyExists
import org.orkg.community.domain.ObservatoryFilterId
import org.orkg.community.domain.ObservatoryFilterNotFound
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommunityJacksonModule::class, FixedClockConfig::class])
internal class ObservatoryFilterExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun observatoryFilterNotFound() {
        val type = "orkg:problem:observatory_filter_not_found"
        documentedGetRequestTo(ObservatoryFilterNotFound(ObservatoryFilterId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Observatory filter "f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc" not found.""")
            .andExpect(jsonPath("$.observatory_filter_id", `is`("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("observatory_filter_id").description("The id of the observatory filter."),
                    )
                )
            )
    }

    @Test
    fun invalidFilterConfig() {
        val type = "orkg:problem:invalid_filter_config"
        documentedGetRequestTo(InvalidFilterConfig())
            .andExpectErrorStatus(HttpStatus.BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid filter config.""")
            .andDocumentWithDefaultExceptionResponseFields(type)
    }

    @Test
    fun observatoryFilterAlreadyExists() {
        val type = "orkg:problem:observatory_filter_already_exists"
        documentedGetRequestTo(ObservatoryFilterAlreadyExists(ObservatoryFilterId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andExpectErrorStatus(HttpStatus.BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Observatory filter "f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc" already exists.""")
            .andExpect(jsonPath("$.observatory_filter_id", `is`("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("observatory_filter_id").description("The id of the observatory filter."),
                    )
                )
            )
    }
}
