package org.orkg.graph.adapter.output.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.orkg.graph.domain.ExternalThing
import org.orkg.graph.output.ExternalClassService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.util.regex.Pattern
import org.orkg.common.send

@Component
class OLSServiceAdapter(
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient,
    @Value("\${orkg.external-services.ols.host}")
    private val host: String = "https://service.tib.eu/ts4tib/api"
) : ExternalClassService {
    private val pattern = Pattern.compile("[a-zA-Z0-9_]+")

    override fun findClassByShortForm(ontologyId: String, shortForm: String): ExternalThing? {
        if (!supportsOntology(ontologyId)) return null
        return fetch(
            UriComponentsBuilder.fromHttpUrl(host)
                .path("/ontologies/$ontologyId/terms") // in ols, classes are called terms
                .queryParam("short_form", shortForm)
                .build()
                .toUri()
        )
    }

    override fun findClassByURI(ontologyId: String, uri: URI): ExternalThing? {
        if (!supportsOntology(ontologyId)) return null
        return fetch(
            UriComponentsBuilder.fromHttpUrl(host)
                .path("/ontologies/$ontologyId/terms") // in ols, classes are called terms
                .queryParam("iri", uri.toString(), Charsets.UTF_8)
                .build()
                .toUri()
        )
    }

    private fun fetch(uri: URI): ExternalThing? {
        val request = HttpRequest.newBuilder()
            .uri(uri)
            .header("Accept", MediaType.APPLICATION_JSON_VALUE)
            .GET()
            .build()
        return httpClient.send(request, "OntologyLookupService") { response ->
            val tree = objectMapper.readTree(response)
            val item = tree.path("_embedded").path("terms").toList().singleOrNull()
                ?: return@send null
            ExternalThing(
                uri = URI.create(item.path("iri").asText()),
                label = item.path("label").asText(),
                description = item.path("description").toList()
                    .firstOrNull()?.asText()
                    .takeIf { !it.isNullOrBlank() },
            )
        }
    }

    override fun supportsOntology(ontologyId: String): Boolean =
        pattern.matcher(ontologyId).matches()

    override fun supportsMultipleOntologies(): Boolean = true
}
