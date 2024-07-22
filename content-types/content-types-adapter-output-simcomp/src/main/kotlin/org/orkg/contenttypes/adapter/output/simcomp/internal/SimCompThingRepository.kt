package org.orkg.contenttypes.adapter.output.simcomp.internal

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.http.HttpClient
import java.net.http.HttpClient.Version.HTTP_1_1
import java.net.http.HttpRequest
import java.util.*
import org.orkg.common.ThingId
import org.orkg.common.send
import org.orkg.contenttypes.domain.PublishedContentType
import org.springframework.beans.factory.annotation.Value
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
        val result = httpClient.send(request, "SimComp") { response ->
            with(objectMapper.readValue(response, ThingGetResponse::class.java).payload.thing.data) {
                PublishedContentType(rootResource, statements)
            }
        }
        return Optional.ofNullable(result)
    }
}
