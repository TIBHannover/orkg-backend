package org.orkg.dataimport.domain.rdf

import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.testing.fixtures.Assets.responseJson
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.USER_AGENT
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import tools.jackson.databind.ObjectMapper
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.util.UUID

internal class TypeRegistryLabelResolverUnitTest : MockkBaseTest {
    private val httpClient = mockk<HttpClient>()
    private val objectMapper = ObjectMapper()
    private val userAgent = "test user agent"
    private val typeRegistryCordraBaseUrl = "https://typeregistry.pidconsortium.net/cordra"

    private val rdfLabelResolverService = TypeRegistryRdfLabelResolver(httpClient, objectMapper, userAgent, typeRegistryCordraBaseUrl)

    @Test
    fun `Given an rdf file id and a resource, when resolving a label and type registry returns success, it returns the label`() {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<InputStream>>()

        val rdfFileId = UUID.randomUUID()
        val resource = SimpleValueFactory.getInstance().createIRI("https://doi.org/21.T11969/4607bc7c42ac8db29bfc")
        val body = ByteArrayInputStream(responseJson("typeregistry/success").toByteArray())
        val expected = "Cell"

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<InputStream>>()) } returns response
        every { response.statusCode() } returns 200
        every { response.body() } returns body

        rdfLabelResolverService.resolve(rdfFileId, resource) shouldBe expected

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("https://typeregistry.pidconsortium.net/cordra/0.DOIP/Op.Retrieve?targetId=21.T11969/4607bc7c42ac8db29bfc")
                    it.headers().map() shouldContainAll mapOf(
                        ACCEPT to listOf(APPLICATION_JSON_VALUE),
                        USER_AGENT to listOf(userAgent),
                    )
                },
                any<HttpResponse.BodyHandler<InputStream>>(),
            )
        }
        verify(exactly = 1) { response.statusCode() }
        verify(exactly = 1) { response.body() }
    }

    @ParameterizedTest
    @MethodSource("urlsWithFragment")
    fun `Given an rdf file id and a resource with fragment, when resolving a label and type registry returns success, it returns the label`(url: String, expected: String) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<InputStream>>()

        val rdfFileId = UUID.randomUUID()
        val resource = SimpleValueFactory.getInstance().createIRI(url)
        val body = ByteArrayInputStream(responseJson("typeregistry/success").toByteArray())

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<InputStream>>()) } returns response
        every { response.statusCode() } returns 200
        every { response.body() } returns body

        rdfLabelResolverService.resolve(rdfFileId, resource) shouldBe expected

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("https://typeregistry.pidconsortium.net/cordra/0.DOIP/Op.Retrieve?targetId=21.T11969/4607bc7c42ac8db29bfc")
                    it.headers().map() shouldContainAll mapOf(
                        ACCEPT to listOf(APPLICATION_JSON_VALUE),
                        USER_AGENT to listOf(userAgent),
                    )
                },
                any<HttpResponse.BodyHandler<InputStream>>(),
            )
        }
        verify(exactly = 1) { response.statusCode() }
        verify(exactly = 1) { response.body() }
    }

    @Test
    fun `Given an rdf file id and a resource, when resolving a label and type registry returns status 404, it returns null`() {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<InputStream>>()

        val rdfFileId = UUID.randomUUID()
        val resource = SimpleValueFactory.getInstance().createIRI("https://doi.org/21.T11969/4607bc7c42ac8db29bfc")
        val expected = null

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<InputStream>>()) } returns response
        every { response.statusCode() } returns 404

        rdfLabelResolverService.resolve(rdfFileId, resource) shouldBe expected

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("https://typeregistry.pidconsortium.net/cordra/0.DOIP/Op.Retrieve?targetId=21.T11969/4607bc7c42ac8db29bfc")
                    it.headers().map() shouldContainAll mapOf(
                        ACCEPT to listOf(APPLICATION_JSON_VALUE),
                        USER_AGENT to listOf(userAgent),
                    )
                },
                any<HttpResponse.BodyHandler<InputStream>>(),
            )
        }
        verify(exactly = 1) { response.statusCode() }
    }

    @Test
    fun `Given an rdf file id and a resource, when resolving a label and an unexpected error occurs, it returns null`() {
        val rdfFileId = UUID.randomUUID()
        val resource = SimpleValueFactory.getInstance().createIRI("https://doi.org/21.T11969/4607bc7c42ac8db29bfc")
        val expected = null

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<InputStream>>()) } throws IOException()

        rdfLabelResolverService.resolve(rdfFileId, resource) shouldBe expected

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("https://typeregistry.pidconsortium.net/cordra/0.DOIP/Op.Retrieve?targetId=21.T11969/4607bc7c42ac8db29bfc")
                    it.headers().map() shouldContainAll mapOf(
                        ACCEPT to listOf(APPLICATION_JSON_VALUE),
                        USER_AGENT to listOf(userAgent),
                    )
                },
                any<HttpResponse.BodyHandler<InputStream>>(),
            )
        }
    }

    companion object {
        @JvmStatic
        fun urlsWithFragment() = listOf(
            Arguments.of("https://doi.org/21.T11969/4607bc7c42ac8db29bfc#column", "Column"), // from "Title" field
            Arguments.of("https://doi.org/21.T11969/4607bc7c42ac8db29bfc#value", "value"), // from "Name" field, because "Title" is absent
        )
    }
}
