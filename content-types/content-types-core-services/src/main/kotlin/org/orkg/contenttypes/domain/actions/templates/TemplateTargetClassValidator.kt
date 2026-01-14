package org.orkg.contenttypes.domain.actions.templates

import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.TemplateAlreadyExistsForClass
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.ReservedClass
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.StatementRepository

class TemplateTargetClassValidator<T, S>(
    private val classRepository: ClassRepository,
    private val statementRepository: StatementRepository,
    private val newValueSelector: (T) -> ThingId?,
    private val oldValueSelector: (S) -> ThingId? = { null },
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val newTargetClass = newValueSelector(command)
        val oldTargetClass = oldValueSelector(state)
        if (newTargetClass != null && newTargetClass != oldTargetClass) {
            if (newTargetClass == Classes.rosettaStoneStatement) {
                throw ReservedClass(newTargetClass)
            }
            classRepository.findById(newTargetClass).orElseThrow { ClassNotFound.withThingId(newTargetClass) }
            val rsStatements = statementRepository.findAll(
                subjectClasses = setOf(Classes.rosettaNodeShape),
                predicateId = Predicates.shTargetClass,
                objectId = newTargetClass,
                pageable = PageRequests.SINGLE,
            )
            if (rsStatements.numberOfElements > 0) {
                throw ReservedClass(newTargetClass)
            }
            val statements = statementRepository.findAll(
                subjectClasses = setOf(Classes.nodeShape),
                predicateId = Predicates.shTargetClass,
                objectId = newTargetClass,
                pageable = PageRequests.SINGLE
            )
            if (statements.numberOfElements > 0) {
                throw TemplateAlreadyExistsForClass(newTargetClass, statements.single().subject.id)
            }
        }
        return state
    }
}
