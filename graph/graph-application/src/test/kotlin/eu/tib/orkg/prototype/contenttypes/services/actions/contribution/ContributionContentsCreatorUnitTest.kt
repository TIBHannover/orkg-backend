package eu.tib.orkg.prototype.contenttypes.services.actions.contribution

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.services.actions.BakedStatement
import eu.tib.orkg.prototype.contenttypes.services.actions.ContributionState
import eu.tib.orkg.prototype.contenttypes.services.actions.CreateContributionCommand
import eu.tib.orkg.prototype.shared.Either
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.CreatePredicateUseCase
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.api.ListUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.testing.fixtures.createResource
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContributionContentsCreatorUnitTest {
    private val statementRepository: StatementRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val predicateService: PredicateUseCases = mockk()
    private val listService: ListUseCases = mockk()

    private val contributionContentsCreatorCreator = ContributionContentsCreator(
        resourceService = resourceService,
        statementService = statementService,
        literalService = literalService,
        predicateService = predicateService,
        statementRepository = statementRepository,
        listService = listService
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(
            statementRepository,
            resourceService,
            statementService,
            literalService,
            predicateService,
            listService
        )
    }

    @Test
    fun `Given a contribution create command, when creating its contents, it returns success`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val temp1 = CreatePaperUseCase.CreateCommand.PredicateDefinition(
            label = "hasResult"
        )
        val contributionDefinition = CreatePaperUseCase.CreateCommand.Contribution(
            label = "Contribution 1",
            statements = mapOf(
                "#temp1" to listOf(
                    CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003")
                )
            )
        )
        val resource = createResource(id = ThingId("R3003"))
        val paperId = ThingId("R15632")

        val command = CreateContributionCommand(
            paperId = paperId,
            contributorId = contributorId,
            predicates = mapOf("#temp1" to temp1),
            contribution = contributionDefinition
        )
        val state = ContributionState(
            tempIds = setOf("#temp1"),
            validatedIds = mapOf(
                "#temp1" to Either.left("#temp1"),
                resource.id.value to Either.right(resource)
            ),
            bakedStatements = setOf(
                BakedStatement("^0", "#temp1", "R3003")
            )
        )
        val contributionId = ThingId("R456")
        val predicateId = ThingId("R789")

        every {
            predicateService.create(
                CreatePredicateUseCase.CreateCommand(
                    label = temp1.label,
                    contributorId = command.contributorId
                )
            )
        } returns predicateId
        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = contributionDefinition.label,
                    classes = setOf(Classes.contribution),
                    contributorId = command.contributorId
                )
            )
        } returns contributionId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = paperId,
                predicate = Predicates.hasContribution,
                `object` = contributionId
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = contributionId,
                predicate = predicateId,
                `object` = ThingId("R3003")
            )
        } just runs

        val result = contributionContentsCreatorCreator(command, state)

        result.asClue {
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds
            it.bakedStatements shouldBe state.bakedStatements
            it.contributionId shouldBe contributionId
        }

        verify(exactly = 1) {
            predicateService.create(
                CreatePredicateUseCase.CreateCommand(
                    label = temp1.label,
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = contributionDefinition.label,
                    classes = setOf(Classes.contribution),
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = paperId,
                predicate = Predicates.hasContribution,
                `object` = contributionId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = contributionId,
                predicate = predicateId,
                `object` = ThingId("R3003")
            )
        }
    }
}
