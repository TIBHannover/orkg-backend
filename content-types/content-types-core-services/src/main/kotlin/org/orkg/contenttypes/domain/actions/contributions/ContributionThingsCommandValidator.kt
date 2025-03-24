package org.orkg.contenttypes.domain.actions.contributions

import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class ContributionThingsCommandValidator(
    private val thingsCommandValidator: ThingsCommandValidator,
) : ContributionAction {
    constructor(
        thingRepository: ThingRepository,
        classRepository: ClassRepository,
    ) : this(ThingsCommandValidator(thingRepository, classRepository))

    override operator fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState =
        state.copy(
            validationCache = thingsCommandValidator.validate(
                thingsCommand = command,
                validationCache = state.validationCache
            )
        )
}
