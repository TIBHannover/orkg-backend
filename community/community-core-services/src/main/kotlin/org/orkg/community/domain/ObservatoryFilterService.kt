package org.orkg.community.domain

import java.time.Clock
import java.time.LocalDateTime
import java.util.*
import org.orkg.common.ObservatoryId
import org.orkg.community.input.CreateObservatoryFilterUseCase
import org.orkg.community.input.ObservatoryFilterUseCases
import org.orkg.community.input.UpdateObservatoryFilterUseCase
import org.orkg.community.output.ObservatoryFilterRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@TransactionalOnJPA
class ObservatoryFilterService(
    private val repository: ObservatoryFilterRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val predicateRepository: PredicateRepository,
    private val classRepository: ClassRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) : ObservatoryFilterUseCases {

    override fun create(command: CreateObservatoryFilterUseCase.CreateCommand): ObservatoryFilterId {
        val observatory = observatoryRepository
            .findById(command.observatoryId)
            .orElseThrow { ObservatoryNotFound(command.observatoryId) }
        classRepository.findById(command.range).orElseThrow { ClassNotFound.withThingId(command.range) }
        command.path.forEach {
            predicateRepository.findById(it).orElseThrow { PredicateNotFound(it) }
        }
        val id = command.id?.also { id ->
            repository.findById(id).ifPresent { throw ObservatoryFilterAlreadyExists(id) }
        } ?: repository.nextIdentity()
        val observatoryFilter = ObservatoryFilter(
            id = id,
            observatoryId = observatory.id,
            label = command.label,
            createdBy = command.contributorId,
            createdAt = LocalDateTime.now(clock),
            path = command.path,
            range = command.range,
            exact = command.exact,
            featured = command.featured
        )
        repository.save(observatoryFilter)
        return id
    }

    override fun update(command: UpdateObservatoryFilterUseCase.UpdateCommand) {
        val filter = repository.findById(command.id)
            .orElseThrow { ObservatoryFilterNotFound(command.id) }
        if (command.range != null) {
            classRepository.findById(command.range!!).orElseThrow { ClassNotFound.withThingId(command.range!!) }
        }
        command.path?.forEach {
            predicateRepository.findById(it).orElseThrow { PredicateNotFound(it) }
        }
        repository.save(
            filter.copy(
                label = command.label ?: filter.label,
                path = command.path ?: filter.path,
                range = command.range ?: filter.range,
                exact = command.exact ?: filter.exact,
                featured = command.featured ?: filter.featured
            )
        )
    }

    override fun findById(id: ObservatoryFilterId): Optional<ObservatoryFilter> =
        repository.findById(id)

    override fun findAllByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<ObservatoryFilter> =
        repository.findAllByObservatoryId(id, pageable)

    override fun deleteById(id: ObservatoryFilterId) =
        repository.deleteById(id)

    override fun deleteAll() =
        repository.deleteAll()
}
