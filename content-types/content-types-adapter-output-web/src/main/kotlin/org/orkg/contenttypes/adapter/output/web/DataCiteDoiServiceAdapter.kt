package org.orkg.contenttypes.adapter.output.web

import org.orkg.common.DOI
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.configuration.DataCiteConfiguration
import org.orkg.contenttypes.output.DoiService
import org.orkg.integration.datacite.json.DataCiteJson
import org.orkg.integration.datacite.json.DataCiteJson.Attributes
import org.orkg.integration.datacite.json.DataCiteJson.Creator
import org.orkg.integration.datacite.json.DataCiteJson.Description
import org.orkg.integration.datacite.json.DataCiteJson.NameIdentifier
import org.orkg.integration.datacite.json.DataCiteJson.RelatedIdentifier
import org.orkg.integration.datacite.json.DataCiteJson.Rights
import org.orkg.integration.datacite.json.DataCiteJson.Subject
import org.orkg.integration.datacite.json.DataCiteJson.Title
import org.orkg.integration.datacite.json.DataCiteJson.Type
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpHeaders.USER_AGENT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Clock
import java.time.OffsetDateTime
import java.util.Optional

private const val CITATION_STYLE_LANGUAGE_JSON = "application/vnd.citationstyles.csl+json"

@Component
class DataCiteDoiServiceAdapter(
    private val dataciteConfiguration: DataCiteConfiguration,
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient,
    private val bodyPublisherFactory: (String) -> HttpRequest.BodyPublisher = HttpRequest.BodyPublishers::ofString,
    @param:Value("\${orkg.http.user-agent}")
    private val userAgent: String,
    private val clock: Clock = Clock.systemDefaultZone(),
) : DoiService {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun findMetadataByDoi(doi: DOI): Optional<JsonNode> {
        val request = HttpRequest.newBuilder(URI.create(doi.uri))
            .header(ACCEPT, CITATION_STYLE_LANGUAGE_JSON)
            .header(USER_AGENT, userAgent)
            .GET()
            .build()
        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())
            val contentType = response.headers().firstValue(CONTENT_TYPE).orElse(null)
            if (response.statusCode() == HttpStatus.OK.value() && contentType == CITATION_STYLE_LANGUAGE_JSON) {
                return Optional.of(response.body().use(objectMapper::readTree))
            }
        } catch (e: IOException) {
            logger.error("DOI service threw an exception", e)
        } catch (e: InterruptedException) {
            logger.error("DOI service threw an exception", e)
        }
        return Optional.empty()
    }

    override fun register(command: DoiService.RegisterCommand): DOI {
        val body = command.toDataCiteRequest(dataciteConfiguration.doiPrefix!!, dataciteConfiguration.publish!!, clock)
        val request = HttpRequest.newBuilder(URI.create(dataciteConfiguration.url!!))
            .header(CONTENT_TYPE, "application/vnd.api+json; utf-8")
            .header(AUTHORIZATION, "Basic ${dataciteConfiguration.encodedCredentials}")
            .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .header(USER_AGENT, userAgent)
            .POST(bodyPublisherFactory(objectMapper.writeValueAsString(body)))
            .build()
        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != HttpStatus.CREATED.value()) {
                val responseMessage: String = try {
                    objectMapper.readTree(response.body())
                        .path("errors")
                        .firstOrNull()?.path("title")?.asString()
                        ?: "Unknown error"
                } catch (_: Exception) {
                    "Unknown error"
                }
                throw ServiceUnavailable.create("DOI", response.statusCode(), responseMessage)
            }
            return DOI.of("${dataciteConfiguration.doiPrefix}/${command.suffix}")
        } catch (e: IOException) {
            throw ServiceUnavailable.create("DOI", e)
        } catch (e: InterruptedException) {
            throw ServiceUnavailable.create("DOI", e)
        }
    }

    private fun DoiService.RegisterCommand.toDataCiteRequest(prefix: String, action: String, clock: Clock): DataCiteJson =
        DataCiteJson(
            attributes = Attributes(
                doi = "$prefix/$suffix",
                event = action,
                creators = creators.map { author: Author ->
                    Creator(
                        name = author.name,
                        nameIdentifiers = author.identifiers?.get("orcid")
                            .orEmpty()
                            .map(NameIdentifier::fromORCID)
                    )
                },
                titles = listOf(Title(title)),
                publicationYear = OffsetDateTime.now(clock).year,
                subjects = listOf(Subject(subject)),
                types = Type(resourceType, resourceTypeGeneral),
                relatedIdentifiers = relatedIdentifiers.map { RelatedIdentifier.fromDOI(it) },
                rightsList = listOf(Rights.CC_BY_SA_4_0),
                descriptions = listOf(Description(description)),
                url = url
            )
        )
}
