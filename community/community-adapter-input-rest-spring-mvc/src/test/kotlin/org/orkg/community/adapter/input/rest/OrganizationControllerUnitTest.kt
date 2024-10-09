package org.orkg.community.adapter.input.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import jakarta.activation.MimeType
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.OrganizationId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.domain.LogoNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.domain.OrganizationType
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.fixtures.createOrganization
import org.orkg.graph.input.ResourceUseCases
import org.orkg.mediastorage.domain.Image
import org.orkg.mediastorage.domain.ImageData
import org.orkg.mediastorage.domain.ImageId
import org.orkg.mediastorage.domain.InvalidImageData
import org.orkg.mediastorage.domain.InvalidMimeType
import org.orkg.mediastorage.input.ImageUseCases
import org.orkg.mediastorage.testing.fixtures.loadImage
import org.orkg.mediastorage.testing.fixtures.loadRawImage
import org.orkg.mediastorage.testing.fixtures.testImage
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.annotations.TestWithMockCurator
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.SecurityTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod.PATCH
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@Import(SecurityTestConfiguration::class)
@ContextConfiguration(classes = [OrganizationController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [OrganizationController::class])
@DisplayName("Given an Organization controller")
internal class OrganizationControllerUnitTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var context: WebApplicationContext

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

    @Autowired
    private lateinit var clock: Clock

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    fun `Given a logo is fetched, when service succeeds, then status is 200 OK and logo is returned`() {
        val id = OrganizationId(UUID.randomUUID())
        val image = loadImage(testImage)

        every { organizationService.findLogo(id) } returns Optional.of(image)

        mockMvc.perform(get("/api/organizations/{id}/logo", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(image.mimeType.toString()))
            .andExpect(content().bytes(image.data.bytes))

        verify(exactly = 1) { organizationService.findLogo(id) }
    }

    @Test
    fun `Given a logo is fetched, when service reports logo not found, then status is 404 NOT FOUND`() {
        val id = OrganizationId(UUID.randomUUID())

        every { organizationService.findLogo(id) } throws LogoNotFound(id)

        mockMvc.perform(get("/api/organizations/{id}/logo", id))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Given a logo is fetched, when service reports organization not found, then status is 404 NOT FOUND`() {
        val id = OrganizationId(UUID.randomUUID())

        every { organizationService.findLogo(id) } throws OrganizationNotFound(id)

        mockMvc.perform(get("/api/organizations/{id}/logo", id))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Given an organization is fetched, when service succeeds, then status 200 OK and the organization is returned with the encoded logo`() {
        val id = OrganizationId(UUID.randomUUID())
        val logoId = ImageId(UUID.randomUUID())
        val organization = createOrganization().copy(
            id = id,
            logoId = logoId
        )
        val contributor = ContributorId(UUID.randomUUID())
        val image = Image(
            id = logoId,
            data = ImageData("irrelevant".toByteArray()),
            mimeType = MimeType("image/png"),
            createdBy = contributor,
            createdAt = OffsetDateTime.now(clock)
        )

        every { organizationService.findById(id) } returns Optional.of(organization)
        every { organizationService.findLogo(id) } returns Optional.of(image)

        mockMvc.perform(get("/api/organizations/{id}", id))
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

        mockMvc
            .perform(
                multipart(PATCH, "/api/organizations/{id}", id)
                    .json("properties", body)
                    .characterEncoding(Charsets.UTF_8.name())
            )
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

        mockMvc
            .perform(
                multipart(PATCH, "/api/organizations/{id}", id)
                    .json("properties", body)
                    .characterEncoding(Charsets.UTF_8.name())
            )
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

        mockMvc
            .perform(
                multipart(PATCH, "/api/organizations/{id}", id)
                    .json("properties", body)
                    .characterEncoding(Charsets.UTF_8.name())
            )
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

        mockMvc
            .perform(
                multipart(PATCH, "/api/organizations/{id}", id)
                    .json("properties", body)
                    .characterEncoding(Charsets.UTF_8.name())
            )
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

        mockMvc
            .perform(
                multipart(PATCH, "/api/organizations/{id}", id)
                    .json("properties", body)
                    .characterEncoding(Charsets.UTF_8.name())
            )
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

        mockMvc
            .perform(
                multipart(PATCH, "/api/organizations/{id}", id)
                    .json("properties", body)
                    .characterEncoding(Charsets.UTF_8.name())
            )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { organizationService.update(any(), any()) }
    }

    @Test
    @TestWithMockCurator
    fun `Given an organization is updated, when payload contains json and logo, then status is 204 NO CONTENT`() {
        val id = OrganizationId(UUID.randomUUID())
        val image = loadRawImage(testImage)
        val body = mapOf(
            "name" to "Organization",
            "url" to "https://example.com",
            "type" to OrganizationType.GENERAL
        )

        every { organizationService.update(any(), any()) } returns Unit

        mockMvc
            .perform(
                multipart(PATCH, "/api/organizations/{id}", id)
                    .json("properties", body)
                    .file(MockMultipartFile("logo", "image.png", image.mimeType.toString(), image.data.bytes))
                    .characterEncoding(Charsets.UTF_8.name())
            )
            .andExpect(status().isNoContent)

        verify(exactly = 1) { organizationService.update(any(), any()) }
    }

    @Test
    @TestWithMockCurator
    fun `Given an organization is updated, when payload contains logo only, then status is 204 NO CONTENT`() {
        val id = OrganizationId(UUID.randomUUID())
        val image = loadRawImage(testImage)

        every { organizationService.update(any(), any()) } returns Unit

        mockMvc
            .perform(
                multipart(PATCH, "/api/organizations/{id}", id)
                    .file(MockMultipartFile("logo", "image.png", image.mimeType.toString(), image.data.bytes))
                    .characterEncoding(Charsets.UTF_8.name())
            )
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

        mockMvc
            .perform(
                multipart(PATCH, "/api/organizations/{id}", id)
                    .json("properties", body)
                    .characterEncoding(Charsets.UTF_8.name())
            )
            .andExpect(status().isNoContent)

        verify(exactly = 1) { organizationService.update(any(), any()) }
    }

    @Test
    @TestWithMockCurator
    fun `Given an organization is updated, when payload is empty, then status is 204 NO CONTENT`() {
        val id = OrganizationId(UUID.randomUUID())

        every { organizationService.update(any(), any()) } returns Unit

        mockMvc
            .perform(
                multipart(PATCH, "/api/organizations/{id}", id)
                    .characterEncoding(Charsets.UTF_8.name())
            )
            .andExpect(status().isNoContent)

        verify(exactly = 1) { organizationService.update(any(), any()) }
    }

    private fun MockMultipartHttpServletRequestBuilder.json(
        name: String,
        data: Map<String, Any>
    ): MockMultipartHttpServletRequestBuilder = file(
        MockMultipartFile(
            name,
            null,
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsString(data).toByteArray()
        )
    )
}
