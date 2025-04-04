package org.orkg.graph.domain

import dev.forkhandles.values.ofOrNull
import org.orkg.common.ThingId
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UpdateListUseCase
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.Optional

@Service
@TransactionalOnNeo4j
class ListService(
    private val repository: ListRepository,
    private val thingRepository: ThingRepository,
    private val clock: Clock,
) : ListUseCases {
    override fun create(command: CreateListUseCase.CreateCommand): ThingId {
        val label = Label.ofOrNull(command.label)?.value
            ?: throw InvalidLabel()
        val id = command.id
            ?.also { id -> thingRepository.findById(id).ifPresent { throw ThingAlreadyExists(id) } }
            ?: repository.nextIdentity()
        if (command.elements.isNotEmpty() && !thingRepository.existsAllById(command.elements.toSet())) {
            throw ListElementNotFound()
        }
        val list = List(
            id = id,
            label = label,
            elements = command.elements,
            createdAt = OffsetDateTime.now(clock),
            createdBy = command.contributorId,
            modifiable = command.modifiable
        )
        repository.save(list, list.createdBy)
        return id
    }

    override fun findById(id: ThingId): Optional<List> =
        repository.findById(id)

    override fun findAllElementsById(id: ThingId, pageable: Pageable): Page<Thing> {
        if (!existsById(id)) throw ListNotFound(id)
        return repository.findAllElementsById(id, pageable)
    }

    override fun existsById(id: ThingId): Boolean = repository.existsById(id)

    override fun update(command: UpdateListUseCase.UpdateCommand) {
        val list = repository.findById(command.id)
            .orElseThrow { ListNotFound(command.id) }
        if (!list.modifiable) {
            throw ListNotModifiable(command.id)
        }
        val label = command.label?.also {
            Label.ofOrNull(it) ?: throw InvalidLabel()
        }
        val elements = command.elements?.also {
            if (it.isNotEmpty() && !thingRepository.existsAllById(it.toSet())) {
                throw ListElementNotFound()
            }
        }
        val updated = list.copy(
            label = label ?: list.label,
            elements = elements ?: list.elements
        )
        if (updated != list) {
            repository.save(updated, command.contributorId)
        }
    }

    override fun deleteById(id: ThingId) {
        repository.findById(id).ifPresent {
            if (!it.modifiable) {
                throw ListNotModifiable(id)
            }
            if (thingRepository.isUsedAsObject(id)) {
                throw ListInUse(id)
            }
            repository.deleteById(id)
        }
    }
}
