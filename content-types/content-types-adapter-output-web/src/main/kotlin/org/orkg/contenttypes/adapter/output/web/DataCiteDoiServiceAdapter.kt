package org.orkg.contenttypes.adapter.output.web

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Clock
import java.time.OffsetDateTime

@Component
class DataCiteDoiServiceAdapter(
    private val dataciteConfiguration: DataCiteConfiguration,
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient,
    private val bodyPublisherFactory: (String) -> HttpRequest.BodyPublisher = HttpRequest.BodyPublishers::ofString,
    private val clock: Clock = Clock.systemDefaultZone(),
) : DoiService {
    override fun register(command: DoiService.RegisterCommand): DOI {
        val body = command.toDataCiteRequest(dataciteConfiguration.doiPrefix!!, dataciteConfiguration.publish!!, clock)
        val request = HttpRequest.newBuilder()
            .uri(URI.create(dataciteConfiguration.url!!))
            .header("Content-Type", "application/vnd.api+json; utf-8")
            .header("Authorization", "Basic ${dataciteConfiguration.encodedCredentials}")
            .header("Accept", MediaType.APPLICATION_JSON_VALUE)
            .POST(bodyPublisherFactory(objectMapper.writeValueAsString(body)))
            .build()
        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != HttpStatus.CREATED.value()) {
                val responseMessage: String = try {
                    objectMapper.readTree(response.body())
                        .path("errors")
                        .firstOrNull()?.path("title")?.asText()
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
