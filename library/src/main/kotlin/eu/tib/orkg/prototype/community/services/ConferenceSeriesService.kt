package eu.tib.orkg.prototype.community.services

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.ConferenceSeriesEntity
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresConferenceSeriesRepository
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.api.ConferenceSeriesUseCases
import eu.tib.orkg.prototype.community.application.ConferenceSeriesController
import eu.tib.orkg.prototype.community.application.OrganizationNotFound
import eu.tib.orkg.prototype.community.domain.model.ConferenceSeries
import eu.tib.orkg.prototype.community.domain.model.ConferenceSeriesId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.PeerReviewType
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ConferenceSeriesService(
    private val postgresConferenceSeriesRepository: PostgresConferenceSeriesRepository,
    private val postgresOrganizationRepository: PostgresOrganizationRepository
) : ConferenceSeriesUseCases {
    override fun create(id: OrganizationId, name: String, url: String, displayId: String, metadata: ConferenceSeriesController.Metadata): ConferenceSeries {
        val seriesId = UUID.randomUUID()
        val org = postgresOrganizationRepository
            .findById(id.value)
            .orElseThrow { OrganizationNotFound(id) }
        val newSeries = ConferenceSeriesEntity().apply {
            organization = org
            this.id = seriesId
            this.name = name
            this.displayId = displayId
            this.url = url
            startDate = metadata.startDate
            reviewType = PeerReviewType.fromOrNull(metadata.reviewType)
        }
        return postgresConferenceSeriesRepository.save(newSeries).toConferenceSeries()
    }

    override fun listConferenceSeries(pageable: Pageable): Page<ConferenceSeries> {
        return postgresConferenceSeriesRepository.findAll(pageable)
            .map(ConferenceSeriesEntity::toConferenceSeries)
    }

    override fun findById(id: ConferenceSeriesId): Optional<ConferenceSeries> =
        postgresConferenceSeriesRepository
            .findById(id.value)
            .map(ConferenceSeriesEntity::toConferenceSeries)

    override fun findByName(name: String): Optional<ConferenceSeries> =
        postgresConferenceSeriesRepository
            .findByName(name)
            .map(ConferenceSeriesEntity::toConferenceSeries)

    override fun findByDisplayId(name: String): Optional<ConferenceSeries> =
        postgresConferenceSeriesRepository
            .findByDisplayId(name)
            .map(ConferenceSeriesEntity::toConferenceSeries)

    override fun findSeriesByConference(id: OrganizationId, pageable: Pageable): Page<ConferenceSeries> =
        postgresConferenceSeriesRepository.findByOrganizationId(id.value, pageable)
            .map(ConferenceSeriesEntity::toConferenceSeries)

    override fun removeAll() = postgresConferenceSeriesRepository.deleteAll()
}