package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.application.ConferenceSeriesController
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ConferenceSeriesService {

    fun create(id: OrganizationId, name: String, url: String, displayId: String, metadata: ConferenceSeriesController.Metadata): ConferenceSeries

    fun listConferenceSeries(pageable: Pageable): Page<ConferenceSeries>

    fun findSeriesByConference(id: OrganizationId, pageable: Pageable): Page<ConferenceSeries>

    fun findById(id: ConferenceSeriesId): Optional<ConferenceSeries>

    fun findByName(name: String): Optional<ConferenceSeries>

    fun findByDisplayId(name: String): Optional<ConferenceSeries>

    fun removeAll()
}
