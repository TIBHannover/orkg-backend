package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.AuthorizationServerUnitTestWorkaround
import eu.tib.orkg.prototype.community.api.ObservatoryUseCases
import eu.tib.orkg.prototype.community.api.OrganizationUseCases
import eu.tib.orkg.prototype.community.application.EncodedImage
import eu.tib.orkg.prototype.community.application.InvalidImageEncoding
import eu.tib.orkg.prototype.community.application.LogoNotFound
import eu.tib.orkg.prototype.community.application.OrganizationController
import eu.tib.orkg.prototype.community.application.OrganizationNotFound
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.createOrganization
import eu.tib.orkg.prototype.encodedTestImage
import eu.tib.orkg.prototype.files.api.ImageUseCases
import eu.tib.orkg.prototype.files.application.InvalidImageData
import eu.tib.orkg.prototype.files.application.InvalidMimeType
import eu.tib.orkg.prototype.files.domain.model.Image
import eu.tib.orkg.prototype.files.domain.model.ImageData
import eu.tib.orkg.prototype.files.domain.model.ImageId
import eu.tib.orkg.prototype.loadEncodedImage
import eu.tib.orkg.prototype.loadImage
import eu.tib.orkg.prototype.loadRawImage
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.testImage
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.*
import javax.activation.MimeType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(controllers = [OrganizationController::class])
@AuthorizationServerUnitTestWorkaround
@DisplayName("Given an OrganizationController controller")
internal class OrganizationControllerUnitTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var context: WebApplicationContext

    @MockkBean
    private lateinit var organizationService: OrganizationUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userDetailsService: UserDetailsService

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var observatoryService: ObservatoryUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var contributorService: ContributorService

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var imageService: ImageUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var resourceService: ResourceUseCases

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `Given a logo is fetched, when service succeeds, then status is 200 OK and logo is returned`() {
        val id = OrganizationId(UUID.randomUUID())
        val image = loadImage(testImage)

        every { organizationService.findLogo(id) } returns Optional.of(image)

        mockMvc.perform(get("/api/organizations/$id/logo"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(image.mimeType.toString()))
            .andExpect(content().bytes(image.data.bytes))

        verify(exactly = 1) { organizationService.findLogo(id) }
    }

    @Test
    fun `Given a logo is fetched, when service reports logo not found, then status is 404 NOT FOUND`() {
        val id = OrganizationId(UUID.randomUUID())

        every { organizationService.findLogo(id) } throws LogoNotFound(id)

        mockMvc.perform(get("/api/organizations/$id/logo"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Given a logo is fetched, when service reports organization not found, then status is 404 NOT FOUND`() {
        val id = OrganizationId(UUID.randomUUID())

        every { organizationService.findLogo(id) } throws OrganizationNotFound(id)

        mockMvc.perform(get("/api/organizations/$id/logo"))
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
            mimeType = MimeType(),
            createdBy = contributor,
            createdAt = OffsetDateTime.now()
        )

        every { organizationService.findById(id) } returns Optional.of(organization)
        every { organizationService.findLogo(id) } returns Optional.of(image)

        mockMvc.perform(get("/api/organizations/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("$id"))
            .andExpect(jsonPath("$.logo_id").doesNotExist())

        verify(exactly = 1) { organizationService.findById(id) }
    }

    @Nested
    @DisplayName("When converting images")
    inner class Images {
        @ParameterizedTest
        @ValueSource(strings = [
            "invalid",
            "data:image/png;base64,",
            "data:invalid;base64,",
            "data:;base64,",
            "data:;,",
            "data:;,data"
        ])
        fun `given an image is being decoded, when the image encoding is invalid, then an exception is thrown`(string: String) {
            shouldThrow<InvalidImageEncoding> {
                EncodedImage(string).decodeBase64()
            }
        }

        @ParameterizedTest
        @ValueSource(strings = ["data:invalid;base64,irrelevant", "data:invalid/error/;base64,irrelevant"])
        fun `given an image is being decoded, when the mime type is invalid, then an exception is thrown`(string: String) {
            shouldThrow<InvalidMimeType> {
                EncodedImage(string).decodeBase64()
            }
        }

        @Test
        fun `given an image is being decoded, when the data is invalid, then an exception is thrown`() {
            shouldThrow<InvalidImageData> {
                EncodedImage("data:image/png;base64,error").decodeBase64()
            }
        }

        @Test
        fun `given an image is decoded, then the result is correct`() {
            val decoded = EncodedImage(loadEncodedImage(encodedTestImage)).decodeBase64()
            val expected = loadRawImage(testImage)
            decoded.asClue {
                decoded.data shouldBe expected.data
                decoded.mimeType.toString() shouldBe expected.mimeType.toString()
            }
        }
    }

    @Test
    @WithMockUser(username = "f2d66c90-3cbf-4d4f-951f-0fc470f682c4")
    fun `Given an organization is updated, when service reports organization not found, then status is 404 NOT FOUND`() {
        val id = OrganizationId(UUID.randomUUID())
        val body = mapOf(
            "name" to "irrelevant"
        )

        every { organizationService.update(any(), any()) } throws OrganizationNotFound(id)

        mockMvc.perform(performPatch("/api/organizations/$id", body))
            .andExpect(status().isNotFound)

        verify(exactly = 1) { organizationService.update(any(), any()) }
    }

    @Test
    @WithMockUser(username = "f2d66c90-3cbf-4d4f-951f-0fc470f682c4")
    fun `Given an organization is updated, when service reports invalid mime type for the logo, then status is 400 BAD REQUEST`() {
        val id = OrganizationId(UUID.randomUUID())
        val body = mapOf(
            "name" to "irrelevant"
        )

        every { organizationService.update(any(), any()) } throws InvalidMimeType("irrelevant")

        mockMvc.perform(performPatch("/api/organizations/$id", body))
            .andExpect(status().isForbidden)

        verify(exactly = 1) { organizationService.update(any(), any()) }
    }

    @Test
    @WithMockUser(username = "f2d66c90-3cbf-4d4f-951f-0fc470f682c4")
    fun `Given an organization is updated, when service reports invalid image data for the logo, then status is 400 BAD REQUEST`() {
        val id = OrganizationId(UUID.randomUUID())
        val body = mapOf(
            "name" to "irrelevant"
        )

        every { organizationService.update(any(), any()) } throws InvalidImageData()

        mockMvc.perform(performPatch("/api/organizations/$id", body))
            .andExpect(status().isForbidden)

        verify(exactly = 1) { organizationService.update(any(), any()) }
    }

    @Test
    @WithMockUser(username = "f2d66c90-3cbf-4d4f-951f-0fc470f682c4")
    fun `Given an organization is updated, when service succeeds, then status is 204 NO CONTENT`() {
        val id = OrganizationId(UUID.randomUUID())
        val body = mapOf(
            "name" to "irrelevant"
        )

        every { organizationService.update(any(), any()) } returns Unit

        mockMvc.perform(performPatch("/api/organizations/$id", body))
            .andExpect(status().isNoContent)
            .andExpect(content().string(""))

        verify(exactly = 1) { organizationService.update(any(), any()) }
    }

    @Test
    @WithMockUser(username = "f2d66c90-3cbf-4d4f-951f-0fc470f682c4")
    fun `Given an organization is updated, when name is invalid, then status is 400 BAD REQUEST`() {
        val id = OrganizationId(UUID.randomUUID())
        val body = mapOf(
            "name" to ""
        )

        mockMvc.perform(performPatch("/api/organizations/$id", body))
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { organizationService.update(any(), any()) }
    }

    @Test
    @WithMockUser(username = "f2d66c90-3cbf-4d4f-951f-0fc470f682c4")
    fun `Given an organization is updated, when url is invalid, then status is 400 BAD REQUEST`() {
        val id = OrganizationId(UUID.randomUUID())
        val body = mapOf(
            "url" to ""
        )

        mockMvc.perform(performPatch("/api/organizations/$id", body))
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { organizationService.update(any(), any()) }
    }

    private fun performPatch(uri: String, body: Map<String, Any>) =
        MockMvcRequestBuilders.patch(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(Charsets.UTF_8.name())
            .content(objectMapper.writeValueAsString(body))
}
