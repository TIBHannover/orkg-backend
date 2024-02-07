package org.orkg.graph.domain

import dev.forkhandles.values.ofOrNull
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UpdateListUseCase
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ListService(
    private val repository: ListRepository,
    private val thingRepository: ThingRepository,
    private val clock: Clock,
) : ListUseCases {
    override fun create(command: CreateListUseCase.CreateCommand): ThingId {
        val label = Label.ofOrNull(command.label)?.value
            ?: throw InvalidLabel()
        if (command.elements.isNotEmpty() && !thingRepository.existsAll(command.elements.toSet()))
            throw ListElementNotFound()
        val id = command.id ?: repository.nextIdentity()
        val list = List(
            id = id,
            label = label,
            elements = command.elements,
            createdAt = OffsetDateTime.now(clock),
            createdBy = command.contributorId ?: ContributorId.UNKNOWN,
            modifiable = command.modifiable
        )
        repository.save(list, list.createdBy)
        return id
    }

    override fun findById(id: ThingId): Optional<List> =
        repository.findById(id)

    override fun findAllElementsById(id: ThingId, pageable: Pageable): Page<Thing> {
        if (!exists(id)) throw ListNotFound(id)
        return repository.findAllElementsById(id, pageable)
    }

    override fun exists(id: ThingId): Boolean = repository.exists(id)

    override fun update(id: ThingId, command: UpdateListUseCase.UpdateCommand) {
        val list = repository.findById(id).orElseThrow { ListNotFound(id) }
        if (!list.modifiable)
            throw ListNotModifiable(id)
        val label = command.label?.let {
            Label.ofOrNull(it)?.value ?: throw InvalidLabel()
        }
        val elements = command.elements?.also {
            if (it.isNotEmpty() && !thingRepository.existsAll(it.toSet()))
                throw ListElementNotFound()
        }
        repository.save(
            list.copy(
                label = label ?: list.label,
                elements = elements ?: list.elements
            ),
            contributorId = command.contributorId ?: ContributorId.UNKNOWN
        )
    }

    override fun delete(id: ThingId) {
        repository.findById(id).ifPresent {
            if (!it.modifiable) {
                throw ListNotModifiable(id)
            }
            repository.delete(id)
        }
    }
}
