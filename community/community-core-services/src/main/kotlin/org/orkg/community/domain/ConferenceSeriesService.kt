package org.orkg.community.domain

import java.util.*
import org.orkg.common.OrganizationId
import org.orkg.community.input.ConferenceSeriesUseCases
import org.orkg.community.output.ConferenceSeriesRepository
import org.orkg.community.output.OrganizationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ConferenceSeriesService(
    private val postgresConferenceSeriesRepository: ConferenceSeriesRepository,
    private val postgresOrganizationRepository: OrganizationRepository
) : ConferenceSeriesUseCases {
    override fun create(id: ConferenceSeriesId?, organizationId: OrganizationId, name: String, url: String, displayId: String, metadata: Metadata): ConferenceSeries {
        postgresOrganizationRepository.findById(organizationId).orElseThrow { OrganizationNotFound(organizationId) }

        val seriesId = id ?: ConferenceSeriesId(UUID.randomUUID())
        val newSeries = ConferenceSeries(
            id = seriesId,
            name = name,
            displayId = displayId,
            homepage = url,
            organizationId = organizationId,
            metadata = metadata,
        )
        postgresConferenceSeriesRepository.save(newSeries)
        return newSeries
    }

    override fun listConferenceSeries(pageable: Pageable): Page<ConferenceSeries> =
        postgresConferenceSeriesRepository.findAll(pageable)

    override fun findById(id: ConferenceSeriesId): Optional<ConferenceSeries> =
        postgresConferenceSeriesRepository.findById(id)

    override fun findByName(name: String): Optional<ConferenceSeries> =
        postgresConferenceSeriesRepository.findByName(name)

    override fun findByDisplayId(name: String): Optional<ConferenceSeries> =
        postgresConferenceSeriesRepository.findByDisplayId(name)

    override fun findSeriesByConference(id: OrganizationId, pageable: Pageable): Page<ConferenceSeries> =
        postgresConferenceSeriesRepository.findByOrganizationId(id, pageable)

    override fun removeAll() = postgresConferenceSeriesRepository.deleteAll()
}
