package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.CreateRosettaStoneTemplateAction.State
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository

class RosettaStoneTemplatePropertiesCreateValidator(
    private val abstractRosettaStoneTemplatePropertiesValidator: AbstractRosettaStoneTemplatePropertiesValidator,
) : CreateRosettaStoneTemplateAction {
    constructor(
        predicateRepository: PredicateRepository,
        classRepository: ClassRepository,
    ) : this(
        AbstractRosettaStoneTemplatePropertiesValidator(predicateRepository, classRepository)
    )

    override fun invoke(command: CreateRosettaStoneTemplateCommand, state: State): State =
        state.also { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties) }
}
