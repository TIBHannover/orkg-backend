package org.orkg.contenttypes.adapter.output.simcomp.internal

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.http.HttpClient
import java.net.http.HttpClient.Version.HTTP_1_1
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.contenttypes.domain.PublishedContentType
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class SimCompThingRepository(
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient,
    @Value("\${orkg.simcomp.host}")
    private val host: String = "http://localhost/simcomp"
) {
    fun findById(id: ThingId, type: ThingType): Optional<PublishedContentType> {
        val uri = UriComponentsBuilder.fromHttpUrl(host)
            .path("/thing/") // The trailing slash is important, otherwise we get a redirect (307)
            .queryParam("thing_type", type.name)
            .queryParam("thing_key", id.value)
            .build()
            .toUri()
        val request = HttpRequest.newBuilder()
            .uri(uri)
            .version(HTTP_1_1)
            .header("Accept", MediaType.APPLICATION_JSON_VALUE)
            .GET()
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return when (response.statusCode()) {
            HttpStatus.OK.value() ->
                with(objectMapper.readValue(response.body(), ThingGetResponse::class.java).payload.thing.data) {
                    Optional.of(PublishedContentType(rootResource, statements))
                }
            HttpStatus.NOT_FOUND.value() -> Optional.empty()
            else -> throw ServiceUnavailable.create("SimComp", response.statusCode(), response.body())
        }
    }
}
