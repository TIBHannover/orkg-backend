package org.orkg.licenses.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import java.net.URI
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.licenses.domain.LicenseInformation
import org.orkg.licenses.domain.LicenseNotFound
import org.orkg.licenses.domain.UnsupportedURI
import org.orkg.licenses.input.RetrieveLicenseInformationUseCase
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [LicenseInformationController::class, ExceptionHandler::class, FixedClockConfig::class])
@WebMvcTest(controllers = [LicenseInformationController::class])
internal class LicenseControllerUnitTest : RestDocsTest("licenses") {

    @MockkBean
    private lateinit var licenseService: RetrieveLicenseInformationUseCase

    @Test
    @DisplayName("correctly serializes license information")
    fun getLicense() {
        val license = LicenseInformation("github", "CC-BY-4.0")
        val uri = "https://github.com/github/docs"

        every { licenseService.determineLicense(URI.create(uri)) } returns license

        documentedGetRequestTo("/api/licenses?uri={uri}", uri)
            .accept(LICENSE_JSON_V1)
            .contentType(LICENSE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.license", `is`("CC-BY-4.0")))
            .andDo(
                documentationHandler.document(
                    queryParameters(parameterWithName("uri").description("The uri of the repository to retrieve the license from.")),
                    responseFields(
                        fieldWithPath("license").description("The spdx identifier of the license."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun `Given a repository uri, when license could not be found, then status is 404 NOT FOUND`() {
        val uri = URI.create("https://github.com/github/docs")
        val exception = LicenseNotFound(uri)

        every { licenseService.determineLicense(uri) } throws exception

        get("/api/licenses?uri={uri}", uri)
            .accept(LICENSE_JSON_V1)
            .contentType(LICENSE_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/licenses"))

        verify(exactly = 1) { licenseService.determineLicense(uri) }
    }

    @Test
    fun `Given a repository uri, when uri is not supported, then status is 400 BAD REQUEST`() {
        val uri = URI.create("https://github.com/github/docs")
        val exception = UnsupportedURI(uri)

        every { licenseService.determineLicense(uri) } throws exception

        get("/api/licenses?uri={uri}", uri)
            .accept(LICENSE_JSON_V1)
            .contentType(LICENSE_JSON_V1)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/licenses"))

        verify(exactly = 1) { licenseService.determineLicense(uri) }
    }
}
