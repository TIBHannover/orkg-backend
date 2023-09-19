package eu.tib.orkg.prototype.contenttypes.adapter.output.datacite

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.ObjectMapper
import eu.tib.orkg.prototype.contenttypes.adapter.output.datacite.DataCiteDoiServiceAdapter.RegisterDoiRequest.Attributes
import eu.tib.orkg.prototype.contenttypes.adapter.output.datacite.DataCiteDoiServiceAdapter.RegisterDoiRequest.Creator
import eu.tib.orkg.prototype.contenttypes.adapter.output.datacite.DataCiteDoiServiceAdapter.RegisterDoiRequest.Description
import eu.tib.orkg.prototype.contenttypes.adapter.output.datacite.DataCiteDoiServiceAdapter.RegisterDoiRequest.NameIdentifier
import eu.tib.orkg.prototype.contenttypes.adapter.output.datacite.DataCiteDoiServiceAdapter.RegisterDoiRequest.RelatedIdentifier
import eu.tib.orkg.prototype.contenttypes.adapter.output.datacite.DataCiteDoiServiceAdapter.RegisterDoiRequest.Rights
import eu.tib.orkg.prototype.contenttypes.adapter.output.datacite.DataCiteDoiServiceAdapter.RegisterDoiRequest.Subject
import eu.tib.orkg.prototype.contenttypes.adapter.output.datacite.DataCiteDoiServiceAdapter.RegisterDoiRequest.Title
import eu.tib.orkg.prototype.contenttypes.adapter.output.datacite.DataCiteDoiServiceAdapter.RegisterDoiRequest.Type
import eu.tib.orkg.prototype.contenttypes.domain.model.DOI
import eu.tib.orkg.prototype.contenttypes.spi.DoiService
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
            return "${dataciteConfiguration.doiPrefix}/${command.suffix}"
        } catch (e: IOException) {
            throw DOIServiceUnavailable(e)
        } catch (e: InterruptedException) {
            throw DOIServiceUnavailable(e)
        }
    }

    @JsonTypeName(value = "data")
    @JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
    data class RegisterDoiRequest(
        val attributes: Attributes,
        val type: String = "dois"
    ) {
        data class Attributes(
            val doi: String,
            val event: String,
            val creators: List<Creator>,
            val titles: List<Title>,
            val publicationYear: Int,
            val subjects: List<Subject>,
            val types: Type,
            val relatedIdentifiers: List<RelatedIdentifier>,
            val rightsList: List<Rights>,
            val descriptions: List<Description>,
            val url: URI,
            val language: String = "en",
            val publisher: String = "Open Research Knowledge Graph"
        )

        data class Creator(
            val name: String,
            val nameIdentifiers: List<NameIdentifier>,
            val nameType: String = "Personal"
        )

        data class NameIdentifier(
            val schemeUri: URI,
            val nameIdentifier: String,
            val nameIdentifierScheme: String
        ) {
            companion object {
                fun fromORCID(orcid: String): NameIdentifier =
                    NameIdentifier(
                        schemeUri = URI.create("https://orcid.org"),
                        nameIdentifier = "https://orcid.org/$orcid",
                        nameIdentifierScheme = "ORCID"
                    )
            }
        }

        data class Title(
            val title: String,
            val lang: String = "en"
        )

        data class Subject(
            val subject: String,
            val lang: String = "en"
        )

        data class Type(
            val resourceType: String,
            val resourceTypeGeneral: String
        )

        data class RelatedIdentifier(
            val relatedIdentifier: String,
            val relatedIdentifierType: String,
            val relationType: String = "References"
        ) {
            companion object {
                fun fromDOI(doi: String): RelatedIdentifier =
                    RelatedIdentifier(
                        relatedIdentifier = doi,
                        relatedIdentifierType = "DOI"
                    )
            }
        }

        data class Rights(
            val rights: String,
            val rightsUri: URI
        ) {
            companion object {
                val CC_BY_SA_4_0 = Rights(
                    rights = "Creative Commons Attribution-ShareAlike 4.0 International License.",
                    rightsUri = URI.create("https://creativecommons.org/licenses/by-sa/4.0/")
                )
            }
        }

        data class Description(
            val description: String,
            val descriptionType: String = "Abstract"
        )
    }

    private fun DoiService.RegisterCommand.toDataCiteRequest(prefix: String, action: String, clock: Clock): RegisterDoiRequest =
        RegisterDoiRequest(
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
