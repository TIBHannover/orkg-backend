package org.orkg.graph.domain

import org.orkg.common.ThingId
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UpdateClassUseCase
import org.orkg.graph.output.ClassRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime

@Service
@TransactionalOnNeo4j
class UnsafeClassService(
    private val repository: ClassRepository,
    private val clock: Clock,
) : UnsafeClassUseCases {
    override fun create(command: CreateClassUseCase.CreateCommand): ThingId {
        val `class` = Class(
            id = command.id ?: repository.nextIdentity(),
            label = command.label,
            uri = command.uri,
            createdAt = OffsetDateTime.now(clock),
            createdBy = command.contributorId,
            modifiable = command.modifiable
        )
        repository.save(`class`)
        return `class`.id
    }

    override fun update(command: UpdateClassUseCase.UpdateCommand) {
        if (command.hasNoContents()) return
        val `class` = repository.findById(command.id)
            .orElseThrow { ClassNotFound.withThingId(command.id) }
        val updated = `class`.apply(command)
        if (updated != `class`) {
            repository.save(updated)
        }
    }

    override fun replace(command: UpdateClassUseCase.ReplaceCommand) {
        val `class` = repository.findById(command.id)
            .orElseThrow { ClassNotFound.withThingId(command.id) }
        val updated = `class`.apply(command)
        if (updated != `class`) {
            repository.save(updated)
        }
    }

    override fun deleteAll() = repository.deleteAll()
}
