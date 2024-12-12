package org.orkg.graph.adapter.output.web

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.util.function.Predicate
import java.util.regex.Pattern
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.send
import org.orkg.graph.domain.ExternalThing
import org.orkg.graph.output.ExternalClassService
import org.orkg.graph.output.ExternalPredicateService
import org.orkg.graph.output.ExternalResourceService
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.util.Predicates
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class WikidataServiceAdapter(
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient,
    @Value("\${orkg.external-services.wikidata.host}")
    private val host: String
) : ExternalResourceService, ExternalClassService, ExternalPredicateService {
    private val itemIdPattern = Pattern.compile("(Q[0-9]+)")
    private val propertyIdPattern = Pattern.compile("(P[0-9]+)")
    private val itemPattern = Pattern.compile("""https?://(?:www\.)?wikidata.org/entity/(Q[0-9]+)/?""")
    private val propertyPattern = Pattern.compile("""https?://(?:www\.)?wikidata.org/entity/(P[0-9]+)/?""")

    override fun findResourceByShortForm(ontologyId: String, shortForm: String): ExternalThing? =
        fetch(ontologyId, shortForm, itemIdPattern, ::isResource)

    override fun findResourceByURI(ontologyId: String, uri: ParsedIRI): ExternalThing? =
        fetch(ontologyId, uri.toString(), itemPattern, ::isResource)

    override fun findClassByShortForm(ontologyId: String, shortForm: String): ExternalThing? =
        fetch(ontologyId, shortForm, itemIdPattern, ::isClass)

    override fun findClassByURI(ontologyId: String, uri: ParsedIRI): ExternalThing? =
        fetch(ontologyId, uri.toString(), itemPattern, ::isClass)

    override fun findPredicateByShortForm(ontologyId: String, shortForm: String): ExternalThing? =
        fetch(ontologyId, shortForm, propertyIdPattern)

    override fun findPredicateByURI(ontologyId: String, uri: ParsedIRI): ExternalThing? =
        fetch(ontologyId, uri.toString(), propertyPattern)

    private fun isResource(claims: JsonNode): Boolean {
        return claims.size() == 0 || claims.has("P31")
    }

    private fun isClass(claims: JsonNode): Boolean {
        return claims.size() == 0 || claims.has("P279")
    }

    private fun fetch(
        ontologyId: String,
        input: String,
        pattern: Pattern,
        predicate: Predicate<JsonNode> = Predicates.isTrue()
    ): ExternalThing? {
        if (!supportsOntology(ontologyId)) return null
        val id = pattern.matchSingleGroupOrNull(input) ?: return null
        val uri = UriComponentsBuilder.fromUriString(host)
            .path("/w/api.php")
            .queryParam("action", "wbgetentities")
            .queryParam("ids", id)
            .queryParam("format", "json")
            .queryParam("languages", "en")
            .queryParam("props", "labels|descriptions|datatype|claims")
            .build()
            .toUri()
        val request = HttpRequest.newBuilder()
            .uri(uri)
            .header("Accept", MediaType.APPLICATION_JSON_VALUE)
            .GET()
            .build()
        return httpClient.send(request, "Wikidata") { response ->
            val tree = objectMapper.readTree(response)
            val error = tree.path("error").path("code")
            if (!error.isMissingNode) {
                if (error.textValue() == "no-such-entity") {
                    return@send null
                } else {
                    throw ServiceUnavailable.create("Wikidata", HttpStatus.SERVICE_UNAVAILABLE.value(), response)
                }
            }
            val entity = tree.path("entities").path(id)
            if (!predicate.test(entity.path("claims"))) {
                return@send null
            }
            ExternalThing(
                uri = ParsedIRI("https://www.wikidata.org/entity/$id"),
                label = entity.path("labels").path("en").path("value").asText(),
                description = entity.path("descriptions").path("en").path("value").asText()
            )
        }
    }

    override fun supportsOntology(ontologyId: String): Boolean {
        return ontologyId == "wikidata"
    }

    override fun supportsMultipleOntologies(): Boolean = false
}
