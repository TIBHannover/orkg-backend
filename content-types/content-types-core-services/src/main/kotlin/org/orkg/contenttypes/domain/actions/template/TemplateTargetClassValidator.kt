package org.orkg.contenttypes.domain.actions.template

import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.TemplateAlreadyExistsForClass
import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.template.TemplateAction.State
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.StatementRepository

class TemplateTargetClassValidator(
    private val classRepository: ClassRepository,
    private val statementRepository: StatementRepository
) : TemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State {
        classRepository.findById(command.targetClass).orElseThrow { ClassNotFound.withThingId(command.targetClass) }
        val statements = statementRepository.findAll(
            predicateId = Predicates.shTargetClass,
            objectId = command.targetClass,
            pageable = PageRequests.SINGLE
        )
        if (statements.numberOfElements > 0) {
            throw TemplateAlreadyExistsForClass(command.targetClass, statements.single().subject.id)
        }
        return state
    }
}
