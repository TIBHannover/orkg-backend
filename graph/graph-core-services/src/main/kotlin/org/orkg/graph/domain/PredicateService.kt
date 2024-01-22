package org.orkg.graph.domain

import dev.forkhandles.values.ofOrNull
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.UpdatePredicateUseCase
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PredicateService(
    private val repository: PredicateRepository,
    private val statementRepository: StatementRepository,
    private val clock: Clock,
) : PredicateUseCases {
    @Transactional(readOnly = true)
    override fun exists(id: ThingId): Boolean = repository.exists(id)

    override fun create(command: CreatePredicateUseCase.CreateCommand): ThingId {
        val id = command.id
            ?.also { id -> repository.findById(id).ifPresent { throw PredicateAlreadyExists(id) } }
            ?: repository.nextIdentity()
        val predicate = Predicate(
            id = id,
            label = Label.ofOrNull(command.label)?.value ?: throw InvalidLabel(),
            createdAt = OffsetDateTime.now(clock),
            createdBy = command.contributorId ?: ContributorId.createUnknownContributor()
        )
        repository.save(predicate)
        return id
    }

    override fun findAll(pageable: Pageable): Page<Predicate> =
        repository.findAll(pageable)

    override fun findById(id: ThingId): Optional<Predicate> =
        repository.findById(id)

    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Predicate> =
        repository.findAllByLabel(labelSearchString, pageable)

    override fun update(id: ThingId, command: UpdatePredicateUseCase.ReplaceCommand) {
        var found = repository.findById(id).get()

        // update all the properties
        found = found.copy(label = command.label)

        repository.save(found)
    }

    override fun delete(predicateId: ThingId) {
        val predicate = findById(predicateId).orElseThrow { PredicateNotFound(predicateId) }

        if (statementRepository.countPredicateUsage(predicate.id) > 0)
            throw PredicateUsedInStatement(predicate.id)

        repository.deleteById(predicate.id)
    }

    override fun removeAll() = repository.deleteAll()
}
