package eu.tib.orkg.prototype.statements.services

import dev.forkhandles.values.ofOrNull
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.CreateListUseCase
import eu.tib.orkg.prototype.statements.api.ListUseCases
import eu.tib.orkg.prototype.statements.api.UpdateListUseCase
import eu.tib.orkg.prototype.statements.application.InvalidLabel
import eu.tib.orkg.prototype.statements.application.ListElementNotFound
import eu.tib.orkg.prototype.statements.application.ListNotFound
import eu.tib.orkg.prototype.statements.domain.model.Label
import eu.tib.orkg.prototype.statements.domain.model.List
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ListRepository
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ListService(
    private val repository: ListRepository,
    private val thingRepository: ThingRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
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
            createdBy = command.contributorId ?: ContributorId.createUnknownContributor(),
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
        val label = command.label?.let {
            Label.ofOrNull(command.label)?.value ?: throw InvalidLabel()
        }
        val elements = command.elements?.apply {
            if (!thingRepository.existsAll(command.elements.toSet()))
                throw ListElementNotFound()
        }
        repository.save(
            list.copy(
                label = label ?: list.label,
                elements = elements ?: list.elements
            ),
            contributorId = command.contributorId ?: ContributorId.createUnknownContributor()
        )
    }

    override fun delete(id: ThingId) = repository.delete(id)
}
