package eu.tib.orkg.prototype.community.api

import eu.tib.orkg.prototype.community.domain.model.ConferenceSeries
import eu.tib.orkg.prototype.community.domain.model.ConferenceSeriesId
import eu.tib.orkg.prototype.community.domain.model.Metadata
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ConferenceSeriesUseCases {

    fun create(id: ConferenceSeriesId?, organizationId: OrganizationId, name: String, url: String, displayId: String, metadata: Metadata): ConferenceSeries

    fun listConferenceSeries(pageable: Pageable): Page<ConferenceSeries>

    fun findSeriesByConference(id: OrganizationId, pageable: Pageable): Page<ConferenceSeries>

    fun findById(id: ConferenceSeriesId): Optional<ConferenceSeries>

    fun findByName(name: String): Optional<ConferenceSeries>

    fun findByDisplayId(name: String): Optional<ConferenceSeries>

    fun removeAll()
}
