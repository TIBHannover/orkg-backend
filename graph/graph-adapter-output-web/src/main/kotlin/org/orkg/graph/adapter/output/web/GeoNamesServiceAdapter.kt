package org.orkg.graph.adapter.output.web

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.send
import org.orkg.graph.domain.ExternalThing
import org.orkg.graph.output.ExternalResourceService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.USER_AGENT
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import tools.jackson.databind.ObjectMapper
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.util.regex.Pattern

@Component
class GeoNamesServiceAdapter(
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient,
    @param:Value("\${orkg.http.user-agent}")
    private val userAgent: String,
    @Value("\${orkg.external-services.geonames.host}")
    private val host: String,
    @Value("\${orkg.external-services.geonames.username}")
    private val username: String,
) : ExternalResourceService {
    private val pattern = Pattern.compile("""https?://(?:sws\.|www\.|)geonames\.org/([0-9]+)(?:/\S*\.html|/)?""")

    override fun findResourceByShortForm(ontologyId: String, shortForm: String): ExternalThing? {
        // geonames only supports 32-bit integer ids, anything above will *not* throw a status 404
        if (!supportsOntology(ontologyId) || shortForm.toIntOrNull() == null) return null
        val apiUri = UriComponentsBuilder.fromUriString(host)
            .path("/get")
            .queryParam("geonameId", shortForm)
            .queryParam("username", username)
            .build()
            .toUri()
        val request = HttpRequest.newBuilder(apiUri)
            .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .header(USER_AGENT, userAgent)
            .GET()
            .build()
        return httpClient.send(request, "GeoNames") { response ->
            ExternalThing(
                uri = ParsedIRI.create("https://sws.geonames.org/$shortForm"),
                label = objectMapper.readTree(response).path("name").asString(),
                description = null
            )
        }
    }

    override fun findResourceByURI(ontologyId: String, uri: ParsedIRI): ExternalThing? {
        val id = pattern.matchSingleGroupOrNull(uri.toString()) ?: return null
        return findResourceByShortForm(ontologyId, id)
    }

    override fun supportsOntology(ontologyId: String): Boolean =
        ontologyId == "geonames"

    override fun supportsMultipleOntologies(): Boolean = false
}
