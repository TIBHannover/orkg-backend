package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.contenttypes.domain.actions.CreateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.templates.instances.CreateTemplateInstanceAction.State
import org.orkg.graph.domain.InvalidClassCollection
import org.orkg.graph.domain.ReservedClassId
import org.orkg.graph.domain.ThingAlreadyExists
import org.orkg.graph.domain.reservedClassIds
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class TemplateInstanceSubjectCreateValidator(
    private val thingRepository: ThingRepository,
    private val classRepository: ClassRepository,
) : CreateTemplateInstanceAction {
    override fun invoke(command: CreateTemplateInstanceCommand, state: State): State {
        command.id?.also { id ->
            thingRepository.findById(id).ifPresent { throw ThingAlreadyExists(id) }
        }
        val classes = command.additionalClasses
        if (classes.isNotEmpty()) {
            val reserved = classes.intersect(reservedClassIds)
            if (reserved.isNotEmpty()) {
                throw ReservedClassId(reserved.first())
            }
            if (!classRepository.existsAllById(classes - reservedClassIds)) {
                throw InvalidClassCollection(classes)
            }
        }
        return state
    }
}
