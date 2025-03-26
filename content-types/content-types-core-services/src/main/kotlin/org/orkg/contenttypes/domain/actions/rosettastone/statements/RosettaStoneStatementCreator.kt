package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.common.Either.Companion.merge
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.domain.RosettaStoneStatementVersion
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.rosettastone.statements.CreateRosettaStoneStatementAction.State
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.output.ThingRepository
import java.time.Clock
import java.time.OffsetDateTime

class RosettaStoneStatementCreator(
    private val rosettaStoneStatementRepository: RosettaStoneStatementRepository,
    private val thingRepository: ThingRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : CreateRosettaStoneStatementAction {
    override fun invoke(command: CreateRosettaStoneStatementCommand, state: State): State {
        val version = RosettaStoneStatementVersion(
            id = rosettaStoneStatementRepository.nextIdentity(),
            formattedLabel = state.rosettaStoneTemplate!!.formattedLabel,
            subjects = command.subjects.map { state.resolve(it) },
            objects = command.objects.map { objects -> objects.map { state.resolve(it) } },
            createdAt = OffsetDateTime.now(clock),
            createdBy = command.contributorId,
            certainty = command.certainty,
            negated = command.negated,
            observatories = command.observatories,
            organizations = command.organizations,
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
            modifiable = command.modifiable
        )
        val statement = RosettaStoneStatement(
            id = rosettaStoneStatementRepository.nextIdentity(),
            contextId = command.context,
            templateId = command.templateId,
            templateTargetClassId = state.rosettaStoneTemplate.targetClass,
            label = "", // empty label, because we do want the underlying resource to be findable via resource search endpoints
            versions = listOf(version),
            observatories = command.observatories,
            organizations = command.organizations,
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
            modifiable = command.modifiable,
        )
        rosettaStoneStatementRepository.save(statement)
        return state.copy(rosettaStoneStatementId = statement.id)
    }

    // FIXME: Fetching thing instances that are effectively not needed by the repository should be avoided.
    //        Only the ids are needed in order to save the statement.
    private fun State.resolve(id: String) =
        validationCache[id]!!.mapLeft { thingRepository.findById(tempIdToThingId[id]!!).get() }.merge()
}
