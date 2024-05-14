package org.orkg.contenttypes.domain

import java.time.Clock
import java.util.*
import org.orkg.common.ThingId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementState
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementContextValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementCreator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementPropertyValueValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementTempIdCreateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementTemplateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementThingDefinitionCreateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementThingDefinitionCreator
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class RosettaStoneStatementService(
    private val repository: RosettaStoneStatementRepository,
    private val rosettaStoneTemplateService: RosettaStoneTemplateUseCases,
    private val resourceRepository: ResourceRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository,
    private val thingRepository: ThingRepository,
    private val classRepository: ClassRepository,
    private val classService: ClassUseCases,
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val predicateService: PredicateUseCases,
    private val statementRepository: StatementRepository,
    private val listService: ListUseCases,
    private val clock: Clock = Clock.systemDefaultZone()
) : RosettaStoneStatementUseCases {
    override fun findByIdOrVersionId(id: ThingId): Optional<RosettaStoneStatement> =
        repository.findByIdOrVersionId(id)

    override fun findAll(pageable: Pageable): Page<RosettaStoneStatement> =
        repository.findAll(pageable)

    override fun create(command: CreateRosettaStoneStatementCommand): ThingId {
        val steps = listOf(
            RosettaStoneStatementTempIdCreateValidator(),
            RosettaStoneStatementTemplateValidator(rosettaStoneTemplateService),
            RosettaStoneStatementContextValidator(resourceRepository),
            ObservatoryValidator(observatoryRepository, { it.observatories }),
            OrganizationValidator(organizationRepository, { it.organizations }),
            RosettaStoneStatementThingDefinitionCreateValidator(thingRepository, classRepository),
            RosettaStoneStatementPropertyValueValidator(thingRepository),
            RosettaStoneStatementThingDefinitionCreator(classService, resourceService, statementService, literalService, predicateService, statementRepository, listService),
            RosettaStoneStatementCreator(repository, thingRepository, clock)
        )
        return steps.execute(command, CreateRosettaStoneStatementState()).rosettaStoneStatementId!!
    }
}
