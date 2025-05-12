package org.orkg.community.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import org.orkg.common.OrganizationId
import org.orkg.community.domain.BadPeerReviewType
import org.orkg.community.domain.ConferenceAlreadyExists
import org.orkg.community.domain.ConferenceSeries
import org.orkg.community.domain.ConferenceSeriesId
import org.orkg.community.domain.ConferenceSeriesNotFound
import org.orkg.community.domain.Metadata
import org.orkg.community.domain.PeerReviewType
import org.orkg.community.input.ConferenceSeriesUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/conference-series", produces = [MediaType.APPLICATION_JSON_VALUE])
class ConferenceSeriesController(
    private val service: ConferenceSeriesUseCases,
) {
    @RequestMapping(method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun addConferenceSeries(
        @RequestBody @Valid conference: ConferenceSeriesRequest,
        uriComponentsBuilder: UriComponentsBuilder,
    ): ResponseEntity<Any> {
        if (service.findByName(conference.name).isPresent) {
            throw ConferenceAlreadyExists.withName(conference.name)
        } else if (service.findByDisplayId(conference.displayId).isPresent) {
            throw ConferenceAlreadyExists.withDisplayId(conference.displayId)
        }
        val response = service.create(
            id = null,
            conference.organizationId,
            conference.name,
            conference.url,
            conference.displayId,
            Metadata(
                startDate = conference.metadata.startDate,
                reviewType = PeerReviewType.fromOrNull(conference.metadata.reviewType)
                    ?: throw BadPeerReviewType(conference.metadata.reviewType),
            )
        )
        val location = uriComponentsBuilder
            .path("/api/conference-series/{id}")
            .buildAndExpand(response.id)
            .toUri()
        return created(location).build()
    }

    @GetMapping
    fun listConferenceSeries(pageable: Pageable): Page<ConferenceSeries> =
        service.findAll(pageable)

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: String,
    ): ConferenceSeries {
        val response: ConferenceSeries = if (id.isValidUUID(id)) {
            service
                .findById(ConferenceSeriesId(id))
                .orElseThrow { ConferenceSeriesNotFound(id) }
        } else {
            service
                .findByDisplayId(id)
                .orElseThrow { ConferenceSeriesNotFound(id) }
        }
        return response
    }

    @GetMapping("/{id}/series")
    fun findSeriesByConference(
        @PathVariable id: OrganizationId,
        pageable: Pageable,
    ): Page<ConferenceSeries> = service.findAllByOrganizationId(id, pageable)

    fun String.isValidUUID(id: String): Boolean = try {
        UUID.fromString(id) != null
    } catch (e: IllegalArgumentException) {
        false
    }

    data class ConferenceSeriesRequest(
        @JsonProperty("organization_id")
        val organizationId: OrganizationId,
        val name: String,
        @JsonProperty("display_id")
        val displayId: String,
        val url: String,
        val metadata: MetadataRequest,
    )

    data class MetadataRequest(
        // conference start date
        @JsonProperty("start_date")
        val startDate: LocalDate,
        @JsonProperty("review_type")
        val reviewType: String,
    )
}
