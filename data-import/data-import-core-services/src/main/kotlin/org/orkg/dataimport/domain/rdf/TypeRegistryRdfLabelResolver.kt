package org.orkg.dataimport.domain.rdf

import org.eclipse.rdf4j.model.Resource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.USER_AGENT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import tools.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID

@Component
class TypeRegistryRdfLabelResolver(
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper,
    @param:Value($$"${orkg.http.user-agent}")
    private val userAgent: String,
    @param:Value($$"${orkg.external-services.typeregistry.host}")
    private val host: String,
) : RdfLabelResolver {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun resolve(rdfFileId: UUID, resource: Resource): String? {
        try {
            val apiUri = UriComponentsBuilder.fromUriString(host)
                .path("/0.DOIP/Op.Retrieve")
                .queryParam("targetId", resource.stringValue().substringAfter("https://doi.org/").substringBefore("#"))
                .build()
                .toUri()
            val request = HttpRequest.newBuilder(apiUri)
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(USER_AGENT, userAgent)
                .GET()
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())
            if (response.statusCode() != HttpStatus.OK.value()) {
                return null
            }
            val body = objectMapper.readTree(response.body())
            val fragment = URI.create(resource.stringValue()).fragment
            if (fragment != null) {
                return body.path("attributes").path("content").path("Schema").path("Properties")
                    .firstOrNull { it.path("Name").stringValue(null) == fragment }
                    ?.let { it.path("Title").stringValue(null) ?: it.path("Name").stringValue(null) }
            }
            return body.path("attributes").path("content").path("name").stringValue(null)
        } catch (t: Throwable) {
            logger.error("Could not fetch resource label from type registry", t)
        }
        return null
    }

    override fun canResolve(rdfFileId: UUID, resource: Resource): Boolean =
        resource.stringValue().startsWith("https://doi.org/21.T11969/")
}
