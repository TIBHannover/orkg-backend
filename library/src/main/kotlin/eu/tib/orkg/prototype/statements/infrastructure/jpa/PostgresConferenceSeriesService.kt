package eu.tib.orkg.prototype.statements.infrastructure.jpa

import eu.tib.orkg.prototype.statements.application.ConferenceSeriesController
import eu.tib.orkg.prototype.statements.application.OrganizationNotFound
import eu.tib.orkg.prototype.statements.domain.model.ConferenceSeriesService
import eu.tib.orkg.prototype.statements.domain.model.ConferenceSeries
import eu.tib.orkg.prototype.statements.domain.model.ConferenceSeriesId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.PeerReviewType
import eu.tib.orkg.prototype.statements.domain.model.jpa.ConferenceSeriesEntity
import eu.tib.orkg.prototype.statements.domain.model.jpa.PostgresConferenceSeriesRepository
import eu.tib.orkg.prototype.statements.domain.model.jpa.PostgresOrganizationRepository
import java.util.Optional
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Service
@Transactional
class PostgresConferenceSeriesService(
    private val postgresConferenceSeriesRepository: PostgresConferenceSeriesRepository,
    private val postgresOrganizationRepository: PostgresOrganizationRepository
) : ConferenceSeriesService {
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
