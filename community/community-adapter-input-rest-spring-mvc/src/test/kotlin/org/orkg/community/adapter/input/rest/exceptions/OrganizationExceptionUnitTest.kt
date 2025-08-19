package org.orkg.community.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.OrganizationId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.domain.InvalidImageEncoding
import org.orkg.community.domain.InvalidPeerReviewType
import org.orkg.community.domain.LogoNotFound
import org.orkg.community.domain.OrganizationAlreadyExists
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
internal class OrganizationExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun organizationAlreadyExists_withName() {
        documentedGetRequestTo(OrganizationAlreadyExists.withName("Cool name"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:organization_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Organization with name "Cool name" already exists.""")
            .andExpect(jsonPath("$.name", `is`("Cool name")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("name").type("String").description("The name of the organization that already exists. (optional, either `name` or `display_id` is present)").optional(),
                        fieldWithPath("display_id").type("String").description("The display_id of the organization that already exists. (optional, either `name` or `display_id` is present)").optional(),
                    )
                )
            )
    }

    @Test
    fun organizationAlreadyExists_withDisplayId() {
        get(OrganizationAlreadyExists.withDisplayId("cool_name"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:organization_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Organization with display id "cool_name" already exists.""")
            .andExpect(jsonPath("$.display_id", `is`("cool_name")))
    }

    @Test
    fun organizationNotFound_withId() {
        documentedGetRequestTo(OrganizationNotFound(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:organization_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Organization "f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc" not found.""")
            .andExpect(jsonPath("$.id", `is`("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("id").description("The id of the organization that could not be found. (optional, either `id` or `display_id` is present)"),
                        fieldWithPath("display_id").type("String").description("The display_id of the organization that could not be found. (optional, either `id` or `display_id` is present)").optional(),
                    )
                )
            )
    }

    @Test
    fun organizationNotFound_withDisplayId() {
        get(OrganizationNotFound("display_name"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:organization_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Organization with display id "display_name" not found.""")
            .andExpect(jsonPath("$.display_id", `is`("display_name")))
    }

    @Test
    fun logoNotFound() {
        documentedGetRequestTo(LogoNotFound(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:logo_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Logo for organization "f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidImageEncoding() {
        documentedGetRequestTo(InvalidImageEncoding())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_image_encoding")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid image encoding.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidPeerReviewType() {
        documentedGetRequestTo(InvalidPeerReviewType("not a peer review type"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_peer_review_type")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The value "not a peer review type" is not a valid peer review type.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
