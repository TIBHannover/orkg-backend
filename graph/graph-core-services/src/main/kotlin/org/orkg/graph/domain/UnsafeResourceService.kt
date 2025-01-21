package org.orkg.graph.domain

import java.time.Clock
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase
import org.orkg.graph.output.ResourceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
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

    override fun update(command: UpdateResourceUseCase.UpdateCommand) {
        var found = repository.findById(command.id).get()

        // update all the properties
        if (command.label != null) found = found.copy(label = command.label!!)
        if (command.classes != null) found = found.copy(classes = command.classes!!)
        if (command.observatoryId != null) found = found.copy(observatoryId = command.observatoryId!!)
        if (command.organizationId != null) found = found.copy(organizationId = command.organizationId!!)
        if (command.extractionMethod != null) found = found.copy(extractionMethod = command.extractionMethod!!)
        if (command.modifiable != null) found = found.copy(modifiable = command.modifiable!!)

        repository.save(found)
    }

    override fun delete(id: ThingId, contributorId: ContributorId) {
        repository.deleteById(id)
    }

    override fun removeAll() = repository.deleteAll()
}
