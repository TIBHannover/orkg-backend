package org.orkg.community.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.OrganizationId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.domain.LogoNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.domain.OrganizationType
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.asciidoc.allowedOrganizationTypeValues
import org.orkg.community.testing.fixtures.createOrganization
import org.orkg.graph.input.ResourceUseCases
import org.orkg.mediastorage.domain.ImageId
import org.orkg.mediastorage.domain.InvalidImageData
import org.orkg.mediastorage.domain.InvalidMimeType
import org.orkg.mediastorage.input.ImageUseCases
import org.orkg.mediastorage.testing.fixtures.loadImage
import org.orkg.mediastorage.testing.fixtures.loadRawImage
import org.orkg.mediastorage.testing.fixtures.testImage
import org.orkg.testing.annotations.TestWithMockCurator
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.partWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParts
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [OrganizationController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [OrganizationController::class])
internal class OrganizationControllerUnitTest : MockMvcBaseTest("organizations") {

    @MockkBean
    private lateinit var organizationService: OrganizationUseCases

    @MockkBean
    private lateinit var observatoryService: ObservatoryUseCases

    @MockkBean
    private lateinit var imageService: ImageUseCases

    @MockkBean
    private lateinit var resourceService: ResourceUseCases

    @MockkBean
    private lateinit var organizationRepository: OrganizationRepository

    @Test
    fun `Given a logo is fetched, when service succeeds, then status is 200 OK and logo is returned`() {
        val id = OrganizationId(UUID.randomUUID())
        val image = loadImage(testImage)

        every { organizationService.findLogo(id) } returns Optional.of(image)

        get("/api/organizations/{id}/logo", id)
            .perform()
            .andExpect(status().isOk)
            .andExpect(content().contentType(image.mimeType.toString()))
            .andExpect(content().bytes(image.data.bytes))

        verify(exactly = 1) { organizationService.findLogo(id) }
    }

    @Test
    fun `Given a logo is fetched, when service reports logo not found, then status is 404 NOT FOUND`() {
        val id = OrganizationId(UUID.randomUUID())

        every { organizationService.findLogo(id) } throws LogoNotFound(id)

        get("/api/organizations/{id}/logo", id)
            .perform()
            .andExpect(status().isNotFound)

        verify(exactly = 1) { organizationService.findLogo(id) }
    }

    @Test
    fun `Given a logo is fetched, when service reports organization not found, then status is 404 NOT FOUND`() {
        val id = OrganizationId(UUID.randomUUID())

        every { organizationService.findLogo(id) } throws OrganizationNotFound(id)

        get("/api/organizations/{id}/logo", id)
            .perform()
            .andExpect(status().isNotFound)

        verify(exactly = 1) { organizationService.findLogo(id) }
    }

    @Test
    fun `Given an organization is fetched, when service succeeds, then status 200 OK and the organization is returned`() {
        val id = OrganizationId(UUID.randomUUID())
        val logoId = ImageId(UUID.randomUUID())
        val organization = createOrganization().copy(
            id = id,
            logoId = logoId
        )

        every { organizationService.findById(id) } returns Optional.of(organization)

        get("/api/organizations/{id}", id)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("$id"))
            .andExpect(jsonPath("$.logo_id").doesNotExist())

