package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementState
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementState
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementContextValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementCreator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementExistenceValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementModifiableValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementPropertyValueCreateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementPropertyValueUpdateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementTempIdCreateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementTempIdUpdateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementTemplateCreateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementTemplateUpdateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementThingDefinitionCreateCreator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementThingDefinitionCreateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementThingDefinitionUpdateCreator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementThingDefinitionUpdateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.RosettaStoneStatementUpdater
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.domain.NotACurator
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.OffsetDateTime
import java.util.Optional

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
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val literalService: LiteralUseCases,
    private val unsafePredicateUseCases: UnsafePredicateUseCases,
    private val statementRepository: StatementRepository,
    private val listService: ListUseCases,
    private val contributorRepository: ContributorRepository,
    private val classHierarchyRepository: ClassHierarchyRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : RosettaStoneStatementUseCases {
    override fun findByIdOrVersionId(id: ThingId): Optional<RosettaStoneStatement> =
        repository.findByIdOrVersionId(id)

    override fun findAll(
        pageable: Pageable,
        context: ThingId?,
        templateId: ThingId?,
        templateTargetClassId: ThingId?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
    ): Page<RosettaStoneStatement> =
        repository.findAll(
            pageable = pageable,
            context = context,
            templateId = templateId,
            templateTargetClassId = templateTargetClassId,
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId
        )

    override fun create(command: CreateRosettaStoneStatementCommand): ThingId {
        val steps = listOf(
            RosettaStoneStatementTempIdCreateValidator(),
            RosettaStoneStatementTemplateCreateValidator(rosettaStoneTemplateService),
            RosettaStoneStatementContextValidator(resourceRepository),
            ObservatoryValidator(observatoryRepository, { it.observatories }),
            OrganizationValidator(organizationRepository, { it.organizations }),
            RosettaStoneStatementThingDefinitionCreateValidator(thingRepository, classRepository),
            RosettaStoneStatementPropertyValueCreateValidator(thingRepository, statementRepository, classHierarchyRepository, this),
            RosettaStoneStatementThingDefinitionCreateCreator(classService, unsafeResourceUseCases, unsafeStatementUseCases, literalService, unsafePredicateUseCases, statementRepository, listService),
            RosettaStoneStatementCreator(repository, thingRepository, clock)
        )
        return steps.execute(command, CreateRosettaStoneStatementState()).rosettaStoneStatementId!!
    }

    override fun update(command: UpdateRosettaStoneStatementCommand): ThingId {
        val steps = listOf(
            RosettaStoneStatementTempIdUpdateValidator(),
            RosettaStoneStatementExistenceValidator(this),
            RosettaStoneStatementModifiableValidator(),
            RosettaStoneStatementTemplateUpdateValidator(rosettaStoneTemplateService),
            ObservatoryValidator(observatoryRepository, { it.observatories }, { it.observatories }),
            OrganizationValidator(organizationRepository, { it.organizations }, { it.organizations }),
            RosettaStoneStatementThingDefinitionUpdateValidator(thingRepository, classRepository),
            RosettaStoneStatementPropertyValueUpdateValidator(thingRepository, statementRepository, classHierarchyRepository, this),
            RosettaStoneStatementThingDefinitionUpdateCreator(classService, unsafeResourceUseCases, unsafeStatementUseCases, literalService, unsafePredicateUseCases, statementRepository, listService),
            RosettaStoneStatementUpdater(repository, thingRepository, clock)
        )
        return steps.execute(command, UpdateRosettaStoneStatementState()).rosettaStoneStatementId!!
    }

    override fun softDelete(id: ThingId, contributorId: ContributorId) {
        findByIdOrVersionId(id).ifPresent {
            if (!it.modifiable) {
                throw RosettaStoneStatementNotModifiable(id)
            }
            if (it.id != id) {
                throw CannotDeleteIndividualRosettaStoneStatementVersion()
            }
            if (it.visibility != Visibility.DELETED) {
                repository.softDelete(id, contributorId)
            }
        }
    }

    override fun delete(id: ThingId, contributorId: ContributorId) {
        findByIdOrVersionId(id).ifPresent {
            if (!it.modifiable) {
                throw RosettaStoneStatementNotModifiable(id)
            }
            if (it.id != id) {
                throw CannotDeleteIndividualRosettaStoneStatementVersion()
            }
            val contributor = contributorRepository.findById(contributorId)
                .orElseThrow { ContributorNotFound(contributorId) }
            if (!contributor.isCurator) {
                throw NotACurator(contributorId)
            }
            if (repository.isUsedAsObject(id)) {
                throw RosettaStoneStatementInUse(id)
            }
            repository.delete(id)
        }
    }

    private inline val UpdateRosettaStoneStatementState.observatories: List<ObservatoryId> get() =
        rosettaStoneStatement?.let { it.observatories + it.versions.flatMap(RosettaStoneStatementVersion::observatories) }.orEmpty() +
            rosettaStoneStatement?.observatories.orEmpty()

    private inline val UpdateRosettaStoneStatementState.organizations: List<OrganizationId> get() =
        rosettaStoneStatement?.let { it.organizations + it.versions.flatMap(RosettaStoneStatementVersion::organizations) }.orEmpty() +
            rosettaStoneStatement?.organizations.orEmpty()
}
