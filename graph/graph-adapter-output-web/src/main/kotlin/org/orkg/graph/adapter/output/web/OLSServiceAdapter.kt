package org.orkg.graph.adapter.output.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.send
import org.orkg.graph.domain.ExternalThing
import org.orkg.graph.output.ExternalClassService
import org.orkg.graph.output.ExternalPredicateService
import org.orkg.graph.output.ExternalResourceService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.util.regex.Pattern

@Component
class OLSServiceAdapter(
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient,
    @Value("\${orkg.external-services.ols.host}")
    private val host: String,
) : ExternalResourceService,
    ExternalClassService,
    ExternalPredicateService {
    private val pattern = Pattern.compile("[a-zA-Z0-9_]+")

    override fun findClassByShortForm(ontologyId: String, shortForm: String): ExternalThing? =
        fetchByShortForm(ontologyId, Type.CLASS, shortForm)

    override fun findClassByURI(ontologyId: String, uri: ParsedIRI): ExternalThing? =
        fetchByURI(ontologyId, Type.CLASS, uri)

    override fun findResourceByShortForm(ontologyId: String, shortForm: String): ExternalThing? =
        fetchByShortForm(ontologyId, Type.RESOURCE, shortForm)

    override fun findResourceByURI(ontologyId: String, uri: ParsedIRI): ExternalThing? =
        fetchByURI(ontologyId, Type.RESOURCE, uri)

    override fun findPredicateByShortForm(ontologyId: String, shortForm: String): ExternalThing? =
        fetchByShortForm(ontologyId, Type.PREDICATE, shortForm)

    override fun findPredicateByURI(ontologyId: String, uri: ParsedIRI): ExternalThing? =
        fetchByURI(ontologyId, Type.PREDICATE, uri)

    private fun fetchByURI(ontologyId: String, type: Type, uri: ParsedIRI): ExternalThing? {
        if (!supportsOntology(ontologyId)) return null
        return fetch(
            UriComponentsBuilder.fromUriString(host)
                .path("/ontologies/$ontologyId/${type.endpointPath}")
                .queryParam("iri", uri.toString())
                .queryParam("exactMatch", true)
                .queryParam("lang", "en")
                .queryParam("includeObsoleteEntities", false)
                .queryParam("page", 0)
                .queryParam("size", 1)
                .build()
                .toUri()
        )
    }

    private fun fetchByShortForm(ontologyId: String, type: Type, shortForm: String): ExternalThing? {
        if (!supportsOntology(ontologyId)) return null
        return fetch(
            UriComponentsBuilder.fromUriString(host)
                .path("/ontologies/$ontologyId/${type.endpointPath}")
                .queryParam("shortForm", shortForm)
                .queryParam("exactMatch", true)
                .queryParam("lang", "en")
                .queryParam("includeObsoleteEntities", false)
                .queryParam("page", 0)
                .queryParam("size", 1)
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
            val item = tree.path("elements").toList().singleOrNull()
                ?: return@send null
            ExternalThing(
                uri = ParsedIRI.create(item.path("iri").asText()),
                label = item.path("label").toList()
                    .firstOrNull()?.asText() ?: return@send null,
                description = item.path("definition").toList()
                    .firstOrNull()?.asText()
                    .takeIf { !it.isNullOrBlank() },
            )
        }
    }

    override fun supportsOntology(ontologyId: String): Boolean =
        pattern.matcher(ontologyId).matches()

    override fun supportsMultipleOntologies(): Boolean = true

    private enum class Type(val endpointPath: String) {
        CLASS("classes"),
        PREDICATE("properties"),
        RESOURCE("individuals"),
    }
}