        verify(exactly = 1) { organizationService.findById(id) }
    }

    @Test
    @TestWithMockCurator
    fun `Given an organization is updated, when service reports organization not found, then status is 404 NOT FOUND`() {
        val id = OrganizationId(UUID.randomUUID())
        val body = mapOf(
            "name" to "irrelevant"
        )

        every { organizationService.update(any(), any()) } throws OrganizationNotFound(id)

        patchMultipart("/api/organizations/{id}", id)
            .json("properties", body)
            .perform()
            .andExpect(status().isNotFound)

        verify(exactly = 1) { organizationService.update(any(), any()) }
    }

    @Test
    @TestWithMockCurator
    fun `Given an organization is updated, when service reports invalid mime type for the logo, then status is 400 BAD REQUEST`() {
        val id = OrganizationId(UUID.randomUUID())
        val body = mapOf(
            "name" to "irrelevant"
        )
        val exception = InvalidMimeType("irrelevant")

        every { organizationService.update(any(), any()) } throws exception

        patchMultipart("/api/organizations/{id}", id)
            .json("properties", body)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/organizations/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { organizationService.update(any(), any()) }
    }

    @Test
    @TestWithMockCurator
    fun `Given an organization is updated, when service reports invalid image data for the logo, then status is 400 BAD REQUEST`() {
        val id = OrganizationId(UUID.randomUUID())
        val body = mapOf(
            "name" to "irrelevant"
        )
        val exception = InvalidImageData()

        every { organizationService.update(any(), any()) } throws exception

        patchMultipart("/api/organizations/{id}", id)
            .json("properties", body)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/organizations/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { organizationService.update(any(), any()) }
    }

    @Test
    @TestWithMockCurator
    fun `Given an organization is updated, when service succeeds, then status is 204 NO CONTENT`() {
        val id = OrganizationId(UUID.randomUUID())
        val body = mapOf(
            "name" to "irrelevant"
        )

        every { organizationService.update(any(), any()) } returns Unit

        patchMultipart("/api/organizations/{id}", id)
            .json("properties", body)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(content().string(""))

        verify(exactly = 1) { organizationService.update(any(), any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given an organization is updated, when name is invalid, then status is 400 BAD REQUEST`() {
        val id = OrganizationId(UUID.randomUUID())
        val body = mapOf(
            "name" to ""
        )

        patchMultipart("/api/organizations/{id}", id)
            .json("properties", body)
            .perform()
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { organizationService.update(any(), any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given an organization is updated, when url is invalid, then status is 400 BAD REQUEST`() {
        val id = OrganizationId(UUID.randomUUID())
        val body = mapOf(
            "url" to ""
        )

        patchMultipart("/api/organizations/{id}", id)
            .json("properties", body)
            .perform()
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { organizationService.update(any(), any()) }
    }

    @Test
    @TestWithMockCurator
    @DisplayName("Given an organization is updated, when payload contains json and logo, then status is 204 NO CONTENT")
    fun update() {
        val id = OrganizationId("2224e276-5f32-483a-9f7a-441fe6ae7856")
        val image = loadRawImage(testImage)
        val body = mapOf(
            "name" to "Organization",
            "url" to "https://example.com",
            "type" to OrganizationType.GENERAL
        )

        every { organizationService.update(any(), any()) } returns Unit

        documentedPatchMultipart("/api/organizations/{id}", id)
            .json("properties", body)
            .file(MockMultipartFile("logo", "image.png", image.mimeType.toString(), image.data.bytes))
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/organizations/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated organization can be fetched from.")
                    ),
                    pathParameters(
                        parameterWithName("id").description("The identifier of the organization.")
                    ),
                    requestParts(
                        partWithName("properties").description("The updated properties of the organization. (optional)"),
                        partWithName("logo").description("The updated logo of the organization. (optional)")
                    ),
                    requestPartFields(
                        "properties",
                        fieldWithPath("name").description("The updated name of the organization. (optional)"),
                        fieldWithPath("url").description("The updated URL of the organization. (optional)"),
                        fieldWithPath("type").description("The updated type of the organization. One of $allowedOrganizationTypeValues. (optional)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { organizationService.update(any(), any()) }
    }

    @Test
    @TestWithMockCurator
    fun `Given an organization is updated, when payload contains logo only, then status is 204 NO CONTENT`() {
        val id = OrganizationId(UUID.randomUUID())
        val image = loadRawImage(testImage)

        every { organizationService.update(any(), any()) } returns Unit

        patchMultipart("/api/organizations/{id}", id)
            .file(MockMultipartFile("logo", "image.png", image.mimeType.toString(), image.data.bytes))
            .perform()
            .andExpect(status().isNoContent)

        verify(exactly = 1) { organizationService.update(any(), any()) }
    }

    @Test
    @TestWithMockCurator
    fun `Given an organization is updated, when payload contains json only, then status is 204 NO CONTENT`() {
        val id = OrganizationId(UUID.randomUUID())
        val body = mapOf(
            "name" to "Organization",
            "url" to "https://example.com",
            "type" to OrganizationType.GENERAL
        )

        every { organizationService.update(any(), any()) } returns Unit

        patchMultipart("/api/organizations/{id}", id)
            .json("properties", body)
            .perform()
            .andExpect(status().isNoContent)

        verify(exactly = 1) { organizationService.update(any(), any()) }
    }

    @Test
    @TestWithMockCurator
    fun `Given an organization is updated, when payload is empty, then status is 204 NO CONTENT`() {
        val id = OrganizationId(UUID.randomUUID())

        every { organizationService.update(any(), any()) } returns Unit

        patchMultipart("/api/organizations/{id}", id)
            .perform()
            .andExpect(status().isNoContent)

        verify(exactly = 1) { organizationService.update(any(), any()) }
    }
}
