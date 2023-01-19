package eu.tib.orkg.prototype.community.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.community.api.ConferenceSeriesUseCases
import eu.tib.orkg.prototype.community.domain.model.ConferenceSeries
import eu.tib.orkg.prototype.community.domain.model.ConferenceSeriesId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import java.time.LocalDate
import java.util.*
import javax.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/conference-series/")
class ConferenceSeriesController(
    private val service: ConferenceSeriesUseCases
) {
    @RequestMapping("/", method = [RequestMethod.POST, RequestMethod.PUT])
    fun addConferenceSeries(
        @RequestBody @Valid conference: ConferenceSeriesRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        if (service.findByName(conference.name).isPresent) {
            throw ConferenceAlreadyExists.withName(conference.name)
        } else if (service.findByDisplayId(conference.displayId).isPresent) {
            throw ConferenceAlreadyExists.withDisplayId(conference.displayId)
        }
        val response = service.create(
            conference.organizationId,
            conference.name,
            conference.url,
            conference.displayId,
            conference.metadata
        )
        val location = uriComponentsBuilder
           .path("api/conference-series/{id}")
           .buildAndExpand(response.id)
           .toUri()
        return ResponseEntity.created(location).body(service.findById(response.id).get())
    }

    @GetMapping("/")
    fun listConferenceSeries(pageable: Pageable): Page<ConferenceSeries> =
        service.listConferenceSeries(pageable)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String): ConferenceSeries {
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

    @GetMapping("{id}/series")
    fun findSeriesByConference(@PathVariable id: OrganizationId, pageable: Pageable): Page<ConferenceSeries> {
        return service.findSeriesByConference(id, pageable)
    }
    fun String.isValidUUID(id: String): Boolean = try { UUID.fromString(id) != null } catch (e: IllegalArgumentException) { false }

    data class ConferenceSeriesRequest(
        @JsonProperty("organization_id")
        val organizationId: OrganizationId,
        val name: String,
        @JsonProperty("display_id")
        val displayId: String,
        val url: String,
        val metadata: Metadata
    )
    data class Metadata(
        // conference start date
        @JsonProperty("start_date")
        val startDate: LocalDate,
        @JsonProperty("review_type")
        val reviewType: String
    )
}
