package org.orkg.community.adapter.output.jpa

import org.orkg.common.OrganizationId
import org.orkg.community.adapter.output.jpa.internal.ConferenceSeriesEntity
import org.orkg.community.adapter.output.jpa.internal.PostgresConferenceSeriesRepository
import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.community.domain.ConferenceSeries
import org.orkg.community.domain.ConferenceSeriesId
import org.orkg.community.output.ConferenceSeriesRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional

@Component
@TransactionalOnJPA
class SpringJpaPostgresConferenceSeriesAdapter(
    private val postgresConferenceSeriesRepository: PostgresConferenceSeriesRepository,
    private val postgresOrganizationRepository: PostgresOrganizationRepository,
) : ConferenceSeriesRepository {
    override fun save(conferenceSeries: ConferenceSeries) {
        postgresConferenceSeriesRepository.save(
            postgresConferenceSeriesRepository.toConferenceSeriesEntity(
                conferenceSeries,
                postgresOrganizationRepository
            )
        )
    }

    override fun deleteAll() = postgresConferenceSeriesRepository.deleteAll()

    override fun findAll(pageable: Pageable): Page<ConferenceSeries> =
        postgresConferenceSeriesRepository.findAll(pageable).map(ConferenceSeriesEntity::toConferenceSeries)

    override fun findById(id: ConferenceSeriesId): Optional<ConferenceSeries> =
        postgresConferenceSeriesRepository.findById(id.value).map(ConferenceSeriesEntity::toConferenceSeries)

    override fun findAllByOrganizationId(id: OrganizationId, pageable: Pageable): Page<ConferenceSeries> =
        postgresConferenceSeriesRepository.findByOrganizationId(id.value, pageable)
            .map(ConferenceSeriesEntity::toConferenceSeries)

    override fun findByDisplayId(displayId: String): Optional<ConferenceSeries> =
        postgresConferenceSeriesRepository.findByDisplayId(displayId).map(ConferenceSeriesEntity::toConferenceSeries)

    override fun findByName(name: String): Optional<ConferenceSeries> =
        postgresConferenceSeriesRepository.findByName(name).map(ConferenceSeriesEntity::toConferenceSeries)
}
