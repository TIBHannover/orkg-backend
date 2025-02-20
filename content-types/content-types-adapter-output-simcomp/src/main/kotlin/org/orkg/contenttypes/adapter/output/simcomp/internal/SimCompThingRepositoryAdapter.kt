package org.orkg.contenttypes.adapter.output.simcomp.internal

import com.fasterxml.jackson.databind.ObjectMapper
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.send
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException
import java.net.http.HttpClient
import java.net.http.HttpClient.Version.HTTP_1_1
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Optional

@Component
@Profile("development", "production")
class SimCompThingRepositoryAdapter(
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient,
    private val bodyPublisherFactory: (String) -> HttpRequest.BodyPublisher = HttpRequest.BodyPublishers::ofString,
    @Value("\${orkg.simcomp.host}")
    private val host: String,
    @Value("\${orkg.simcomp.api-key}")
    private val apiKey: String,
) : SimCompThingRepository {
    override fun findById(id: ThingId, type: ThingType): Optional<BaseThing> {
        val uri = UriComponentsBuilder.fromUriString(host)
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
            objectMapper.readValue(response, ThingGetResponse::class.java).payload.thing
        }
        return Optional.ofNullable(result)
    }

    override fun save(id: ThingId, type: ThingType, data: Any, config: Any) {
        val body = ThingAddRequest(
            thingType = type,
            thingKey = id,
            config = config,
            data = data
        )
        val uri = UriComponentsBuilder.fromUriString(host)
            .path("/thing/") // The trailing slash is important, otherwise we get a redirect (307)
            .build()
            .toUri()
        val request = HttpRequest.newBuilder()
            .uri(uri)
            .version(HTTP_1_1)
            .header("Accept", MediaType.APPLICATION_JSON_VALUE)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .POST(bodyPublisherFactory(objectMapper.writeValueAsString(body)))
            .build()
        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != HttpStatus.CREATED.value()) {
                throw ServiceUnavailable.create("SimComp", response.statusCode(), response.body())
            }
        } catch (e: IOException) {
            throw ServiceUnavailable.create("SimComp", e)
        }
    }

    override fun update(id: ThingId, type: ThingType, data: Any, config: Any) {
        val body = ThingAddRequest(
            thingType = type,
            thingKey = id,
            config = config,
            data = data
        )
        val uri = UriComponentsBuilder.fromUriString(host)
            .path("/thing/") // The trailing slash is important, otherwise we get a redirect (307)
            .build()
            .toUri()
        val request = HttpRequest.newBuilder()
            .uri(uri)
            .version(HTTP_1_1)
            .header("Accept", MediaType.APPLICATION_JSON_VALUE)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("X-API-KEY", apiKey)
            .PUT(bodyPublisherFactory(objectMapper.writeValueAsString(body)))
            .build()
        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != HttpStatus.NO_CONTENT.value()) {
                throw ServiceUnavailable.create("SimComp", response.statusCode(), response.body())
            }
        } catch (e: IOException) {
            throw ServiceUnavailable.create("SimComp", e)
        }
    }
}
