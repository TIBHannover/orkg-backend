package org.orkg.community.adapter.output.jpa

import org.orkg.common.ObservatoryId
import org.orkg.community.adapter.output.jpa.internal.ObservatoryFilterEntity
import org.orkg.community.adapter.output.jpa.internal.PostgresObservatoryFilterRepository
import org.orkg.community.adapter.output.jpa.internal.toObservatoryFilter
import org.orkg.community.domain.ObservatoryFilter
import org.orkg.community.domain.ObservatoryFilterId
import org.orkg.community.output.ObservatoryFilterRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
@TransactionalOnJPA
class SpringDataJpaObservatoryFilterAdapter(
    private val postgresRepository: PostgresObservatoryFilterRepository,
) : ObservatoryFilterRepository {
    override fun save(observatoryFilter: ObservatoryFilter) {
        postgresRepository.save(observatoryFilter.toObservatoryFilterEntity())
    }

    override fun findById(id: ObservatoryFilterId): Optional<ObservatoryFilter> =
        postgresRepository.findById(id.value)
            .map { it.toObservatoryFilter() }

    override fun findAllByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<ObservatoryFilter> =
        postgresRepository.findAllByObservatoryId(id.value, pageable)
            .map { it.toObservatoryFilter() }

    override fun deleteById(id: ObservatoryFilterId) {
        postgresRepository.deleteById(id.value)
    }

    override fun nextIdentity(): ObservatoryFilterId {
        var id: ObservatoryFilterId
        do {
            id = ObservatoryFilterId(UUID.randomUUID())
        } while (postgresRepository.existsById(id.value))
        return id
    }

    override fun deleteAll() = postgresRepository.deleteAll()

    internal fun ObservatoryFilter.toObservatoryFilterEntity() =
        postgresRepository.findById(id.value).orElseGet { ObservatoryFilterEntity() }.apply {
            id = this@toObservatoryFilterEntity.id.value
            observatoryId = this@toObservatoryFilterEntity.observatoryId.value
            label = this@toObservatoryFilterEntity.label
            createdBy = this@toObservatoryFilterEntity.createdBy.value
            createdAt = this@toObservatoryFilterEntity.createdAt
            createdAtOffsetTotalSeconds = this@toObservatoryFilterEntity.createdAt.offset.totalSeconds
            path = this@toObservatoryFilterEntity.path.joinToString(",")
            range = this@toObservatoryFilterEntity.range.value
            exact = this@toObservatoryFilterEntity.exact
            featured = this@toObservatoryFilterEntity.featured
        }
}
