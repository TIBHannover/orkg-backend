package org.orkg.community.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.OrganizationId
import org.orkg.community.domain.InvalidImageEncoding
import org.orkg.community.domain.InvalidPeerReviewType
import org.orkg.community.domain.LogoNotFound
import org.orkg.community.domain.OrganizationAlreadyExists
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.testing.fixtures.configuration.CommunityControllerExceptionUnitTestConfiguration
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommunityControllerExceptionUnitTestConfiguration::class])
internal class OrganizationExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun organizationAlreadyExists_withName() {
        val type = "orkg:problem:organization_already_exists"
        documentedGetRequestTo(OrganizationAlreadyExists.withName("Cool name"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Organization with name "Cool name" already exists.""")
            .andExpect(jsonPath("$.organization_name", `is`("Cool name")))
            .andDocument {
                responseFields<OrganizationAlreadyExists>(
                    fieldWithPath("organization_name").description("The name of the organization. (optional, either `organization_name` or `organization_display_id` is present)"),
                    fieldWithPath("organization_display_id").type("String").description("The display_id of the organization. (optional, either `organization_name` or `organization_display_id` is present)").optional(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun organizationAlreadyExists_withDisplayId() {
        get(OrganizationAlreadyExists.withDisplayId("cool_name"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:organization_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Organization with display id "cool_name" already exists.""")
            .andExpect(jsonPath("$.organization_display_id", `is`("cool_name")))
    }

    @Test
    fun organizationNotFound_withId() {
        val type = "orkg:problem:organization_not_found"
        documentedGetRequestTo(OrganizationNotFound(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Organization "f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc" not found.""")
            .andExpect(jsonPath("$.organization_id", `is`("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andDocument {
                responseFields<OrganizationNotFound>(
                    fieldWithPath("organization_id").description("The id of the organization. (optional, either `organization_id` or `organization_display_id` is present)").type<OrganizationId>(),
                    fieldWithPath("organization_display_id").type("String").description("The display_id of the organization. (optional, either `organization_id` or `organization_display_id` is present)").optional(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun organizationNotFound_withDisplayId() {
        get(OrganizationNotFound("display_name"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:organization_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Organization with display id "display_name" not found.""")
            .andExpect(jsonPath("$.organization_display_id", `is`("display_name")))
    }

    @Test
    fun logoNotFound() {
        val type = "orkg:problem:logo_not_found"
        documentedGetRequestTo(LogoNotFound(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Logo for organization "f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc" not found.""")
            .andExpect(jsonPath("$.organization_id", `is`("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andDocument {
                responseFields<LogoNotFound>(
                    fieldWithPath("organization_id").description("The id of the organization. (optional, either `organization_id` or `organization_display_id` is present)").type<OrganizationId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun invalidImageEncoding() {
        val type = "orkg:problem:invalid_image_encoding"
        documentedGetRequestTo(InvalidImageEncoding())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid image encoding.""")
            .andDocumentWithDefaultExceptionResponseFields<InvalidImageEncoding>(type)
    }

    @Test
    fun invalidPeerReviewType() {
        val type = "orkg:problem:invalid_peer_review_type"
        documentedGetRequestTo(InvalidPeerReviewType("not a peer review type"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The value "not a peer review type" is not a valid peer review type.""")
            .andExpect(jsonPath("$.peer_review_type", `is`("not a peer review type")))
            .andDocument {
                responseFields<InvalidPeerReviewType>(
                    fieldWithPath("peer_review_type").description("The provided peer review type."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
