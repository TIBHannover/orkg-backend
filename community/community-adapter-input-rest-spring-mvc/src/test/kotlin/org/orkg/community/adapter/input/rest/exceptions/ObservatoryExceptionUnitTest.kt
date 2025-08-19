package org.orkg.community.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.domain.ObservatoryAlreadyExists
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [ExceptionTestConfiguration::class, CommonJacksonModule::class, FixedClockConfig::class])
internal class ObservatoryExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun observatoryAlreadyExistsWithId() {
        documentedGetRequestTo(ObservatoryAlreadyExists.withId(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andPrint()
            .andExpectType("orkg:problem:observatory_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Observatory with id "eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174" already exists.""")
            .andExpect(jsonPath("$.id", `is`("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("id").description("The id of the observatory that already exists. (optional, either `id`, `name` or `display_id` is present)").optional(),
                        fieldWithPath("name").type("String").description("The name of the observatory that already exists. (optional, either `id`, `name` or `display_id` is present)").optional(),
                        fieldWithPath("display_id").type("String").description("The display_id of the observatory that already exists. (optional, either `id`, `name` or `display_id` is present)").optional(),
                    )
                )
            )
    }

    @Test
    fun observatoryAlreadyExistsWithName() {
        get(ObservatoryAlreadyExists.withName("Cool name"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:observatory_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Observatory with name "Cool name" already exists.""")
            .andExpect(jsonPath("$.name", `is`("Cool name")))
    }

    @Test
    fun observatoryAlreadyExistsWithDisplayId() {
        get(ObservatoryAlreadyExists.withDisplayId("cool_name"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:observatory_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Observatory with display id "cool_name" already exists.""")
            .andExpect(jsonPath("$.display_id", `is`("cool_name")))
    }
}
