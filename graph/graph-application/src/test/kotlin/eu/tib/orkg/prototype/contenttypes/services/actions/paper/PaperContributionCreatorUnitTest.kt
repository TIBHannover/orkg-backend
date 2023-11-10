package eu.tib.orkg.prototype.contenttypes.services.actions.paper

import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.services.actions.BakedStatement
import eu.tib.orkg.prototype.contenttypes.services.actions.PaperState
import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreatePaperCommand
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
import eu.tib.orkg.prototype.statements.testing.fixtures.createClass
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PaperContributionCreatorUnitTest {
    private val statementRepository: StatementRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val predicateService: PredicateUseCases = mockk()
    private val listService: ListUseCases = mockk()

    private val paperContentsCreatorCreator = PaperContributionCreator(
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
    fun `Given a paper create command, when creating its contents, it returns success`() {
        val temp1 = CreatePaperUseCase.CreateCommand.PredicateDefinition(
            label = "hasResult"
        )
        val contributionDefinition = CreatePaperUseCase.CreateCommand.Contribution(
            label = "Contribution 1",
            classes = setOf(ThingId("C123")),
            statements = mapOf(
                "#temp1" to listOf(
                    CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003")
                )
            )
        )
        val template = createClass(ThingId("C123"))
        val resource = createResource(ThingId("R3003"))
        val paperId = ThingId("R15632")

        val command = dummyCreatePaperCommand().copy(
            contents = CreatePaperUseCase.CreateCommand.PaperContents(
                predicates = mapOf("#temp1" to temp1),
                contributions = listOf(contributionDefinition)
            )
        )
        val state = PaperState(
            tempIds = setOf("#temp1"),
            validatedIds = mapOf(
                "#temp1" to Either.left("#temp1"),
                template.id.value to Either.right(template),
                resource.id.value to Either.right(resource)
            ),
            bakedStatements = setOf(
                BakedStatement("^0", "#temp1", "R3003")
            ),
            paperId = paperId
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
                    classes = setOf(Classes.contribution, ThingId("C123")),
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

        val result = paperContentsCreatorCreator(command, state)

        result.asClue {
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds
            it.bakedStatements shouldBe state.bakedStatements
            it.authors shouldBe state.authors
            it.paperId shouldBe state.paperId
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
                    classes = setOf(Classes.contribution, ThingId("C123")),
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
