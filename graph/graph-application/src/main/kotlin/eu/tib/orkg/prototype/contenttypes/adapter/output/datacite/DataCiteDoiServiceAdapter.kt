package eu.tib.orkg.prototype.contenttypes.adapter.output.datacite

import com.fasterxml.jackson.databind.ObjectMapper
import eu.tib.orkg.prototype.contenttypes.spi.DoiService
import eu.tib.orkg.prototype.datacite.json.DataCiteJson
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.Attributes
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.Creator
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.Description
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.NameIdentifier
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.RelatedIdentifier
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.Rights
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.Subject
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.Title
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.Type
import eu.tib.orkg.prototype.identifiers.domain.DOI
import eu.tib.orkg.prototype.statements.application.DOIServiceUnavailable
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Clock
import java.time.OffsetDateTime
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

@Component
class DataCiteDoiServiceAdapter(
    private val dataciteConfiguration: DataCiteConfiguration,
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient,
    private val bodyPublisherFactory: (String) -> HttpRequest.BodyPublisher = HttpRequest.BodyPublishers::ofString,
    private val clock: Clock = Clock.systemDefaultZone()
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
                throw DOIServiceUnavailable(response.statusCode(), responseMessage)
            }
            return DOI.of("${dataciteConfiguration.doiPrefix}/${command.suffix}")
        } catch (e: IOException) {
            throw DOIServiceUnavailable(e)
        } catch (e: InterruptedException) {
            throw DOIServiceUnavailable(e)
        }
    }

    private fun DoiService.RegisterCommand.toDataCiteRequest(prefix: String, action: String, clock: Clock): DataCiteJson =
        DataCiteJson(
            attributes = Attributes(
                doi = "$prefix/$suffix",
                event = action,
                creators = creators.map { author ->
                    Creator(
                        name = author.name,
                        nameIdentifiers = author.identifiers?.get("orcid")
                            ?.let { listOf(NameIdentifier.fromORCID(it)) }
                            .orEmpty()
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
