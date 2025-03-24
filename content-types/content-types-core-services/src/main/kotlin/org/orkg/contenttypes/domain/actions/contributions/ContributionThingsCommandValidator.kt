package org.orkg.contenttypes.domain.actions.contributions

import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.contenttypes.domain.actions.contributions.ContributionAction.State
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class ContributionThingsCommandValidator(
    private val thingsCommandValidator: ThingsCommandValidator,
) : ContributionAction {
    constructor(
        thingRepository: ThingRepository,
        classRepository: ClassRepository,
    ) : this(
        ThingsCommandValidator(thingRepository, classRepository)
    )

    override fun invoke(command: CreateContributionCommand, state: State): State =
        state.copy(
            validationCache = thingsCommandValidator.validate(
                thingsCommand = command,
                validationCache = state.validationCache
            )
        )
}
