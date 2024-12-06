package org.orkg.licenses.domain

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class GitHubLicenseInformationProviderUnitTest {
    private val httpClient: HttpClient = mockk()
    private val provider: GitHubLicenseInformationProvider = GitHubLicenseInformationProvider(ObjectMapper(), httpClient)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(httpClient)
    }

    @Test
    fun `provider ID is github`() {
        assertThat(provider.id).isEqualTo("github")
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "https://github.com/github/docs",
        "https://github.com/github/docs/"
    ])
    fun `can process github repository uris`(uri: String) {
        assertThat(provider.canProcess(URI.create(uri))).isTrue()
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "https://github.com/github/docs/blob/main/README.md",
        "https://github.com/github",
        "https://orkg.org"
    ])
    fun `cannot process other uris`(uri: String) {
        assertThat(provider.canProcess(URI.create(uri))).isFalse()
    }

    @Test
    fun `returns null when io error occurs`() {
        val mockResponse: HttpResponse<String> = mockk()
        val uri = URI.create("https://github.com/github/docs")

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } throws IOException()

        assertThat(provider.determineLicense(uri)).isNull()

        verify(exactly = 1) { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) }
        verify(exactly = 0) { mockResponse.statusCode() }
        verify(exactly = 0) { mockResponse.body() }

        confirmVerified(mockResponse)
    }

    @Test
    fun `returns null when external service reports error`() {
        val mockResponse: HttpResponse<String> = mockk()
        val uri = URI.create("https://github.com/github/docs")

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns mockResponse
        every { mockResponse.statusCode() } returns 404

        assertThat(provider.determineLicense(uri)).isNull()

        verify(exactly = 1) { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) }
        verify(exactly = 1) { mockResponse.statusCode() }
        verify(exactly = 0) { mockResponse.body() }

        confirmVerified(mockResponse)
    }

    @Test
    fun `returns the spdx code when external service succeeds`() {
        val mockResponse: HttpResponse<String> = mockk()
        val uri = URI.create("https://github.com/github/docs")

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns mockResponse
        every { mockResponse.statusCode() } returns 200
        every { mockResponse.body() } returns successfulApiResponse

        val expected = LicenseInformation("github", "CC-BY-4.0")

        assertThat(provider.determineLicense(uri)).isEqualTo(expected)

        verify(exactly = 1) { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) }
        verify(exactly = 1) { mockResponse.statusCode() }
        verify(exactly = 1) { mockResponse.body() }

        confirmVerified(mockResponse)
    }

    @Test
    fun `returns null when external service returns empty response`() {
        val mockResponse: HttpResponse<String> = mockk()
        val uri = URI.create("https://github.com/github/docs")

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns mockResponse
        every { mockResponse.statusCode() } returns 200
        every { mockResponse.body() } returns "{}"

        assertThat(provider.determineLicense(uri)).isNull()

        verify(exactly = 1) { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) }
        verify(exactly = 1) { mockResponse.statusCode() }
        verify(exactly = 1) { mockResponse.body() }

        confirmVerified(mockResponse)
    }
}

const val successfulApiResponse = """{
  "name": "LICENSE",
  "path": "LICENSE",
  "sha": "9238c8f9388066fe7cb3b308de35104bb3c9596b",
  "size": 18620,
  "url": "https://api.github.com/repos/github/docs/contents/LICENSE?ref=main",
  "html_url": "https://github.com/github/docs/blob/main/LICENSE",
  "git_url": "https://api.github.com/repos/github/docs/git/blobs/9238c8f9388066fe7cb3b308de35104bb3c9596b",
  "download_url": "https://raw.githubusercontent.com/github/docs/main/LICENSE",
  "type": "file",
  "content": "<omitted>",
  "encoding": "base64",
  "_links": {
    "self": "https://api.github.com/repos/github/docs/contents/LICENSE?ref=main",
    "git": "https://api.github.com/repos/github/docs/git/blobs/9238c8f9388066fe7cb3b308de35104bb3c9596b",
    "html": "https://github.com/github/docs/blob/main/LICENSE"
  },
  "license": {
    "key": "cc-by-4.0",
    "name": "Creative Commons Attribution 4.0 International",
    "spdx_id": "CC-BY-4.0",
    "url": "https://api.github.com/licenses/cc-by-4.0",
    "node_id": "MDc6TGljZW5zZTI1"
  }
}"""
