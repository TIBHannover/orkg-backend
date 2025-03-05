package org.orkg.graph.domain

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UpdatePredicateUseCase
import org.orkg.graph.output.PredicateRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime

@Service
@TransactionalOnNeo4j
class UnsafePredicateService(
    private val repository: PredicateRepository,
    private val clock: Clock,
) : UnsafePredicateUseCases {
    override fun create(command: CreatePredicateUseCase.CreateCommand): ThingId {
        val predicate = Predicate(
            id = command.id ?: repository.nextIdentity(),
            label = command.label,
            createdAt = OffsetDateTime.now(clock),
            createdBy = command.contributorId,
            modifiable = command.modifiable
        )
        repository.save(predicate)
        return predicate.id
    }

    override fun update(command: UpdatePredicateUseCase.UpdateCommand) {
        if (command.hasNoContents()) return
        val predicate = repository.findById(command.id)
            .orElseThrow { PredicateNotFound(command.id) }
        val updated = predicate.apply(command)
        if (updated != predicate) {
            repository.save(updated)
        }
    }

    override fun delete(id: ThingId, contributorId: ContributorId) {
        repository.deleteById(id)
    }

    override fun deleteAll() = repository.deleteAll()
}
