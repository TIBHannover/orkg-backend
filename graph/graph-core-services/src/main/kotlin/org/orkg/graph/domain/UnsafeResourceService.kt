package org.orkg.graph.domain

import java.time.Clock
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase.UpdateCommand
import org.orkg.graph.output.ResourceRepository
import org.springframework.stereotype.Service

@Service
class UnsafeResourceService(
    protected val repository: ResourceRepository,
    protected val clock: Clock,
) : UnsafeResourceUseCases {
    override fun create(command: CreateResourceUseCase.CreateCommand): ThingId {
        val resource = Resource(
            id = command.id ?: repository.nextIdentity(),
            label = command.label,
            classes = command.classes,
            extractionMethod = command.extractionMethod ?: ExtractionMethod.UNKNOWN,
            createdAt = OffsetDateTime.now(clock),
            createdBy = command.contributorId ?: ContributorId.UNKNOWN,
            observatoryId = command.observatoryId ?: ObservatoryId.UNKNOWN,
            organizationId = command.organizationId ?: OrganizationId.UNKNOWN,
            modifiable = command.modifiable
        )
        repository.save(resource)
        return resource.id
    }

    override fun update(command: UpdateCommand) {
        if (!command.hasNoContents()) {
            val resource = repository.findById(command.id)
                .orElseThrow { ResourceNotFound.withId(command.id) }
            val updated = resource.apply(command)
            if (updated != resource) {
                repository.save(updated)
            }
        }
    }

    override fun delete(id: ThingId, contributorId: ContributorId) {
        repository.deleteById(id)
    }

    override fun removeAll() = repository.deleteAll()
}
