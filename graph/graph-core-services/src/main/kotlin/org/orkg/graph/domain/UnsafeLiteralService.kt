package org.orkg.graph.domain

import org.orkg.common.ThingId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UpdateLiteralUseCase
import org.orkg.graph.output.LiteralRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime

@Service
@TransactionalOnNeo4j
class UnsafeLiteralService(
    private val repository: LiteralRepository,
    private val clock: Clock,
) : UnsafeLiteralUseCases {
    override fun create(command: CreateLiteralUseCase.CreateCommand): ThingId {
        val literal = Literal(
            label = command.label,
            id = command.id ?: repository.nextIdentity(),
            datatype = command.datatype,
            createdBy = command.contributorId,
            createdAt = OffsetDateTime.now(clock),
            modifiable = command.modifiable
        )
        repository.save(literal)
        return literal.id
    }

    override fun update(command: UpdateLiteralUseCase.UpdateCommand) {
        if (command.hasNoContents()) return
        val literal = repository.findById(command.id)
            .orElseThrow { LiteralNotFound(command.id) }
        val updated = literal.apply(command)
        if (updated != literal) {
            repository.save(updated)
        }
    }

    override fun deleteAll() = repository.deleteAll()
}
