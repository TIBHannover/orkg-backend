package org.orkg.graph.adapter.output.web

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.util.regex.Pattern
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.send
import org.orkg.graph.domain.ExternalThing
import org.orkg.graph.output.ExternalClassService
import org.orkg.graph.output.ExternalResourceService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OLSServiceAdapter(
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient,
    @Value("\${orkg.external-services.ols.host}")
    private val host: String
) : ExternalResourceService, ExternalClassService {
    private val pattern = Pattern.compile("[a-zA-Z0-9_]+")

    override fun findClassByShortForm(ontologyId: String, shortForm: String): ExternalThing? {
        if (!supportsOntology(ontologyId)) return null
        return fetch(
            type = "terms", // in ols, classes are called terms
            uri = UriComponentsBuilder.fromHttpUrl(host)
                .path("/ontologies/$ontologyId/terms")
                .queryParam("short_form", shortForm)
                .build()
                .toUri()
        )
    }

    override fun findClassByURI(ontologyId: String, uri: ParsedIRI): ExternalThing? {
        if (!supportsOntology(ontologyId)) return null
        return fetch(
            type = "terms", // in ols, classes are called terms
            uri = UriComponentsBuilder.fromHttpUrl(host)
                .path("/ontologies/$ontologyId/terms")
                .queryParam("iri", uri.toString())
                .build()
                .toUri()
        )
    }

    override fun findResourceByShortForm(ontologyId: String, shortForm: String): ExternalThing? {
        if (!supportsOntology(ontologyId)) return null
        return fetch(
            type = "individuals", // in ols, resource are called individuals
            uri = UriComponentsBuilder.fromHttpUrl(host)
                .path("/ontologies/$ontologyId/individuals")
                .queryParam("short_form", shortForm)
                .build()
                .toUri()
        )
    }

    override fun findResourceByURI(ontologyId: String, uri: ParsedIRI): ExternalThing? {
        if (!supportsOntology(ontologyId)) return null
        return fetch(
            type = "individuals", // in ols, resource are called individuals
            uri = UriComponentsBuilder.fromHttpUrl(host)
                .path("/ontologies/$ontologyId/individuals")
                .queryParam("iri", uri.toString())
                .build()
                .toUri()
        )
    }

    private fun fetch(type: String, uri: URI): ExternalThing? {
        val request = HttpRequest.newBuilder()
            .uri(uri)
            .header("Accept", MediaType.APPLICATION_JSON_VALUE)
            .GET()
            .build()
        return httpClient.send(request, "OntologyLookupService") { response ->
            val tree = objectMapper.readTree(response)
            val item = tree.path("_embedded").path(type).toList().singleOrNull()
                ?: return@send null
            ExternalThing(
                uri = ParsedIRI(item.path("iri").asText()),
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
