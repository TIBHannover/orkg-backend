package org.orkg.community.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.community.domain.ObservatoryAlreadyExists
import org.orkg.community.domain.ObservatoryMemberNotFound
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import orkg.orkg.community.testing.fixtures.configuration.CommunityControllerExceptionUnitTestConfiguration
import java.util.UUID

@WebMvcTest
@ContextConfiguration(classes = [CommunityControllerExceptionUnitTestConfiguration::class])
internal class ObservatoryExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun observatoryAlreadyExists_withId() {
        val type = "orkg:problem:observatory_already_exists"
        documentedGetRequestTo(ObservatoryAlreadyExists.withId(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Observatory with id "eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174" already exists.""")
            .andExpect(jsonPath("$.observatory_id", `is`("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")))
            .andDocument {
                responseFields<ObservatoryAlreadyExists>(
                    fieldWithPath("observatory_id").description("The id of the observatory that already exists. (optional, either `observatory_id`, `observatory_name` or `observatory_display_id` is present)").type<ObservatoryId>(),
                    fieldWithPath("observatory_name").type("String").description("The name of the observatory that already exists. (optional, either `observatory_id `observatory_name` or `observatory_display_id` is present)").optional(),
                    fieldWithPath("observatory_display_id").type("String").description("The display_id of the observatory that already exists. (optional, either `observatory_id`, `observatory_name` or `observatory_display_id` is present)").optional(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun observatoryAlreadyExists_withName() {
        get(ObservatoryAlreadyExists.withName("Cool name"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:observatory_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Observatory with name "Cool name" already exists.""")
            .andExpect(jsonPath("$.observatory_name", `is`("Cool name")))
    }

    @Test
    fun observatoryAlreadyExists_withDisplayId() {
        get(ObservatoryAlreadyExists.withDisplayId("cool_name"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:observatory_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Observatory with display id "cool_name" already exists.""")
            .andExpect(jsonPath("$.observatory_display_id", `is`("cool_name")))
    }

    @Test
    fun observatoryNotFound_withId() {
        val type = "orkg:problem:observatory_not_found"
        documentedGetRequestTo(ObservatoryNotFound(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Observatory "eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174" not found.""")
            .andExpect(jsonPath("$.observatory_id", `is`("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")))
            .andDocument {
                responseFields<ObservatoryNotFound>(
                    fieldWithPath("observatory_id").description("The id of the observatory. (optional, either `observatory_id` or `observatory_display_id` is present)").type<ObservatoryId>(),
                    fieldWithPath("observatory_display_id").type("String").description("The display_id of the observatory. (optional, either `observatory_id` or `observatory_display_id` is present)").optional(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun observatoryNotFound_withDisplayId() {
        get(ObservatoryNotFound("display_name"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:observatory_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Observatory with display id "display_name" not found.""")
            .andExpect(jsonPath("$.observatory_display_id", `is`("display_name")))
    }

    @Test
    fun observatoryMemberNotFound() {
        val type = "orkg:problem:observatory_member_not_found"
        documentedGetRequestTo(ObservatoryMemberNotFound(UUID.fromString("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Observatory member "eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174" not found.""")
            .andExpect(jsonPath("$.contributor_id", `is`("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")))
            .andDocument {
                responseFields<ObservatoryMemberNotFound>(
                    fieldWithPath("contributor_id").description("The id of the contributor.").type<ContributorId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
