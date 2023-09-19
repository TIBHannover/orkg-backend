package eu.tib.orkg.prototype.contenttypes.adapter.output.datacite

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import eu.tib.orkg.prototype.dummyRegisterDoiCommand
import eu.tib.orkg.prototype.statements.application.DOIServiceUnavailable
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpResponse
import java.nio.ByteBuffer
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.Flow
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class DataCiteDoiServiceAdapterUnitTest {
    private val dataciteConfiguration: DataCiteConfiguration = mockk()
    private val httpClient: HttpClient = mockk()
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val fixedTime = OffsetDateTime.of(2023, 9, 8, 13, 9, 34, 12345, ZoneOffset.ofHours(1))
    private val staticClock = Clock.fixed(Instant.from(fixedTime), ZoneId.systemDefault())
    private val adapter = DataCiteDoiServiceAdapter(dataciteConfiguration, objectMapper, httpClient, ::TestBodyPublisher, staticClock)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(dataciteConfiguration, httpClient)
    }

    @Test
    fun `Creating a doi, returns success`() {
        val command = dummyRegisterDoiCommand()
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()
        val dataCiteUri = "https://api.test.datacite.org/dois"
        val dataCitePrefix = "10.7484"
        val encodedCredentials = Base64.getEncoder().encodeToString("username:password".toByteArray())
        // Deserialize and serialize json to remove formatting
        val json = objectMapper.writeValueAsString(objectMapper.readTree(dataCiteRequestJson))

        every { dataciteConfiguration.publish } returns "draft"
        every { dataciteConfiguration.url } returns dataCiteUri
        every { dataciteConfiguration.doiPrefix } returns dataCitePrefix
        every { dataciteConfiguration.encodedCredentials } returns encodedCredentials
        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 201

        val result = adapter.register(command)
        result shouldBe "$dataCitePrefix/${command.suffix}"

        verify(exactly = 1) { dataciteConfiguration.publish }
        verify(exactly = 1) { dataciteConfiguration.url }
        verify(exactly = 2) { dataciteConfiguration.doiPrefix }
        verify(exactly = 1) { dataciteConfiguration.encodedCredentials }
        verify(exactly = 1) {
            httpClient.send(
                withArg<HttpRequest> { request ->
                    request.method() shouldBe "POST"
                    request.headers().map() shouldContainAll mapOf(
                        "Content-Type" to listOf("application/vnd.api+json; utf-8"),
                        "Authorization" to listOf("Basic $encodedCredentials"),
                        "Accept" to listOf(MediaType.APPLICATION_JSON_VALUE)
                    )
                    request.uri() shouldBe URI.create(dataCiteUri)
                    request.bodyPublisher().asClue { body ->
                        body.isPresent shouldBe true
                        body.get().shouldBeInstanceOf<TestBodyPublisher>().asClue {
                            it.content shouldBe json
                        }
                    }
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify(exactly = 1) { response.statusCode() }

        confirmVerified(response)
    }

    @Test
    fun `Creating a doi, when data cite returns error, then an exception is thrown`() {
        val command = dummyRegisterDoiCommand()
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()
        val dataCiteUri = "https://api.test.datacite.org/dois"
        val dataCitePrefix = "10.7484"
        val encodedCredentials = Base64.getEncoder().encodeToString("username:password".toByteArray())
        val expected = DOIServiceUnavailable(500, "Internal error")

        every { dataciteConfiguration.publish } returns "draft"
        every { dataciteConfiguration.url } returns dataCiteUri
        every { dataciteConfiguration.doiPrefix } returns dataCitePrefix
        every { dataciteConfiguration.encodedCredentials } returns encodedCredentials
        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 500
        every { response.body() } returns dataCiteErrorResponseJson

        shouldThrow<DOIServiceUnavailable> {
            adapter.register(command)
        }.asClue {
            it.message shouldBe expected.message
        }

        verify(exactly = 1) { dataciteConfiguration.publish }
        verify(exactly = 1) { dataciteConfiguration.url }
        verify(exactly = 1) { dataciteConfiguration.doiPrefix }
        verify(exactly = 1) { dataciteConfiguration.encodedCredentials }
        verify(exactly = 1) { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) }
        verify(exactly = 2) { response.statusCode() }
        verify(exactly = 1) { response.body() }

        confirmVerified(response)
    }

    // Testing class that enables easily accessible body contents
    class TestBodyPublisher(val content: String) : BodyPublisher {
        private val delegate: BodyPublisher = HttpRequest.BodyPublishers.ofString(content)

        override fun subscribe(subscriber: Flow.Subscriber<in ByteBuffer>?) = delegate.subscribe(subscriber)

        override fun contentLength(): Long = delegate.contentLength()
    }
}

private const val dataCiteRequestJson = """
{
  "data": {
    "attributes": {
      "doi": "10.7484/182",
      "event": "draft",
      "creators": [
        {
          "name": "Josiah Stinkney Carberry",
          "nameIdentifiers": [
            {
              "schemeUri": "https://orcid.org",
              "nameIdentifier": "https://orcid.org/0000-0002-1825-0097",
              "nameIdentifierScheme": "ORCID"
            }
          ],
          "nameType":"Personal"
        },
        {
          "name": "Author 2",
          "nameIdentifiers": [],
          "nameType": "Personal"
        }
      ],
      "titles": [
        {
          "title": "Paper title",
          "lang": "en"
        }
      ],
      "publicationYear": 2023,
      "subjects": [
        {
          "subject": "Paper subject",
          "lang": "en"
        }
      ],
      "types": {
        "resourceType": "Paper",
        "resourceTypeGeneral": "Dataset"
      },
      "relatedIdentifiers": [
        {
          "relatedIdentifier": "10.48366/r609337",
          "relatedIdentifierType": "DOI",
          "relationType": "References"
        }
      ],
      "rightsList": [
        {
          "rights": "Creative Commons Attribution-ShareAlike 4.0 International License.",
          "rightsUri": "https://creativecommons.org/licenses/by-sa/4.0/"
        }
      ],
      "descriptions": [
        {
          "description": "Description of the paper",
          "descriptionType": "Abstract"
        }
      ],
      "url": "https://example.org",
      "language": "en",
      "publisher": "Open Research Knowledge Graph"
    },
    "type": "dois"
  }
}"""

private const val dataCiteErrorResponseJson = """{
  "errors": [
    {
      "status": "500",
      "title": "Internal error"
    }
  ]
}"""
