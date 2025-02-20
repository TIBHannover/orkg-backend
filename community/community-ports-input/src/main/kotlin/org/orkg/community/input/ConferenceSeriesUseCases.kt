package org.orkg.community.input

import org.orkg.common.OrganizationId
import org.orkg.community.domain.ConferenceSeries
import org.orkg.community.domain.ConferenceSeriesId
import org.orkg.community.domain.Metadata
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ConferenceSeriesUseCases {
    fun create(id: ConferenceSeriesId?, organizationId: OrganizationId, name: String, url: String, displayId: String, metadata: Metadata): ConferenceSeries

    fun findAll(pageable: Pageable): Page<ConferenceSeries>

    fun findAllByOrganizationId(id: OrganizationId, pageable: Pageable): Page<ConferenceSeries>

    fun findById(id: ConferenceSeriesId): Optional<ConferenceSeries>

    fun findByName(name: String): Optional<ConferenceSeries>

    fun findByDisplayId(name: String): Optional<ConferenceSeries>

    fun deleteAll()
}
